package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkOrderStatusRepository extends JpaRepository<WorkOrderStatus, Short> {
    Optional<WorkOrderStatus> findByCode(String code);
}
