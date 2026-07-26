package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.DivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.DivisionResponse;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-bound/divisions")
public class DivisionController {

    private final AdminBoundService service;

    public DivisionController(AdminBoundService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<DivisionResponse>> list(
            @RequestParam(required = false) Long circleId
    ) {
        return ApiResponse.ok(service.getDivisions(circleId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DivisionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.getDivision(id));
    }

    @PostMapping
    public ApiResponse<DivisionResponse> create(
            @Valid @RequestBody DivisionRequest request
    ) {
        return ApiResponse.ok(
                service.createDivision(request.circleId(), request.code(), request.name()),
                "Division created"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<DivisionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DivisionRequest request
    ) {
        return ApiResponse.ok(
                service.updateDivision(id, request),
                "Division updated"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteDivision(id);
        return ApiResponse.ok(null, "Division deleted");
    }
}