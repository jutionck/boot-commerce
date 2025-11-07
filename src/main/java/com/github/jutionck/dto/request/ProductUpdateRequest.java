package com.github.jutionck.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    private String name;
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal compareAtPrice;
    private String category;
    private String brand;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;

    private List<String> images;
    private Map<String, Object> specifications;
}
