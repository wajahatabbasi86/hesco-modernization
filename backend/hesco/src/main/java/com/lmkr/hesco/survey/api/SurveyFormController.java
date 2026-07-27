package com.lmkr.hesco.survey.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.survey.api.dto.SurveyFormRequest;
import com.lmkr.hesco.survey.api.dto.SurveyFormResponse;
import com.lmkr.hesco.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/survey-forms")
public class SurveyFormController {

    private final SurveyService service;

    @GetMapping
    public ApiResponse<List<SurveyFormResponse>> listByWorkOrder(@RequestParam Long workOrderId) {
        return ApiResponse.ok(service.findResponsesByWorkOrder(workOrderId));
    }

    @PostMapping("/sync")
    public ApiResponse<SurveyFormResponse> submit(
            @Valid @RequestBody SurveyFormRequest request
    ) {
        // submitForResponse (not submit()) so the saved detail row
        // (pole/conductor/transformer/meter) comes back in the response
        // instead of being silently dropped.
        return ApiResponse.ok(
                service.submitForResponse(request),
                "Survey form synced"
        );
    }
}