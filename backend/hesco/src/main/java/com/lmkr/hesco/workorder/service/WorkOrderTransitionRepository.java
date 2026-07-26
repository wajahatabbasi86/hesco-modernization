package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.workorder.entity.WorkOrderTransition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface WorkOrderTransitionRepository extends Repository<WorkOrderTransition, Integer> {

    @Query("select t from WorkOrderTransition t " +
           "where t.fromStatus.code = :fromStatusCode " +
           "and t.action.code = :actionCode " +
           "and t.role.code = :roleCode")
    Optional<WorkOrderTransition> findTransition(String fromStatusCode, String actionCode, String roleCode);
}
