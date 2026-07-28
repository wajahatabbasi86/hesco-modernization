package com.lmkr.hesco.survey.api.dto;

import java.math.BigDecimal;

/**
 * SRS §8.3.6 — shown when Equipment Type is Meter. If Reference No./
 * Meter No. already exist in the system all fields are meant to
 * auto-populate per the SRS — that lookup-by-existing-meter behavior
 * isn't implemented yet (still an open item), so today every submission
 * is treated as a new meter and all four fields are taken as given.
 */
public record MeterDetailRequest(
        String meterNumber,
        String consumerReference,
        BigDecimal sanctionedLoad,
        String meterMake
) {}
