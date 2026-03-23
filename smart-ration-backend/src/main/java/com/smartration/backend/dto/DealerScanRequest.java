package com.smartration.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DealerScanRequest(
        @NotBlank(message = "QR code is required")
        String qrCodeValue
) {
}
