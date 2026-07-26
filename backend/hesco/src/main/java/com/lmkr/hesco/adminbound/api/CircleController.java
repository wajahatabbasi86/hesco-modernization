package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.CircleRequest;
import com.lmkr.hesco.adminbound.api.dto.CircleResponse;
import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.repository.CircleRepository;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HESCO Admin Bound - Circle level (SRS §3.1). Only entry point for
 * circle writes; deletion goes through AdminBoundService.assertDeletable
 * so the dependent-record guard (§3.1.5) can never be bypassed.
 */
@RestController
@RequestMapping("/api/admin-bound/circles")
public class CircleController {

    private final CircleRepository circleRepository;
    private final AdminBoundService adminBoundService;

    public CircleController(CircleRepository circleRepository, AdminBoundService adminBoundService) {
        this.circleRepository = circleRepository;
        this.adminBoundService = adminBoundService;
    }

    @GetMapping
    public ApiResponse<List<CircleResponse>> list() {
        return ApiResponse.ok(circleRepository.findAll().stream().map(CircleResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<CircleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(CircleResponse.from(findOrThrow(id)));
    }

    @PostMapping
    public ApiResponse<CircleResponse> create(@Valid @RequestBody CircleRequest request) {
        Circle circle = new Circle(request.code(), request.name());
        return ApiResponse.ok(CircleResponse.from(circleRepository.save(circle)), "Circle created");
    }

    @PutMapping("/{id}")
    public ApiResponse<CircleResponse> update(@PathVariable Long id, @Valid @RequestBody CircleRequest request) {
        Circle circle = findOrThrow(id);
        circle.setCode(request.code());
        circle.setName(request.name());
        return ApiResponse.ok(CircleResponse.from(circleRepository.save(circle)), "Circle updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Circle circle = findOrThrow(id);
        adminBoundService.assertDeletable(circle);
        circleRepository.delete(circle);
        return ApiResponse.ok(null, "Circle deleted");
    }

    private Circle findOrThrow(Long id) {
        return circleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Circle not found: " + id));
    }
}
