package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.api.dto.*;
import com.lmkr.hesco.survey.entity.*;
import com.lmkr.hesco.survey.exception.InvalidSurveyDetailException;
import com.lmkr.hesco.survey.repository.*;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.warehouse.entity.ItemCategory;
import com.lmkr.hesco.warehouse.entity.ItemType;
import com.lmkr.hesco.warehouse.repository.ItemCategoryRepository;
import com.lmkr.hesco.warehouse.repository.ItemTypeRepository;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.entity.WorkOrderType;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class SurveyService {

    private static final String FEEDER_POLE = "FEEDER_POLE";
    private static final String PRIMARY_POLE = "PRIMARY_POLE";
    private static final String SECONDARY_POLE = "SECONDARY_POLE";
    private static final String TRANSFORMER = "TRANSFORMER";
    private static final String METER = "METER";

    private final SurveyFormRepository surveyFormRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentSequenceValidator equipmentSequenceValidator;
    private final GpsNumberService gpsNumberService;
    private final WorkOrderRepository workOrderRepository;
    private final AppUserRepository userRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final PoleDetailRepository poleDetailRepository;
    private final ConductorDetailRepository conductorDetailRepository;
    private final TransformerDetailRepository transformerDetailRepository;
    private final MeterDetailRepository meterDetailRepository;

    // ===============================
    // READ
    // ===============================
    /**
     * Returns DTOs, mapped inside this @Transactional method - SurveyForm's
     * associations are all FetchType.LAZY and open-in-view is disabled.
     * Detail rows are fetched per-form via findBySurveyFormId; fine at
     * per-work-order volumes (a handful of forms), worth batching if this
     * is ever called for a whole feeder/report-scale list instead.
     */
    @Transactional(readOnly = true)
    public List<SurveyFormResponse> findResponsesByWorkOrder(Long workOrderId) {
        return surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrderId)
                .stream()
                .map(this::toResponseWithDetail)
                .toList();
    }

    public List<SurveyForm> findByWorkOrder(Long workOrderId) {
        return surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrderId);
    }

    private SurveyFormResponse toResponseWithDetail(SurveyForm form) {
        PoleDetailResponse pole = poleDetailRepository.findBySurveyFormId(form.getId())
                .map(PoleDetailResponse::from).orElse(null);
        List<ConductorDetailResponse> conductor = ConductorDetailResponse.fromAll(
                conductorDetailRepository.findBySurveyFormIdOrderByPhaseAsc(form.getId()));
        TransformerDetailResponse transformer = transformerDetailRepository.findBySurveyFormId(form.getId())
                .map(TransformerDetailResponse::from).orElse(null);
        MeterDetailResponse meter = meterDetailRepository.findBySurveyFormId(form.getId())
                .map(MeterDetailResponse::from).orElse(null);
        return SurveyFormResponse.from(form, pole, conductor, transformer, meter);
    }

    // ===============================
    // SUBMIT
    // ===============================
    @Transactional
    public SurveyForm submit(SurveyFormRequest request) {
        return submitWithResponse(request).form();
    }

    @Transactional
    public SurveyFormResponse submitForResponse(SurveyFormRequest request) {
        SubmitResult result = submitWithResponse(request);
        return SurveyFormResponse.from(
                result.form(),
                result.poleDetail() != null ? PoleDetailResponse.from(result.poleDetail()) : null,
                ConductorDetailResponse.fromAll(result.conductorDetails()),
                result.transformerDetail() != null ? TransformerDetailResponse.from(result.transformerDetail()) : null,
                result.meterDetail() != null ? MeterDetailResponse.from(result.meterDetail()) : null
        );
    }

    private record SubmitResult(SurveyForm form, PoleDetail poleDetail, List<ConductorDetail> conductorDetails,
                                TransformerDetail transformerDetail, MeterDetail meterDetail) {}

    private SubmitResult submitWithResponse(SurveyFormRequest request) {

        // 1. Resolve entities (Section Information — SRS §8.3.1)
        WorkOrder workOrder = workOrderRepository.findById(request.workOrderId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Work Order not found: " + request.workOrderId()));

        AppUser submittedBy = userRepository.findById(request.submittedByUserId())
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found: " + request.submittedByUserId()));

        EquipmentType equipmentType = equipmentTypeRepository.findByCode(request.equipmentTypeCode())
                .orElseThrow(() ->
                        new EntityNotFoundException("Equipment Type not found: " + request.equipmentTypeCode()));

        SePointType sePoint = SePointType.valueOf(request.sePoint());

        // 2. Section-level validation (equipment sequence + GPS number)
        List<SurveyForm> existing = surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrder.getId());

        Optional<EquipmentType> previousEndEquipment = existing.isEmpty()
                ? Optional.empty()
                : Optional.of(existing.get(existing.size() - 1).getEquipmentType());

        equipmentSequenceValidator.validate(sePoint, equipmentType, previousEndEquipment);

        gpsNumberService.assertUniqueOnSync(request.gpsNumber());

        // 3. Save Section Information
        SurveyForm form = SurveyForm.builder()
                .workOrder(workOrder)
                .sePoint(sePoint)
                .equipmentType(equipmentType)
                .gpsNumber(request.gpsNumber())
                .lineLengthMeters(request.lineLengthMeters())
                .submittedBy(submittedBy)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .remarks(request.remarks())
                .syncedAt(OffsetDateTime.now())
                .build();

        form = surveyFormRepository.save(form);

        // 4. Conditional detail payload (SRS §8.3.3-§8.3.6) — exactly the
        //    block matching this form's equipment type is required; any
        //    other detail block being present is rejected outright rather
        //    than silently ignored, since a mismatched payload usually
        //    means the mobile client got the equipment type wrong.
        String equipmentCode = equipmentType.getCode();

        PoleDetail poleDetail = null;
        List<ConductorDetail> conductorDetails = List.of();
        TransformerDetail transformerDetail = null;
        MeterDetail meterDetail = null;

        boolean expectsPole = equipmentCode.equals(FEEDER_POLE) || equipmentCode.equals(PRIMARY_POLE)
                || equipmentCode.equals(SECONDARY_POLE);
        boolean expectsTransformer = equipmentCode.equals(TRANSFORMER);
        boolean expectsMeter = equipmentCode.equals(METER);
        boolean expectsConductor = sePoint == SePointType.END_POINT; // SRS §8.3.4 — independent of equipment type

        if (expectsPole) {
            if (request.poleDetail() == null) {
                throw new InvalidSurveyDetailException(
                        "poleDetail is required when equipmentTypeCode is " + equipmentCode + " (SRS §8.3.3)");
            }
            // Feeder Pole / Primary Pole use the primary (HT-side) structure
            // list; Secondary Pole uses the secondary (LT-side) list (SRS §3.15.2.2).
            String structureCategory = equipmentCode.equals(SECONDARY_POLE) ? "SECONDARY_STRUCTURE" : "PRIMARY_STRUCTURE";
            PoleDetailRequest poleReq = request.poleDetail();
            ItemType structureType = resolveItemType(structureCategory, poleReq.structureTypeCode(), "poleDetail.structureTypeCode");
            // endType/poleAssembly are optional lookups — POLE_END_TYPE/POLE_ASSEMBLY
            // are still unseeded pending HESCO/LMKR's dropdown values, so a blank
            // code just means "not captured yet," not a validation failure.
            ItemType endType = resolveOptionalItemType("POLE_END_TYPE", poleReq.endTypeCode(), "poleDetail.endTypeCode");
            ItemType poleAssembly = resolveOptionalItemType("POLE_ASSEMBLY", poleReq.poleAssemblyCode(), "poleDetail.poleAssemblyCode");

            poleDetail = PoleDetail.builder()
                    .surveyForm(form)
                    .structureType(structureType)
                    .poleNumber(poleReq.poleNumber())
                    .heightMeters(poleReq.heightMeters())
                    .noOfFeeders(poleReq.noOfFeeders())
                    .endType(endType)
                    .poleAssembly(poleAssembly)
                    .poleEarthing(poleReq.poleEarthing())
                    .assetCode(poleReq.assetCode())
                    .build();
            poleDetail = poleDetailRepository.save(poleDetail);
        } else if (request.poleDetail() != null) {
            throw new InvalidSurveyDetailException(
                    "poleDetail must not be supplied when equipmentTypeCode is " + equipmentCode);
        }

        // Conductor (per-phase) is resolved before Transformer so the
        // transformer's derived equipmentPhase can be populated from it —
        // both details live on the same form when equipmentTypeCode is
        // TRANSFORMER, since Transformer is always an End Point (SRS §8.3.2).
        String equipmentPhaseSummary = null;
        if (expectsConductor) {
            if (request.conductorDetail() == null) {
                throw new InvalidSurveyDetailException(
                        "conductorDetail is required when sePoint is END_POINT (SRS §8.3.4)");
            }
            ConductorDetailRequest conductorReq = request.conductorDetail();

            // HT work orders use HT_CONDUCTOR, LT use LT_CONDUCTOR; a
            // FULL_UPDATE work order covers both networks so either
            // category is accepted there (SRS doesn't disambiguate this —
            // same open question as the HT/LT-vs-equipment-sequence gap
            // noted in the README). N (Neutral) is only legal once the
            // resolved category turns out to be LT_CONDUCTOR.
            boolean isLt = workOrder.getWoType() == WorkOrderType.LT;
            boolean isFullUpdate = workOrder.getWoType() == WorkOrderType.FULL_UPDATE;

            List<ConductorPhase> activePhases;
            java.util.Map<ConductorPhase, String> phaseToCode = new java.util.EnumMap<>(ConductorPhase.class);

            if (conductorReq.allPhases()) {
                // Which phases are "active" for an all-phases submission is
                // ambiguous without a resolved category up front, so default
                // to R/Y/B and only add N for a plain LT work order — a
                // FULL_UPDATE or HT survey submitting all-phases gets R/Y/B
                // only; N still has to be supplied explicitly via the
                // per-phase list if a FULL_UPDATE survey needs it.
                activePhases = isLt ? List.of(ConductorPhase.R, ConductorPhase.Y, ConductorPhase.B, ConductorPhase.N)
                                    : List.of(ConductorPhase.R, ConductorPhase.Y, ConductorPhase.B);
                for (ConductorPhase p : activePhases) {
                    phaseToCode.put(p, conductorReq.allPhasesConductorTypeCode());
                }
            } else {
                activePhases = new java.util.ArrayList<>();
                for (ConductorPhaseEntry entry : conductorReq.phases()) {
                    ConductorPhase phase;
                    try {
                        phase = ConductorPhase.valueOf(entry.phase().toUpperCase(java.util.Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new InvalidSurveyDetailException(
                                "conductorDetail.phases contains an invalid phase '" + entry.phase() + "' — must be R, Y, B, or N");
                    }
                    if (phase == ConductorPhase.N && !isLt && !isFullUpdate) {
                        throw new InvalidSurveyDetailException(
                                "Phase N (Neutral) is only valid for LT surveys (SRS §8.3.4)");
                    }
                    if (phaseToCode.containsKey(phase)) {
                        throw new InvalidSurveyDetailException(
                                "conductorDetail.phases has a duplicate entry for phase " + phase);
                    }
                    phaseToCode.put(phase, entry.conductorTypeCode());
                    activePhases.add(phase);
                }
            }

            String conductorCategory = isLt ? "LT_CONDUCTOR" : "HT_CONDUCTOR";
            List<ConductorDetail> saved = new java.util.ArrayList<>();
            for (ConductorPhase phase : activePhases) {
                String code = phaseToCode.get(phase);
                ItemType conductorType;
                if (isFullUpdate) {
                    conductorType = resolveItemTypeAnyOf(
                            List.of("HT_CONDUCTOR", "LT_CONDUCTOR"), code, "conductorDetail (" + phase + ")");
                } else {
                    conductorType = resolveItemType(conductorCategory, code, "conductorDetail (" + phase + ")");
                }
                ConductorDetail row = ConductorDetail.builder()
                        .surveyForm(form)
                        .phase(phase)
                        .conductorType(conductorType)
                        .build();
                saved.add(conductorDetailRepository.save(row));
            }
            conductorDetails = saved;

            List<String> orderedPhaseNames = saved.stream()
                    .sorted(java.util.Comparator.comparing(ConductorDetail::getPhase))
                    .map(c -> c.getPhase().name())
                    .toList();
            equipmentPhaseSummary = String.join(",", orderedPhaseNames);
        } else if (request.conductorDetail() != null) {
            throw new InvalidSurveyDetailException(
                    "conductorDetail must not be supplied when sePoint is not END_POINT");
        }

        if (expectsTransformer) {
            if (request.transformerDetail() == null) {
                throw new InvalidSurveyDetailException(
                        "transformerDetail is required when equipmentTypeCode is TRANSFORMER (SRS §8.3.5)");
            }
            TransformerDetailRequest txReq = request.transformerDetail();
            ItemType capacity = resolveItemType("TRANSFORMER_CAPACITY", txReq.capacityCode(), "transformerDetail.capacityCode");
            ItemType equipmentUse = resolveOptionalItemType("EQUIPMENT_USE", txReq.equipmentUseCode(), "transformerDetail.equipmentUseCode");
            ItemType mounting = resolveOptionalItemType("TRANSFORMER_MOUNTING", txReq.mountingCode(), "transformerDetail.mountingCode");
            ItemType fuses = resolveOptionalItemType("TRANSFORMER_FUSE", txReq.fusesCode(), "transformerDetail.fusesCode");

            transformerDetail = TransformerDetail.builder()
                    .surveyForm(form)
                    .capacity(capacity)
                    .transformerName(txReq.transformerName())
                    .cableSize(txReq.cableSize())
                    .ctRatio(txReq.ctRatio())
                    .equipmentNumber("T-" + form.getGpsNumber()) // SRS §8.3.5 — auto-generated, not client-supplied
                    .equipmentPhase(equipmentPhaseSummary)       // auto-filled from this same form's conductorDetail
                    .equipmentUse(equipmentUse)
                    .mounting(mounting)
                    .fuses(fuses)
                    .assetCode(txReq.assetCode())
                    .consumerName(txReq.consumerName())
                    .equipmentLocation(txReq.equipmentLocation())
                    .build();
            transformerDetail = transformerDetailRepository.save(transformerDetail);
        } else if (request.transformerDetail() != null) {
            throw new InvalidSurveyDetailException(
                    "transformerDetail must not be supplied when equipmentTypeCode is " + equipmentCode);
        }

        if (expectsMeter) {
            if (request.meterDetail() == null) {
                throw new InvalidSurveyDetailException(
                        "meterDetail is required when equipmentTypeCode is METER (SRS §8.3.6)");
            }
            meterDetail = MeterDetail.builder()
                    .surveyForm(form)
                    .meterNumber(request.meterDetail().meterNumber())
                    .consumerReference(request.meterDetail().consumerReference())
                    .sanctionedLoad(request.meterDetail().sanctionedLoad())
                    .meterMake(request.meterDetail().meterMake())
                    .build();
            meterDetail = meterDetailRepository.save(meterDetail);
        } else if (request.meterDetail() != null) {
            throw new InvalidSurveyDetailException(
                    "meterDetail must not be supplied when equipmentTypeCode is " + equipmentCode);
        }

        return new SubmitResult(form, poleDetail, conductorDetails, transformerDetail, meterDetail);
    }

    /**
     * For optional dropdown-backed fields whose reference category may
     * still be unseeded (POLE_END_TYPE, POLE_ASSEMBLY, TRANSFORMER_MOUNTING,
     * TRANSFORMER_FUSE, etc.) — a blank itemCode means "not captured,"
     * not a validation failure. An itemCode that IS supplied still has to
     * resolve to a real item_type, same as the required fields.
     */
    private ItemType resolveOptionalItemType(String categoryCode, String itemCode, String fieldLabel) {
        if (itemCode == null || itemCode.isBlank()) {
            return null;
        }
        return resolveItemType(categoryCode, itemCode, fieldLabel);
    }

    private ItemType resolveItemType(String categoryCode, String itemCode, String fieldLabel) {
        ItemCategory category = itemCategoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new InvalidSurveyDetailException(
                        "Reference category not seeded: " + categoryCode));
        return itemTypeRepository.findByCategoryIdAndCode(category.getId(), itemCode)
                .orElseThrow(() -> new InvalidSurveyDetailException(
                        fieldLabel + " '" + itemCode + "' is not a valid " + categoryCode + " value"));
    }

    private ItemType resolveItemTypeAnyOf(List<String> categoryCodes, String itemCode, String fieldLabel) {
        for (String categoryCode : categoryCodes) {
            Optional<ItemCategory> category = itemCategoryRepository.findByCode(categoryCode);
            if (category.isPresent()) {
                Optional<ItemType> match = itemTypeRepository.findByCategoryIdAndCode(category.get().getId(), itemCode);
                if (match.isPresent()) {
                    return match.get();
                }
            }
        }
        throw new InvalidSurveyDetailException(
                fieldLabel + " '" + itemCode + "' is not a valid value in " + categoryCodes);
    }
}