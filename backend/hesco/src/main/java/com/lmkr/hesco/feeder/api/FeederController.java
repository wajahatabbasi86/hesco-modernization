package com.lmkr.hesco.feeder.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.feeder.api.dto.*;
import com.lmkr.hesco.feeder.service.FeederService;
import com.lmkr.hesco.user.entity.AppUser;
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
        return ApiResponse.ok(service.findAll()
                .stream()
                .map(FeederResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<FeederResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(FeederResponse.from(service.findById(id)));
    }

    @PostMapping
    public ApiResponse<FeederResponse> create(@Valid @RequestBody FeederRequest request) {
        return ApiResponse.ok(
                FeederResponse.from(
                        service.create(
                                request.code(),
                                request.name(),
                                request.gridStationId()
                        )
                ),
                "Feeder created"
        );
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<FeederResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody FeederAssignRequest request
    ) {
        return ApiResponse.ok(
                FeederResponse.from(
                        service.assign(id, request.subDivisionId(), request.performedByUserId())
                ),
                "Feeder assigned"
        );
    }

    @PostMapping("/{id}/unassign")
    public ApiResponse<FeederResponse> unassign(
            @PathVariable Long id,
            @Valid @RequestBody FeederUnassignRequest request
    ) {
        return ApiResponse.ok(
                FeederResponse.from(
                        service.unassign(id, request.performedByUserId())
                ),
                "Feeder unassigned"
        );
    }
}