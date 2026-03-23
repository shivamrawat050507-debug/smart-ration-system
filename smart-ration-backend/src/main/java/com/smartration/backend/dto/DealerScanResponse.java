package com.smartration.backend.dto;

import java.util.List;

public record DealerScanResponse(
        String dealerName,
        String depotCode,
        String depotName,
        String qrCodeValue,
        String beneficiaryName,
        String cardNumber,
        int familyMembers,
        String rationCategory,
        boolean eligible,
        boolean canDistribute,
        String status,
        String statusTone,
        String headline,
        String supportingText,
        String reason,
        String warningMessage,
        String pendingMessage,
        List<InstructionLine> instructions,
        DashboardOverviewResponse.ReceiptPreview receiptPreview
) {
    public record InstructionLine(
            String commodity,
            String primaryLabel,
            double giveNowQuantity,
            double pendingQuantity,
            String unit
    ) {
    }
}
