package com.lmkr.hesco.warehouse.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.warehouse.api.dto.ItemCategoryRequest;
import com.lmkr.hesco.warehouse.api.dto.ItemCategoryResponse;
import com.lmkr.hesco.warehouse.service.ItemCategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Warehouse Configuration — item categories (SRS §3.5.1). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/warehouse/categories")
public class ItemCategoryController {

    private final ItemCategoryService service;

    @GetMapping
    public ApiResponse<List<ItemCategoryResponse>> list() {
        return ApiResponse.ok(service.findAll().stream().map(ItemCategoryResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemCategoryResponse> get(@PathVariable Integer id) {
        return ApiResponse.ok(ItemCategoryResponse.from(service.findById(id)));
    }

    @PostMapping
    public ApiResponse<ItemCategoryResponse> create(@Valid @RequestBody ItemCategoryRequest request) {
        return ApiResponse.ok(ItemCategoryResponse.from(service.create(request)), "Item Category created");
    }
}
