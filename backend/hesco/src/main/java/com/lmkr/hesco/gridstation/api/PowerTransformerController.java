package com.lmkr.hesco.gridstation.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.gridstation.api.dto.PowerTransformerRequest;
import com.lmkr.hesco.gridstation.api.dto.PowerTransformerResponse;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.entity.PowerTransformer;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import com.lmkr.hesco.gridstation.repository.PowerTransformerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Power Transformer sub-entity of Grid Station (SRS §2.3 in the revamp
 * plan): transformer_name, cable_size, ct_ratio, capacity_kva.
 */
@RestController
@RequestMapping("/api/power-transformers")
public class PowerTransformerController {

    private final PowerTransformerRepository powerTransformerRepository;
    private final GridStationRepository gridStationRepository;

    public PowerTransformerController(PowerTransformerRepository powerTransformerRepository,
                                       GridStationRepository gridStationRepository) {
        this.powerTransformerRepository = powerTransformerRepository;
        this.gridStationRepository = gridStationRepository;
    }

    @GetMapping
    public ApiResponse<List<PowerTransformerResponse>> list(@RequestParam(required = false) Long gridStationId) {
        List<PowerTransformer> transformers = gridStationId != null
            ? powerTransformerRepository.findByGridStationId(gridStationId)
            : powerTransformerRepository.findAll();
        return ApiResponse.ok(transformers.stream().map(PowerTransformerResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<PowerTransformerResponse> create(@Valid @RequestBody PowerTransformerRequest request) {
        GridStation gridStation = gridStationRepository.findById(request.gridStationId())
            .orElseThrow(() -> new EntityNotFoundException("Grid Station not found: " + request.gridStationId()));
        PowerTransformer transformer = new PowerTransformer(gridStation, request.transformerName(),
            request.cableSize(), request.ctRatio(), request.capacityKva());
        return ApiResponse.ok(PowerTransformerResponse.from(powerTransformerRepository.save(transformer)), "Power Transformer created");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        PowerTransformer transformer = powerTransformerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Power Transformer not found: " + id));
        powerTransformerRepository.delete(transformer);
        return ApiResponse.ok(null, "Power Transformer deleted");
    }
}
