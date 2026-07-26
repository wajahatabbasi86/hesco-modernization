package com.lmkr.hesco.gridstation.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.gridstation.api.dto.GridStationRequest;
import com.lmkr.hesco.gridstation.api.dto.GridStationResponse;
import com.lmkr.hesco.gridstation.service.GridStationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/grid-stations")
public class GridStationController {

    private final GridStationService service;

    @GetMapping
    public ApiResponse<List<GridStationResponse>> list() {
        return ApiResponse.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<GridStationResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.findById(id));
    }

    @PostMapping
    public ApiResponse<GridStationResponse> create(
            @Valid @RequestBody GridStationRequest request
    ) {
        return ApiResponse.ok(service.create(request), "Grid Station created");
    }

    @PutMapping("/{id}")
    public ApiResponse<GridStationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GridStationRequest request
    ) {
        return ApiResponse.ok(service.update(id, request), "Grid Station updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null, "Grid Station deleted");
    }
}