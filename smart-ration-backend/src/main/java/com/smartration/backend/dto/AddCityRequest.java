package com.smartration.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddCityRequest(
        @NotBlank String stateCode,
        @NotBlank String cityCode,
        @NotBlank String cityName,
        @Min(1) int population
) {
}
