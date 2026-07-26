package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Integer> {
    Optional<EquipmentType> findByCode(String code);
}
