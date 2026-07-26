package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubDivisionRepository extends JpaRepository<SubDivision, Long> {
    boolean existsByCode(String code);
    List<SubDivision> findByDivisionId(Long divisionId);
}
