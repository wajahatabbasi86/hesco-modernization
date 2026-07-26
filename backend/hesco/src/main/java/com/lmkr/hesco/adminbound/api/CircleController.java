package com.lmkr.hesco.adminbound.api;

import com.lmkr.hesco.adminbound.api.dto.CircleRequest;
import com.lmkr.hesco.adminbound.api.dto.CircleResponse;
import com.lmkr.hesco.adminbound.service.AdminBoundService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin-bound/circles")
public class CircleController {

    private final AdminBoundService service;

    @GetMapping
    public ApiResponse<List<CircleResponse>> list() {
        return ApiResponse.ok(service.getAllCircles());
    }

    @GetMapping("/{id}")
    public ApiResponse<CircleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(CircleResponse.from(service.getCircle(id)));
    }

    @PostMapping
    public ApiResponse<CircleResponse> create(@Valid @RequestBody CircleRequest request) {
        return ApiResponse.ok(service.createCircle(request), "Circle created");
    }

    @PutMapping("/{id}")
    public ApiResponse<CircleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CircleRequest request
    ) {
        return ApiResponse.ok(service.updateCircle(id, request), "Circle updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteCircle(id);
        return ApiResponse.ok(null, "Circle deleted");
    }
}