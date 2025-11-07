package com.github.jutionck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jutionck.enums.VoucherType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer userUsageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private SellerInfo seller;
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
