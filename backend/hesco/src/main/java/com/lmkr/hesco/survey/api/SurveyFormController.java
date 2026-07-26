package com.lmkr.hesco.survey.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.survey.api.dto.SurveyFormRequest;
import com.lmkr.hesco.survey.api.dto.SurveyFormResponse;
import com.lmkr.hesco.survey.entity.SePointType;
import com.lmkr.hesco.survey.service.SurveyService;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Survey form submission/sync from the mobile survey app (SRS §8.3).
 * Backend re-validation mirror of the offline checks the mobile app
 * already ran (equipment sequence + GPS number uniqueness).
 */
@RestController
@RequestMapping("/api/survey-forms")
public class SurveyFormController {

    private final SurveyService surveyService;
    private final WorkOrderRepository workOrderRepository;
    private final AppUserRepository appUserRepository;

    public SurveyFormController(SurveyService surveyService, WorkOrderRepository workOrderRepository,
                                 AppUserRepository appUserRepository) {
        this.surveyService = surveyService;
        this.workOrderRepository = workOrderRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public ApiResponse<List<SurveyFormResponse>> listByWorkOrder(@RequestParam Long workOrderId) {
        return ApiResponse.ok(surveyService.findByWorkOrder(workOrderId).stream().map(SurveyFormResponse::from).toList());
    }

    @PostMapping("/sync")
    public ApiResponse<SurveyFormResponse> submit(@Valid @RequestBody SurveyFormRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(request.workOrderId())
            .orElseThrow(() -> new EntityNotFoundException("Work Order not found: " + request.workOrderId()));
        AppUser submittedBy = appUserRepository.findById(request.submittedByUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.submittedByUserId()));

        var form = surveyService.submit(workOrder, SePointType.valueOf(request.sePoint()), request.equipmentTypeCode(),
            request.gpsNumber(), request.lineLengthMeters(), submittedBy, request.latitude(), request.longitude(),
            request.remarks());

        return ApiResponse.ok(SurveyFormResponse.from(form), "Survey form synced");
    }
}
