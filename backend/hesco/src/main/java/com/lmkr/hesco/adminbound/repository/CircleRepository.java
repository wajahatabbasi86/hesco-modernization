package com.lmkr.hesco.adminbound.repository;

import com.lmkr.hesco.adminbound.entity.Circle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CircleRepository extends JpaRepository<Circle, Long> {
    boolean existsByCode(String code);
}
