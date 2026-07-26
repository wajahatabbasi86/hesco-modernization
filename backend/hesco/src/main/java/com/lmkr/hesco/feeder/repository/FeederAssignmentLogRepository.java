package com.lmkr.hesco.feeder.repository;

import com.lmkr.hesco.feeder.entity.FeederAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeederAssignmentLogRepository extends JpaRepository<FeederAssignmentLog, Long> {
    List<FeederAssignmentLog> findByFeederIdOrderByPerformedAtDesc(Long feederId);
}
