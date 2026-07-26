package com.lmkr.hesco.feeder.repository;

import com.lmkr.hesco.feeder.entity.FeederAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeederAssignmentLogRepository extends JpaRepository<FeederAssignmentLog, Long> {
    List<FeederAssignmentLog> findByFeederIdOrderByPerformedAtDesc(Long feederId);
}
