package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.MeterDetail;
import java.math.BigDecimal;

public record MeterDetailResponse(
        String meterNumber, String consumerReference, BigDecimal sanctionedLoad, String meterMake
) {
    public static MeterDetailResponse from(MeterDetail d) {
        return new MeterDetailResponse(d.getMeterNumber(), d.getConsumerReference(), d.getSanctionedLoad(), d.getMeterMake());
    }
}
