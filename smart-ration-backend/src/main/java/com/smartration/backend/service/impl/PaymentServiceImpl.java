package com.smartration.backend.service.impl;

import com.smartration.backend.dto.PaymentResponse;
import com.smartration.backend.entity.Order;
import com.smartration.backend.entity.OrderItem;
import com.smartration.backend.entity.OrderStatus;
import com.smartration.backend.entity.PaymentStatus;
import com.smartration.backend.entity.Product;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.OrderRepository;
import com.smartration.backend.repository.ProductRepository;
import com.smartration.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public PaymentResponse payForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already completed for order: " + orderId);
        }

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return PaymentResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .message("Payment successful and order confirmed")
                .build();
    }
}
