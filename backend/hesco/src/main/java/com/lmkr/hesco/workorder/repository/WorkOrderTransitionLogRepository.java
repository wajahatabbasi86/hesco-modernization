package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrderTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderTransitionLogRepository extends JpaRepository<WorkOrderTransitionLog, Long> {
    List<WorkOrderTransitionLog> findByWorkOrderIdOrderByPerformedAtAsc(Long workOrderId);
}
