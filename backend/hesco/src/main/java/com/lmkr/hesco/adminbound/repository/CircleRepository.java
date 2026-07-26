package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.Circle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircleRepository extends JpaRepository<Circle, Long> {
    boolean existsByCode(String code);
}
