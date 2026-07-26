package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByFeederSubDivisionId(Long subDivisionId);
    List<WorkOrder> findByFeederSubDivisionDivisionId(Long divisionId);
    List<WorkOrder> findByFeederSubDivisionDivisionCircleId(Long circleId);
}
