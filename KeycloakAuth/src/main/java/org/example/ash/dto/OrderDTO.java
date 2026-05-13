package org.example.ash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ash.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}