package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrderTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderTransitionLogRepository extends JpaRepository<WorkOrderTransitionLog, Long> {
    List<WorkOrderTransitionLog> findByWorkOrderIdOrderByPerformedAtAsc(Long workOrderId);
}
