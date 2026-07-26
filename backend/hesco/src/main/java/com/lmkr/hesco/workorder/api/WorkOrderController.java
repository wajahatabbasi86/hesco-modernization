package com.lmkr.hesco.workorder.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.repository.FeederRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.workorder.api.dto.WorkOrderCreateRequest;
import com.lmkr.hesco.workorder.api.dto.WorkOrderResponse;
import com.lmkr.hesco.workorder.api.dto.WorkOrderTransitionRequest;
import com.lmkr.hesco.workorder.entity.WorkOrderType;
import com.lmkr.hesco.workorder.service.WorkOrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Work Order lifecycle (SRS §3.6): create + the 4-tier
 * Creator -> Approver 1 -> Approver 2 -> GIS Admin transition chain.
 * Every status change is POSTed as an action (VALIDATE/SUBMIT/REVERT/
 * APPROVE/REJECT/POST/DELETE) rather than a raw PATCH of the status
 * field, so WorkOrderStateMachineService is always the arbiter.
 */
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final FeederRepository feederRepository;
    private final AppUserRepository appUserRepository;

    public WorkOrderController(WorkOrderService workOrderService, FeederRepository feederRepository,
                                AppUserRepository appUserRepository) {
        this.workOrderService = workOrderService;
        this.feederRepository = feederRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public ApiResponse<List<WorkOrderResponse>> list() {
        return ApiResponse.ok(workOrderService.findAll().stream().map(WorkOrderResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(WorkOrderResponse.from(workOrderService.findById(id)));
    }

    @PostMapping
    public ApiResponse<WorkOrderResponse> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        Feeder feeder = feederRepository.findById(request.feederId())
            .orElseThrow(() -> new EntityNotFoundException("Feeder not found: " + request.feederId()));
        AppUser creator = appUserRepository.findById(request.createdByUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.createdByUserId()));

        var workOrder = workOrderService.create(feeder, WorkOrderType.valueOf(request.woType()), creator,
            request.locationLat(), request.locationLng());
        return ApiResponse.ok(WorkOrderResponse.from(workOrder), "Work Order created");
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<WorkOrderResponse> transition(@PathVariable Long id, @Valid @RequestBody WorkOrderTransitionRequest request) {
        AppUser actor = appUserRepository.findById(request.actorUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.actorUserId()));

        var workOrder = workOrderService.transition(id, request.actionCode(), actor, request.comment());
        return ApiResponse.ok(WorkOrderResponse.from(workOrder), "Work Order transitioned");
    }
}
