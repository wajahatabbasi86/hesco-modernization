package com.lmkr.hesco.feeder.service;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import com.lmkr.hesco.feeder.api.dto.FeederResponse;
import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.entity.FeederAssignmentLog;
import com.lmkr.hesco.feeder.repository.FeederAssignmentLogRepository;
import com.lmkr.hesco.feeder.repository.FeederRepository;
import com.lmkr.hesco.gridstation.entity.GridStation;
import com.lmkr.hesco.gridstation.repository.GridStationRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Owns Feeder assign/unassign (SRS §3.3.4). Writing feeder.sub_division_id
 * and appending the audit-log row (feeder_assignment_log) happen together
 * in one @Transactional method — this used to be implicit in the DB
 * (a trigger could have done both), it's now an explicit application
 * transaction boundary instead.
 */
@AllArgsConstructor
@Service
public class FeederService {

    private final FeederRepository feederRepository;
    private final FeederAssignmentLogRepository assignmentLogRepository;
    private final GridStationRepository gridStationRepository;
    private final AppUserRepository appUserRepository;
    private final SubDivisionRepository subDivisionRepository;

    /**
     * Returns DTOs, mapped WHILE the session is open (@Transactional on
     * this method), since Feeder.gridStation/subDivision are both
     * FetchType.LAZY and open-in-view is disabled - same
     * LazyInitializationException risk as UserService.findAll() had,
     * fixed the same way: map inside the transaction, not in the
     * controller after the (previously untransactional) call returned.
     */
    @Transactional(readOnly = true)
    public List<FeederResponse> findAllResponses() {
        return feederRepository.findAll().stream().map(FeederResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FeederResponse findResponseById(Long id) {
        return FeederResponse.from(findById(id));
    }

    public Feeder findById(Long id) {
        return feederRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feeder not found: " + id));
    }

    public List<Feeder> findAll() {
        return feederRepository.findAll();
    }

    public Feeder create(Feeder feeder) {
        return feederRepository.save(feeder);
    }

    /**
     * Returns FeederResponse, not Feeder - mapped HERE, inside the
     * transaction. Previously returned the raw Feeder entity and the
     * controller called FeederResponse.from(...) afterward; that broke
     * because assign() only ever touches/sets subDivision, so
     * feeder.gridStation stays an untouched lazy proxy - fine while the
     * session is open, but FeederResponse.from() reads
     * feeder.getGridStation() too, and by the time the controller got to
     * it the session had already closed (open-in-view is disabled),
     * throwing LazyInitializationException on GridStation specifically
     * (not SubDivision, which HAD been touched/replaced with a real
     * object and was therefore safe).
     */
    @Transactional
    public FeederResponse assign(Long feederId, Long subDivisionId, Long userId) {
        Feeder feeder = findById(feederId);
        SubDivision subDivision = getSubDivision(subDivisionId);
        AppUser user = getUser(userId);

        feeder.setSubDivision(subDivision);

        FeederAssignmentLog log = FeederAssignmentLog.builder()
                .feeder(feeder)
                .subDivision(subDivision)
                .action(FeederAssignmentLog.Action.ASSIGN)
                .performedBy(user)
                .build();

        assignmentLogRepository.save(log);

        return FeederResponse.from(feederRepository.save(feeder));
    }

    @Transactional
    public FeederResponse unassign(Long feederId, Long userId) {
        Feeder feeder = findById(feederId);
        AppUser user = getUser(userId);

        FeederAssignmentLog log = FeederAssignmentLog.builder()
                .feeder(feeder)
                .subDivision(feeder.getSubDivision())
                .action(FeederAssignmentLog.Action.UNASSIGN)
                .performedBy(user)
                .build();

        assignmentLogRepository.save(log);

        feeder.setSubDivision(null);

        return FeederResponse.from(feederRepository.save(feeder));
    }

    public GridStation getGridStation(Long id) {
        return gridStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grid Station not found: " + id));
    }

    public SubDivision getSubDivision(Long id) {
        return subDivisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + id));
    }

    public AppUser getUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional
    public FeederResponse create(String code, String name, Long gridStationId) {

        GridStation gridStation = null;

        if (gridStationId != null) {
            gridStation = gridStationRepository.findById(gridStationId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Grid Station not found: " + gridStationId));
        }

        Feeder feeder = Feeder.builder()
                .code(code)
                .name(name)
                .gridStation(gridStation)
                .build();

        return FeederResponse.from(feederRepository.save(feeder));
    }
}