package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.entity.EquipmentType;
import com.lmkr.hesco.survey.entity.SePointType;
import com.lmkr.hesco.survey.entity.SurveyForm;
import com.lmkr.hesco.survey.repository.EquipmentTypeRepository;
import com.lmkr.hesco.survey.repository.SurveyFormRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Owns survey-form submission/sync (SRS §8.3). Every submission runs
 * EquipmentSequenceValidator against the previous form's End Point
 * equipment (§8.3.2) and GpsNumberService.assertUniqueOnSync (§8.3.1)
 * BEFORE the row is written, mirroring the offline validation the mobile
 * app already ran.
 */
@Service
public class SurveyService {

    private final SurveyFormRepository surveyFormRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentSequenceValidator equipmentSequenceValidator;
    private final GpsNumberService gpsNumberService;

    public SurveyService(SurveyFormRepository surveyFormRepository, EquipmentTypeRepository equipmentTypeRepository,
                          EquipmentSequenceValidator equipmentSequenceValidator, GpsNumberService gpsNumberService) {
        this.surveyFormRepository = surveyFormRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.equipmentSequenceValidator = equipmentSequenceValidator;
        this.gpsNumberService = gpsNumberService;
    }

    public List<SurveyForm> findByWorkOrder(Long workOrderId) {
        return surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrderId);
    }

    @Transactional
    public SurveyForm submit(WorkOrder workOrder, SePointType sePoint, String equipmentTypeCode, String gpsNumber,
                              BigDecimal lineLengthMeters, AppUser submittedBy, Double latitude, Double longitude,
                              String remarks) {
        EquipmentType equipmentType = equipmentTypeRepository.findByCode(equipmentTypeCode)
            .orElseThrow(() -> new EntityNotFoundException("Equipment Type not found: " + equipmentTypeCode));

        List<SurveyForm> existing = surveyFormRepository.findByWorkOrderIdOrderByIdAsc(workOrder.getId());
        Optional<EquipmentType> previousEndEquipment = existing.isEmpty()
            ? Optional.empty()
            : Optional.of(existing.get(existing.size() - 1).getEquipmentType());

        equipmentSequenceValidator.validate(sePoint, equipmentType, previousEndEquipment);
        gpsNumberService.assertUniqueOnSync(gpsNumber);

        SurveyForm form = new SurveyForm(workOrder, sePoint, gpsNumber, equipmentType, lineLengthMeters,
            submittedBy, latitude, longitude, remarks);
        return surveyFormRepository.save(form);
    }
}
