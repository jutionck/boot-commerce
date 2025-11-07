package com.github.jutionck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String category;
    private String brand;
    private Integer stock;
    private List<String> images;
    private SellerInfo seller;
    private Double rating;
    private Integer reviewCount;
    private Map<String, Object> specifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
