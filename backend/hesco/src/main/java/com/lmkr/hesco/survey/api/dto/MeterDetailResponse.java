package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.MeterDetail;

public record MeterDetailResponse(String meterNumber, String consumerReference) {
    public static MeterDetailResponse from(MeterDetail d) {
        return new MeterDetailResponse(d.getMeterNumber(), d.getConsumerReference());
    }
}