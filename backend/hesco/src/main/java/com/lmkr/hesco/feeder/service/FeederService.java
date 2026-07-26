package com.lmkr.hesco.feeder.service;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
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

    @Transactional
    public Feeder assign(Long feederId, Long subDivisionId, Long userId) {
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

        return feederRepository.save(feeder);
    }

    @Transactional
    public Feeder unassign(Long feederId, Long userId) {
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

        return feederRepository.save(feeder);
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

    public Feeder create(String code, String name, Long gridStationId) {

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

        return feederRepository.save(feeder);
    }
}
