package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkOrderStatusRepository extends JpaRepository<WorkOrderStatus, Short> {
    Optional<WorkOrderStatus> findByCode(String code);
}
