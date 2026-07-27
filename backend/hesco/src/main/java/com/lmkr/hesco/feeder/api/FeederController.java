package com.lmkr.hesco.feeder.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.feeder.api.dto.FeederAssignRequest;
import com.lmkr.hesco.feeder.api.dto.FeederRequest;
import com.lmkr.hesco.feeder.api.dto.FeederResponse;
import com.lmkr.hesco.feeder.api.dto.FeederUnassignRequest;
import com.lmkr.hesco.feeder.service.FeederService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/feeders")
public class FeederController {

    private final FeederService service;

    @GetMapping
    public ApiResponse<List<FeederResponse>> list() {
        return ApiResponse.ok(service.findAllResponses());
    }

    @GetMapping("/{id}")
    public ApiResponse<FeederResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.findResponseById(id));
    }

    @PostMapping
    public ApiResponse<FeederResponse> create(@Valid @RequestBody FeederRequest request) {
        return ApiResponse.ok(
                service.create(request.code(), request.name(), request.gridStationId()),
                "Feeder created"
        );
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<FeederResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody FeederAssignRequest request
    ) {
        return ApiResponse.ok(
                service.assign(id, request.subDivisionId(), request.performedByUserId()),
                "Feeder assigned"
        );
    }

    @PostMapping("/{id}/unassign")
    public ApiResponse<FeederResponse> unassign(
            @PathVariable Long id,
            @Valid @RequestBody FeederUnassignRequest request
    ) {
        return ApiResponse.ok(
                service.unassign(id, request.performedByUserId()),
                "Feeder unassigned"
        );
    }
}