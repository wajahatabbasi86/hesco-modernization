package com.lmkr.hesco.feeder.repository;

import com.lmkr.hesco.feeder.entity.Feeder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeederRepository extends JpaRepository<Feeder, Long> {
    Optional<Feeder> findByCode(String code);
    List<Feeder> findBySubDivisionId(Long subDivisionId);
}
