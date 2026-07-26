package com.lmkr.hesco.gridstation.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.gridstation.api.dto.GridStationRequest;
import com.lmkr.hesco.gridstation.api.dto.GridStationResponse;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Grid Station CRUD (SRS §3.4). Power Transformer sub-entity CRUD lives
 * in PowerTransformerController.
 */
@RestController
@RequestMapping("/api/grid-stations")
public class GridStationController {

    private final GridStationRepository gridStationRepository;

    public GridStationController(GridStationRepository gridStationRepository) {
        this.gridStationRepository = gridStationRepository;
    }

    @GetMapping
    public ApiResponse<List<GridStationResponse>> list() {
        return ApiResponse.ok(gridStationRepository.findAll().stream().map(GridStationResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<GridStationResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(GridStationResponse.from(findOrThrow(id)));
    }

    @PostMapping
    public ApiResponse<GridStationResponse> create(@Valid @RequestBody GridStationRequest request) {
        GridStation gridStation = new GridStation(request.code(), request.name(), request.latitude(), request.longitude());
        return ApiResponse.ok(GridStationResponse.from(gridStationRepository.save(gridStation)), "Grid Station created");
    }

    @PutMapping("/{id}")
    public ApiResponse<GridStationResponse> update(@PathVariable Long id, @Valid @RequestBody GridStationRequest request) {
        GridStation gridStation = findOrThrow(id);
        gridStation.setCode(request.code());
        gridStation.setName(request.name());
        gridStation.setLatitude(request.latitude());
        gridStation.setLongitude(request.longitude());
        return ApiResponse.ok(GridStationResponse.from(gridStationRepository.save(gridStation)), "Grid Station updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        gridStationRepository.delete(findOrThrow(id));
        return ApiResponse.ok(null, "Grid Station deleted");
    }

    private GridStation findOrThrow(Long id) {
        return gridStationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Grid Station not found: " + id));
    }
}
