package com.lmkr.hesco.gridstation.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.gridstation.api.dto.PowerTransformerRequest;
import com.lmkr.hesco.gridstation.api.dto.PowerTransformerResponse;
import com.lmkr.hesco.gridstation.service.PowerTransformerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/power-transformers")
public class PowerTransformerController {

    private final PowerTransformerService service;

    @GetMapping
    public ApiResponse<List<PowerTransformerResponse>> list(
            @RequestParam(required = false) Long gridStationId
    ) {
        return ApiResponse.ok(service.findAll(gridStationId));
    }

    @PostMapping
    public ApiResponse<PowerTransformerResponse> create(
            @Valid @RequestBody PowerTransformerRequest request
    ) {
        return ApiResponse.ok(service.create(request), "Power Transformer created");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null, "Power Transformer deleted");
    }
}