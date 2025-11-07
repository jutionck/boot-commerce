package com.github.jutionck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jutionck.entity.Address;
import com.github.jutionck.enums.OrderStatus;
import com.github.jutionck.enums.PaymentMethod;
import com.github.jutionck.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private CustomerInfo customer;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shipping;
    private BigDecimal tax;
    private BigDecimal total;
    private Address shippingAddress;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String voucherCode;
    private String referralCode;
    private String notes;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private UUID id;
        private ProductInfo product;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInfo {
        private UUID id;
        private String name;
        private String category;
        private String brand;
    }
}
