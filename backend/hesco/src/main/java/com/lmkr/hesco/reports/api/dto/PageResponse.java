package com.lmkr.hesco.reports.api.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long total
) {}
