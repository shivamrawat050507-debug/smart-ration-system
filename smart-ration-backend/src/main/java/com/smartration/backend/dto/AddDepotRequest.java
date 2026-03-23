package com.smartration.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AddDepotRequest(
        @NotBlank String cityCode,
        @NotBlank String depotCode,
        @NotBlank String depotName,
        @NotBlank String address
) {
}
