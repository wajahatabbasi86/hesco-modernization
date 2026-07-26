package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {
    boolean existsByCode(String code);
    List<Division> findByCircleId(Long circleId);
}
