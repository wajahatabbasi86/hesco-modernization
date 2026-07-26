package com.lmkr.hesco.gridstation.service;

import com.lmkr.hesco.gridstation.api.dto.PowerTransformerRequest;
import com.lmkr.hesco.gridstation.api.dto.PowerTransformerResponse;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.entity.PowerTransformer;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import com.lmkr.hesco.gridstation.repository.PowerTransformerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class PowerTransformerService {

    private final PowerTransformerRepository transformerRepository;
    private final GridStationRepository gridStationRepository;

    public List<PowerTransformerResponse> findAll(Long gridStationId) {
        List<PowerTransformer> transformers = (gridStationId != null)
                ? transformerRepository.findByGridStationId(gridStationId)
                : transformerRepository.findAll();

        return transformers.stream()
                .map(PowerTransformerResponse::from)
                .toList();
    }

    public PowerTransformerResponse create(PowerTransformerRequest request) {
        GridStation gridStation = gridStationRepository.findById(request.gridStationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Grid Station not found: " + request.gridStationId()));

        PowerTransformer transformer = PowerTransformer.builder()
                .gridStation(gridStation)
                .transformerName(request.transformerName())
                .cableSize(request.cableSize())
                .ctRatio(request.ctRatio())
                .capacityKva(request.capacityKva())
                .build();

        return PowerTransformerResponse.from(transformerRepository.save(transformer));
    }

    public void delete(Long id) {
        PowerTransformer transformer = transformerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Power Transformer not found: " + id));

        transformerRepository.delete(transformer);
    }
}