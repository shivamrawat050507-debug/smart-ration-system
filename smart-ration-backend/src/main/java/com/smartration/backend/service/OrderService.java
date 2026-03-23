package com.smartration.backend.service;

import com.smartration.backend.dto.OrderRequest;
import com.smartration.backend.dto.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getAllOrders();
}
