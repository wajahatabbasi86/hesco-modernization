package com.lmkr.hesco.warehouse.repository;

import com.lmkr.hesco.warehouse.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {
    List<ItemType> findByCategoryIdOrderBySortOrderAsc(Integer categoryId);
    Optional<ItemType> findByCategoryIdAndCode(Integer categoryId, String code);
}
