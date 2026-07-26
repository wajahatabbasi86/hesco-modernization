package com.lmkr.hesco.gridstation.repository;

import com.lmkr.hesco.gridstation.entity.PowerTransformer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PowerTransformerRepository extends JpaRepository<PowerTransformer, Long> {
    List<PowerTransformer> findByGridStationId(Long gridStationId);
}
