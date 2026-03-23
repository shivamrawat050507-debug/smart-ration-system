package com.smartration.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    private LocalDate deliveryDate;

    @NotNull(message = "Delivery time is required")
    private LocalTime deliveryTime;

    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<OrderItemRequest> items;
}
