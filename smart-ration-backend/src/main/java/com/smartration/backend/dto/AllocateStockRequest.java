package com.smartration.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record AllocateStockRequest(
        @NotBlank String depotCode,
        @NotBlank String commodity,
        @DecimalMin("0.01") double quantity
) {
}
