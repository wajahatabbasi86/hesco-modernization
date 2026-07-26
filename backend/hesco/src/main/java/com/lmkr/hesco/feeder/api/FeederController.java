package com.lmkr.hesco.feeder.api;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.feeder.api.dto.FeederAssignRequest;
import com.lmkr.hesco.feeder.api.dto.FeederRequest;
import com.lmkr.hesco.feeder.api.dto.FeederResponse;
import com.lmkr.hesco.feeder.api.dto.FeederUnassignRequest;
import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.service.FeederService;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feeder CRUD + assign/unassign (SRS §3.3). Assign/unassign always goes
 * through FeederService.assign/unassign so the audit-log row
 * (feeder_assignment_log, §3.3.4) is always written in the same
 * transaction as the sub_division_id change.
 */
@RestController
@RequestMapping("/api/feeders")
public class FeederController {

    private final FeederService feederService;
    private final GridStationRepository gridStationRepository;
    private final SubDivisionRepository subDivisionRepository;
    private final AppUserRepository appUserRepository;

    public FeederController(FeederService feederService, GridStationRepository gridStationRepository,
                             SubDivisionRepository subDivisionRepository, AppUserRepository appUserRepository) {
        this.feederService = feederService;
        this.gridStationRepository = gridStationRepository;
        this.subDivisionRepository = subDivisionRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public ApiResponse<List<FeederResponse>> list() {
        return ApiResponse.ok(feederService.findAll().stream().map(FeederResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<FeederResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(FeederResponse.from(feederService.findById(id)));
    }

    @PostMapping
    public ApiResponse<FeederResponse> create(@Valid @RequestBody FeederRequest request) {
        GridStation gridStation = request.gridStationId() != null
            ? gridStationRepository.findById(request.gridStationId())
                .orElseThrow(() -> new EntityNotFoundException("Grid Station not found: " + request.gridStationId()))
            : null;
        Feeder feeder = new Feeder(request.code(), request.name(), gridStation);
        return ApiResponse.ok(FeederResponse.from(feederService.create(feeder)), "Feeder created");
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<FeederResponse> assign(@PathVariable Long id, @Valid @RequestBody FeederAssignRequest request) {
        SubDivision subDivision = subDivisionRepository.findById(request.subDivisionId())
            .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + request.subDivisionId()));
        AppUser performedBy = appUserRepository.findById(request.performedByUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.performedByUserId()));
        return ApiResponse.ok(FeederResponse.from(feederService.assign(id, subDivision, performedBy)), "Feeder assigned");
    }

    @PostMapping("/{id}/unassign")
    public ApiResponse<FeederResponse> unassign(@PathVariable Long id, @Valid @RequestBody FeederUnassignRequest request) {
        AppUser performedBy = appUserRepository.findById(request.performedByUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.performedByUserId()));
        return ApiResponse.ok(FeederResponse.from(feederService.unassign(id, performedBy)), "Feeder unassigned");
    }
}
