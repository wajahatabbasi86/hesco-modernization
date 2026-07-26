package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.SubDivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.SubDivisionResponse;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-bound/sub-divisions")
public class SubDivisionController {

    private final AdminBoundService service;

    public SubDivisionController(AdminBoundService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SubDivisionResponse>> list(
            @RequestParam(required = false) Long divisionId
    ) {
        return ApiResponse.ok(service.getSubDivisions(divisionId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubDivisionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.getSubDivision(id));
    }

    @PostMapping
    public ApiResponse<SubDivisionResponse> create(
            @Valid @RequestBody SubDivisionRequest request
    ) {
        return ApiResponse.ok(
                service.createSubDivision(
                        request.divisionId(),
                        request.code(),
                        request.name()
                ),
                "Sub-Division created"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<SubDivisionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SubDivisionRequest request
    ) {
        return ApiResponse.ok(
                service.updateSubDivision(id, request),
                "Sub-Division updated"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteSubDivision(id);
        return ApiResponse.ok(null, "Sub-Division deleted");
    }
}