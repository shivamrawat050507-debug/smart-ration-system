package com.smartration.backend.dto;

import com.smartration.backend.entity.OrderStatus;
import com.smartration.backend.entity.PaymentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime orderDate;
    private LocalDate deliveryDate;
    private LocalTime deliveryTime;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Integer totalItems;
    private List<OrderItemResponse> items;
}
