package com.lmkr.hesco.survey.api.dto;

/**
 * SRS §8.3.6 — shown when Equipment Type is Meter. Kept minimal
 * (meterNumber, consumerReference) pending a full §8.3.6 field-list
 * review, matching the MeterDetail entity's own scope note.
 */
public record MeterDetailRequest(
        String meterNumber,
        String consumerReference
) {}