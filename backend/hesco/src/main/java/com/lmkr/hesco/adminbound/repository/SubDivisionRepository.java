package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubDivisionRepository extends JpaRepository<SubDivision, Long> {
    boolean existsByCode(String code);
    List<SubDivision> findByDivisionId(Long divisionId);
}
