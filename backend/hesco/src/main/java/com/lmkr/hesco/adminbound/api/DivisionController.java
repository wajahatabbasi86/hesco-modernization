package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.DivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.DivisionResponse;
import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.repository.CircleRepository;
import com.lmkr.hesco.adminbound.repository.DivisionRepository;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HESCO Admin Bound - Division level (SRS §3.1). Every create runs the
 * division-code-prefix check (§3.1.1) through AdminBoundService before
 * the row is written.
 */
@RestController
@RequestMapping("/api/admin-bound/divisions")
public class DivisionController {

    private final DivisionRepository divisionRepository;
    private final CircleRepository circleRepository;
    private final AdminBoundService adminBoundService;

    public DivisionController(DivisionRepository divisionRepository, CircleRepository circleRepository,
                               AdminBoundService adminBoundService) {
        this.divisionRepository = divisionRepository;
        this.circleRepository = circleRepository;
        this.adminBoundService = adminBoundService;
    }

    @GetMapping
    public ApiResponse<List<DivisionResponse>> list(@RequestParam(required = false) Long circleId) {
        List<Division> divisions = circleId != null
            ? divisionRepository.findByCircleId(circleId)
            : divisionRepository.findAll();
        return ApiResponse.ok(divisions.stream().map(DivisionResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<DivisionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(DivisionResponse.from(findOrThrow(id)));
    }

    @PostMapping
    public ApiResponse<DivisionResponse> create(@Valid @RequestBody DivisionRequest request) {
        Circle circle = circleRepository.findById(request.circleId())
            .orElseThrow(() -> new EntityNotFoundException("Circle not found: " + request.circleId()));
        adminBoundService.validateNewDivision(request.code(), circle);
        Division division = new Division(circle, request.code(), request.name());
        return ApiResponse.ok(DivisionResponse.from(divisionRepository.save(division)), "Division created");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Division division = findOrThrow(id);
        adminBoundService.assertDeletable(division);
        divisionRepository.delete(division);
        return ApiResponse.ok(null, "Division deleted");
    }

    private Division findOrThrow(Long id) {
        return divisionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Division not found: " + id));
    }
}
