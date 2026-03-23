package com.smartration.backend.service.impl;

import com.smartration.backend.dto.OrderItemRequest;
import com.smartration.backend.dto.OrderItemResponse;
import com.smartration.backend.dto.OrderRequest;
import com.smartration.backend.dto.OrderResponse;
import com.smartration.backend.entity.Order;
import com.smartration.backend.entity.OrderItem;
import com.smartration.backend.entity.OrderStatus;
import com.smartration.backend.entity.PaymentStatus;
import com.smartration.backend.entity.Product;
import com.smartration.backend.entity.User;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.OrderRepository;
import com.smartration.backend.repository.ProductRepository;
import com.smartration.backend.repository.UserRepository;
import com.smartration.backend.service.OrderService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (request.getDeliveryDate().equals(java.time.LocalDate.now())
                && request.getDeliveryTime().isBefore(LocalTime.now())) {
            throw new BadRequestException("Delivery time cannot be in the past");
        }

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .deliveryDate(request.getDeliveryDate())
                .deliveryTime(request.getDeliveryTime())
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .totalItems(request.getItems().stream().mapToInt(OrderItemRequest::getQuantity).sum())
                .build();

        List<OrderItem> items = request.getItems()
                .stream()
                .map(itemRequest -> buildOrderItem(itemRequest, order))
                .toList();

        order.setItems(items);

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderItem buildOrderItem(OrderItemRequest request, Order order) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for " + product.getName());
        }

        return OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .unit(product.getUnit())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .deliveryTime(order.getDeliveryTime())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalItems(order.getTotalItems())
                .items(order.getItems().stream().map(this::mapToItemResponse).toList())
                .build();
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .build();
    }
}
