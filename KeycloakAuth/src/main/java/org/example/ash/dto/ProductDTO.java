package org.example.ash.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private CategoryDetailDTO category;
}