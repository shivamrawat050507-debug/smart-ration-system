package com.smartration.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotNull(message = "Rice quantity is required")
    @Min(value = 0, message = "Rice quantity cannot be negative")
    private Integer rice;

    @NotNull(message = "Wheat quantity is required")
    @Min(value = 0, message = "Wheat quantity cannot be negative")
    private Integer wheat;

    @NotNull(message = "Sugar quantity is required")
    @Min(value = 0, message = "Sugar quantity cannot be negative")
    private Integer sugar;
}
