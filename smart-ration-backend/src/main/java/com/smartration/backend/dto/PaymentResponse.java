package com.smartration.backend.dto;

import com.smartration.backend.entity.OrderStatus;
import com.smartration.backend.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long orderId;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String message;
}
