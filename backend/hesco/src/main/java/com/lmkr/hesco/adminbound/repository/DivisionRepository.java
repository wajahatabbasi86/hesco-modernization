package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DivisionRepository extends JpaRepository<Division, Long> {
    boolean existsByCode(String code);
    List<Division> findByCircleId(Long circleId);
}
