package com.lmkr.hesco.warehouse.repository;

import com.lmkr.hesco.warehouse.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer> {
    Optional<ItemCategory> findByCode(String code);
}
