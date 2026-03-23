package com.smartration.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record UpdateRationRuleRequest(
        @NotBlank String stateCode,
        @NotBlank String rationCategory,
        @NotBlank String commodityName,
        @NotBlank String unit,
        @DecimalMin("0.01") double quantityPerPerson
) {
}
