package org.example.ash.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class AddProductRequest {
    private String name;
    private BigInteger category_id;
}
