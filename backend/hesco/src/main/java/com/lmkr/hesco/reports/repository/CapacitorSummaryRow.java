package com.lmkr.hesco.reports.repository;

/**
 * Internal flat row used only to build FeederDeviceReportRow's
 * capacitorBanks group — one (feeder, capacitor capacity item_type)
 * tuple, zero-filled the same way as the transformer capacity rows.
 * Not exposed via the API.
 */
public record CapacitorSummaryRow(
        Long feederId,
        String feederCode,
        String feederName,
        String substationName,
        String itemCode,
        String itemLabel,
        long count
) {}
