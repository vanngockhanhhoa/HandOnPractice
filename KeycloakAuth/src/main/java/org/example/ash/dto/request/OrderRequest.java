package org.example.ash.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private Long userId;
    private BigDecimal totalAmount;
    private String note;
}