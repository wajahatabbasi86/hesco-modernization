package com.lmkr.hesco.gridstation.repository;

import com.lmkr.hesco.gridstation.entity.GridStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GridStationRepository extends JpaRepository<GridStation, Long> {
    boolean existsByCode(String code);
}
