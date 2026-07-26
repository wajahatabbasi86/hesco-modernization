package com.lmkr.hesco.gridstation.repository;

import com.lmkr.hesco.gridstation.entity.PowerTransformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PowerTransformerRepository extends JpaRepository<PowerTransformer, Long> {
    List<PowerTransformer> findByGridStationId(Long gridStationId);
}
