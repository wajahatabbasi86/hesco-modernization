package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.api.dto.SurveyFormRequest;
import com.lmkr.hesco.survey.entity.EquipmentType;
import com.lmkr.hesco.survey.entity.SePointType;
import com.lmkr.hesco.survey.entity.SurveyForm;
import com.lmkr.hesco.survey.repository.EquipmentTypeRepository;
import com.lmkr.hesco.survey.repository.SurveyFormRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class SurveyService {

    private final SurveyFormRepository surveyFormRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentSequenceValidator equipmentSequenceValidator;
    private final GpsNumberService gpsNumberService;
    private final WorkOrderRepository workOrderRepository;
    private final AppUserRepository userRepository;

    // ===============================
    // READ
    // ===============================
    public List<SurveyForm> findByWorkOrder(Long workOrderId) {
        return surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrderId);
    }

    // ===============================
    // SUBMIT (CLEAN)
    // ===============================
    @Transactional
    public SurveyForm submit(SurveyFormRequest request) {

        // 1. Resolve entities
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

        // 2. Validation
        List<SurveyForm> existing = surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrder.getId());

        Optional<EquipmentType> previousEndEquipment = existing.isEmpty()
                ? Optional.empty()
                : Optional.of(existing.get(existing.size() - 1).getEquipmentType());

        equipmentSequenceValidator.validate(sePoint, equipmentType, previousEndEquipment);

        gpsNumberService.assertUniqueOnSync(request.gpsNumber());

        // 3. Build entity
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
                .build();

        return surveyFormRepository.save(form);
    }
}