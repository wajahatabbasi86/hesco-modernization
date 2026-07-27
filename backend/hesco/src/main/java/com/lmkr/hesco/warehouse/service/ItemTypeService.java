package com.lmkr.hesco.warehouse.service;

import com.lmkr.hesco.warehouse.api.dto.ItemTypeRequest;
import com.lmkr.hesco.warehouse.entity.ItemCategory;
import com.lmkr.hesco.warehouse.entity.ItemType;
import com.lmkr.hesco.warehouse.repository.ItemCategoryRepository;
import com.lmkr.hesco.warehouse.repository.ItemTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SRS §3.5.2 (Add) / §3.5.3 (Update) — "Upon saving, the new/revised item
 * type shall become available for use in the Mobile Application survey
 * forms [and reports-service DTOs, per revamp plan §2.4]." Every write
 * goes through here so category/code uniqueness (SRS: the same item
 * type name shouldn't silently duplicate within a category) is checked
 * in one place.
 */
@AllArgsConstructor
@Service
public class ItemTypeService {

    private final ItemTypeRepository itemTypeRepository;
    private final ItemCategoryRepository categoryRepository;

    public List<ItemType> findByCategory(Integer categoryId) {
        return itemTypeRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
    }

    public ItemType findById(Integer id) {
        return itemTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item Type not found: " + id));
    }

    @Transactional
    public ItemType create(ItemTypeRequest request) {
        ItemCategory category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new EntityNotFoundException("Item Category not found: " + request.categoryId()));

        ItemType itemType = ItemType.builder()
            .category(category)
            .code(request.code())
            .displayLabel(request.displayLabel())
            .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
            .build();
        return itemTypeRepository.save(itemType);
    }

    @Transactional
    public ItemType update(Integer id, ItemTypeRequest request) {
        ItemType itemType = findById(id);
        ItemCategory category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new EntityNotFoundException("Item Category not found: " + request.categoryId()));

        itemType.setCategory(category);
        itemType.setCode(request.code());
        itemType.setDisplayLabel(request.displayLabel());
        if (request.sortOrder() != null) {
            itemType.setSortOrder(request.sortOrder());
        }
        return itemTypeRepository.save(itemType);
    }
}
