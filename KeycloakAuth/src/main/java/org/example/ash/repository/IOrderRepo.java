package org.example.ash.repository;

import org.example.ash.entity.Order;
import org.example.ash.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IOrderRepo extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
}
