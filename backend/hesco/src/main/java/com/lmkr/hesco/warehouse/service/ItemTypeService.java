package com.lmkr.hesco.warehouse.service;

import com.lmkr.hesco.warehouse.api.dto.ItemTypeRequest;
import com.lmkr.hesco.warehouse.api.dto.ItemTypeResponse;
import com.lmkr.hesco.warehouse.entity.ItemCategory;
import com.lmkr.hesco.warehouse.entity.ItemType;
import com.lmkr.hesco.warehouse.repository.ItemCategoryRepository;
import com.lmkr.hesco.warehouse.repository.ItemTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class ItemTypeService {

    private final ItemTypeRepository itemTypeRepository;
    private final ItemCategoryRepository categoryRepository;

    // READ - LIST
    @Transactional(readOnly = true)
    public List<ItemTypeResponse> findByCategory(Integer categoryId) {
        return itemTypeRepository.findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(ItemTypeResponse::from)
                .toList();
    }

    // READ - SINGLE
    @Transactional(readOnly = true)
    public ItemTypeResponse findById(Integer id) {
        return itemTypeRepository.findById(id)
                .map(ItemTypeResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Item Type not found: " + id));
    }

    // CREATE
    @Transactional
    public ItemTypeResponse create(ItemTypeRequest request) {

        ItemCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Item Category not found: " + request.categoryId()));

        ItemType itemType = ItemType.builder()
                .category(category)
                .code(request.code())
                .displayLabel(request.displayLabel())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();

        return ItemTypeResponse.from(itemTypeRepository.save(itemType));
    }

    //  UPDATE
    @Transactional
    public ItemTypeResponse update(Integer id, ItemTypeRequest request) {

        ItemType itemType = itemTypeRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Item Type not found: " + id));

        ItemCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Item Category not found: " + request.categoryId()));

        itemType.setCategory(category);
        itemType.setCode(request.code());
        itemType.setDisplayLabel(request.displayLabel());

        if (request.sortOrder() != null) {
            itemType.setSortOrder(request.sortOrder());
        }

        return ItemTypeResponse.from(itemTypeRepository.save(itemType));
    }
}