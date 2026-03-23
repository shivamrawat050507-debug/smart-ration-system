package com.smartration.backend.repository;

import com.smartration.backend.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findAllByOrderByOrderDateDesc();
}
