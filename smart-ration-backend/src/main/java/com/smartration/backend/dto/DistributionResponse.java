package com.smartration.backend.dto;

import java.util.List;

public record DistributionResponse(
        boolean success,
        String message,
        String distributionMode,
        String smsStatus,
        List<DashboardOverviewResponse.DistributionItem> items,
        DashboardOverviewResponse.ReceiptPreview receipt
) {
}
