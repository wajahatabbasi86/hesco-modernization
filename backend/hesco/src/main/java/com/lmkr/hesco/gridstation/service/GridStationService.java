package com.lmkr.hesco.gridstation.service;

import com.lmkr.hesco.gridstation.api.dto.GridStationRequest;
import com.lmkr.hesco.gridstation.api.dto.GridStationResponse;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class GridStationService {

    private final GridStationRepository repository;

    @Transactional(readOnly = true)
    public List<GridStationResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(GridStationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GridStationResponse findById(Long id) {
        return GridStationResponse.from(getEntity(id));
    }

    @Transactional
    public GridStationResponse create(GridStationRequest request) {
        GridStation gridStation = GridStation.builder()
                .code(request.code())
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return GridStationResponse.from(repository.save(gridStation));
    }

    @Transactional
    public GridStationResponse update(Long id, GridStationRequest request) {
        GridStation gridStation = getEntity(id);

        gridStation.setCode(request.code());
        gridStation.setName(request.name());
        gridStation.setLatitude(request.latitude());
        gridStation.setLongitude(request.longitude());

        return GridStationResponse.from(repository.save(gridStation));
    }

    @Transactional
    public void delete(Long id) {
        GridStation gridStation = getEntity(id);
        repository.delete(gridStation);
    }

    private GridStation getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grid Station not found: " + id));
    }
}