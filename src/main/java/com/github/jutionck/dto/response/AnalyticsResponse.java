package com.github.jutionck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponse {
    private RevenueAnalytics revenue;
    private OrderAnalytics orders;
    private ProductAnalytics products;
    private CustomerAnalytics customers;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueAnalytics {
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DailyRevenue> dailyRevenue;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer orderCount;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderAnalytics {
        private Long totalOrders;
        private Long pendingOrders;
        private Long processingOrders;
        private Long shippedOrders;
        private Long deliveredOrders;
        private Long cancelledOrders;
        private Map<String, Long> ordersByStatus;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductAnalytics {
        private Long totalProducts;
        private Long lowStockProducts;
        private List<TopProduct> topSellingProducts;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private String productId;
        private String productName;
        private Long quantitySold;
        private BigDecimal totalRevenue;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerAnalytics {
        private Long totalCustomers;
        private Long newCustomersThisMonth;
        private BigDecimal averageCustomerValue;
    }
}
