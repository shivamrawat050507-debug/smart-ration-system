package com.smartration.backend.service;

import com.smartration.backend.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse payForOrder(Long orderId);
}
