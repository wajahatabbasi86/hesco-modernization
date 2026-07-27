package com.lmkr.hesco.warehouse.service;

import com.lmkr.hesco.warehouse.api.dto.ItemCategoryRequest;
import com.lmkr.hesco.warehouse.entity.ItemCategory;
import com.lmkr.hesco.warehouse.repository.ItemCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SRS §3.5.1 — "The system shall display a list of all configured
 * warehouse item categories along with their associated sub-types."
 * Categories themselves (TRANSFORMER_CAPACITY, HT_CONDUCTOR, etc.) are
 * effectively fixed by the SRS's enumerated lists (§3.15.2) — this
 * service mainly supports listing/lookup; adding a wholly new category
 * outside the SRS's list is possible but unusual (see ItemTypeService
 * for the day-to-day "add/update item type" operations SRS §3.5.2/3.5.3
 * actually describes).
 */
@AllArgsConstructor
@Service
public class ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;

    public List<ItemCategory> findAll() {
        return categoryRepository.findAll();
    }

    public ItemCategory findById(Integer id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item Category not found: " + id));
    }

    @Transactional
    public ItemCategory create(ItemCategoryRequest request) {
        ItemCategory category = ItemCategory.builder()
            .code(request.code())
            .name(request.name())
            .build();
        return categoryRepository.save(category);
    }
}
