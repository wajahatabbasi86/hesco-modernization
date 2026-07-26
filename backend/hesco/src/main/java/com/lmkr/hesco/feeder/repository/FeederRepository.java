package com.lmkr.hesco.feeder.repository;

import com.lmkr.hesco.feeder.entity.Feeder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeederRepository extends JpaRepository<Feeder, Long> {
    Optional<Feeder> findByCode(String code);
    List<Feeder> findBySubDivisionId(Long subDivisionId);
}
