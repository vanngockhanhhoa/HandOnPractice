package org.example.ash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ash.dto.OrderDTO;
import org.example.ash.dto.request.OrderRequest;
import org.example.ash.entity.Order;
import org.example.ash.entity.OrderStatus;
import org.example.ash.exception.AppException;
import org.example.ash.kafka.event.OrderCreatedEvent;
import org.example.ash.kafka.producer.OrderEventProducer;
import org.example.ash.mapper.OrderMapper;
import org.example.ash.repository.IOrderRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final IOrderRepo       orderRepo;
    private final OrderMapper      orderMapper;
    private final OrderEventProducer eventProducer;

    @Transactional
    public OrderDTO createOrder(OrderRequest request) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .build();

        Order saved = orderRepo.save(order);
        log.info("Order created: id={} number={}", saved.getId(), saved.getOrderNumber());

        // Publish event — other services consume from topic "order.created"
        eventProducer.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .userId(saved.getUserId())
                .totalAmount(saved.getTotalAmount())
                .status(saved.getStatus().name())
                .note(saved.getNote())
                .createdAt(saved.getCreatedAt())
                .build());

        return orderMapper.toDto(saved);
    }

    public OrderDTO getById(Long id) {
        return orderMapper.toDto(
                orderRepo.findById(id)
                        .orElseThrow(() -> new AppException("Order not found: " + id)
                                .status(HttpStatus.NOT_FOUND.value()))
        );
    }

    public List<OrderDTO> getByUserId(Long userId) {
        return orderMapper.toListDto(orderRepo.findByUserId(userId));
    }

    public List<OrderDTO> getAll() {
        return orderMapper.toListDto(orderRepo.findAll());
    }

    @Transactional
    public OrderDTO updateStatus(Long id, OrderStatus status) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new AppException("Order not found: " + id)
                        .status(HttpStatus.NOT_FOUND.value()));
        order.setStatus(status);
        return orderMapper.toDto(orderRepo.save(order));
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + (int) (Math.random() * 9000 + 1000);
    }
}
