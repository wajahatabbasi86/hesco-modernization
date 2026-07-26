package com.lmkr.hesco.workorder.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.workorder.api.dto.WorkOrderCreateRequest;
import com.lmkr.hesco.workorder.api.dto.WorkOrderResponse;
import com.lmkr.hesco.workorder.api.dto.WorkOrderTransitionRequest;
import com.lmkr.hesco.workorder.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Work Order lifecycle (SRS §3.6)
 * All business logic is handled in service layer.
 * Controller is strictly API layer.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService service;

    // ===============================
    // LIST
    // ===============================
    @GetMapping
    public ApiResponse<List<WorkOrderResponse>> list() {
        return ApiResponse.ok(
                service.findAll()
                        .stream()
                        .map(WorkOrderResponse::from)
                        .toList()
        );
    }

    // ===============================
    // GET BY ID
    // ===============================
    @GetMapping("/{id}")
    public ApiResponse<WorkOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(
                WorkOrderResponse.from(service.findById(id))
        );
    }

    // ===============================
    // CREATE
    // ===============================
    @PostMapping
    public ApiResponse<WorkOrderResponse> create(
            @Valid @RequestBody WorkOrderCreateRequest request
    ) {
        return ApiResponse.ok(
                service.create(request),
                "Work Order created"
        );
    }

    // ===============================
    // TRANSITION (STATE MACHINE)
    // ===============================
    @PostMapping("/{id}/transition")
    public ApiResponse<WorkOrderResponse> transition(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderTransitionRequest request
    ) {
        return ApiResponse.ok(
                service.transition(id, request),
                "Work Order transitioned"
        );
    }
}