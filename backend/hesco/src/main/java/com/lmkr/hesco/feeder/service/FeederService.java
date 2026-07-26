package com.lmkr.hesco.feeder.service;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.entity.FeederAssignmentLog;
import com.lmkr.hesco.feeder.repository.FeederAssignmentLogRepository;
import com.lmkr.hesco.feeder.repository.FeederRepository;
import com.lmkr.hesco.user.entity.AppUser;
import jakarta.persistence.EntityNotFoundException;
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
@Service
public class FeederService {

    private final FeederRepository feederRepository;
    private final FeederAssignmentLogRepository assignmentLogRepository;

    public FeederService(FeederRepository feederRepository,
                          FeederAssignmentLogRepository assignmentLogRepository) {
        this.feederRepository = feederRepository;
        this.assignmentLogRepository = assignmentLogRepository;
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

    @Transactional
    public Feeder assign(Long feederId, SubDivision subDivision, AppUser performedBy) {
        Feeder feeder = findById(feederId);
        feeder.setSubDivision(subDivision);
        Feeder saved = feederRepository.save(feeder);
        assignmentLogRepository.save(
            new FeederAssignmentLog(saved, subDivision, FeederAssignmentLog.Action.ASSIGN, performedBy));
        return saved;
    }

    @Transactional
    public Feeder unassign(Long feederId, AppUser performedBy) {
        Feeder feeder = findById(feederId);
        SubDivision previous = feeder.getSubDivision();
        assignmentLogRepository.save(
            new FeederAssignmentLog(feeder, previous, FeederAssignmentLog.Action.UNASSIGN, performedBy));
        feeder.setSubDivision(null);
        return feederRepository.save(feeder);
    }
}
