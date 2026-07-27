package com.lmkr.hesco.warehouse.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.warehouse.api.dto.ItemTypeRequest;
import com.lmkr.hesco.warehouse.api.dto.ItemTypeResponse;
import com.lmkr.hesco.warehouse.service.ItemTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Warehouse Configuration — item types within a category (SRS §3.5.2/§3.5.3). */
@AllArgsConstructor
@RestController
@RequestMapping("/api/warehouse/item-types")
public class ItemTypeController {

    private final ItemTypeService service;

    @GetMapping
    public ApiResponse<List<ItemTypeResponse>> listByCategory(@RequestParam Integer categoryId) {
        return ApiResponse.ok(service.findByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemTypeResponse> get(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @PostMapping
    public ApiResponse<ItemTypeResponse> create(@Valid @RequestBody ItemTypeRequest request) {
        return ApiResponse.ok(service.create(request), "Item Type created");
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemTypeResponse> update(@PathVariable Integer id, @Valid @RequestBody ItemTypeRequest request) {
        return ApiResponse.ok(service.update(id, request), "Item Type updated");
    }
}
