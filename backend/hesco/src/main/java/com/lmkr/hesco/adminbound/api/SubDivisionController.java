package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.SubDivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.SubDivisionResponse;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.DivisionRepository;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HESCO Admin Bound - Sub-Division level (SRS §3.1), the finest-grained
 * bound and the one most other modules scope against (feeders, users,
 * work orders all hang off a Sub-Division).
 */
@RestController
@RequestMapping("/api/admin-bound/sub-divisions")
public class SubDivisionController {

    private final SubDivisionRepository subDivisionRepository;
    private final DivisionRepository divisionRepository;
    private final AdminBoundService adminBoundService;

    public SubDivisionController(SubDivisionRepository subDivisionRepository, DivisionRepository divisionRepository,
                                  AdminBoundService adminBoundService) {
        this.subDivisionRepository = subDivisionRepository;
        this.divisionRepository = divisionRepository;
        this.adminBoundService = adminBoundService;
    }

    @GetMapping
    public ApiResponse<List<SubDivisionResponse>> list(@RequestParam(required = false) Long divisionId) {
        List<SubDivision> subDivisions = divisionId != null
            ? subDivisionRepository.findByDivisionId(divisionId)
            : subDivisionRepository.findAll();
        return ApiResponse.ok(subDivisions.stream().map(SubDivisionResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<SubDivisionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(SubDivisionResponse.from(findOrThrow(id)));
    }

    @PostMapping
    public ApiResponse<SubDivisionResponse> create(@Valid @RequestBody SubDivisionRequest request) {
        Division division = divisionRepository.findById(request.divisionId())
            .orElseThrow(() -> new EntityNotFoundException("Division not found: " + request.divisionId()));
        adminBoundService.validateNewSubDivision(request.code(), division);
        SubDivision subDivision = new SubDivision(division, request.code(), request.name());
        return ApiResponse.ok(SubDivisionResponse.from(subDivisionRepository.save(subDivision)), "Sub-Division created");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        SubDivision subDivision = findOrThrow(id);
        adminBoundService.assertDeletable(subDivision);
        subDivisionRepository.delete(subDivision);
        return ApiResponse.ok(null, "Sub-Division deleted");
    }

    private SubDivision findOrThrow(Long id) {
        return subDivisionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + id));
    }
}
