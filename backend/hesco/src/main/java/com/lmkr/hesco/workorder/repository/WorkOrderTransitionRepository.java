package com.lmkr.hesco.workorder.repository;

import com.lmkr.hesco.workorder.entity.WorkOrderTransition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface WorkOrderTransitionRepository extends Repository<WorkOrderTransition, Integer> {

    @Query("""
        SELECT t FROM WorkOrderTransition t
        JOIN t.fromStatus fs
        JOIN t.action a
        JOIN t.role r
        WHERE fs.code = :fromStatusCode
        AND a.code = :actionCode
        AND r.code = :roleCode
    """)
    Optional<WorkOrderTransition> findTransition(
            String fromStatusCode,
            String actionCode,
            String roleCode
    );
}
