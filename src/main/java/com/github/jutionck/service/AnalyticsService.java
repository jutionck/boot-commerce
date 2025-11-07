package com.github.jutionck.service;

import com.github.jutionck.dto.response.AnalyticsResponse;
import com.github.jutionck.entity.Order;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.OrderStatus;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.repository.OrderItemRepository;
import com.github.jutionck.repository.OrderRepository;
import com.github.jutionck.repository.ProductRepository;
import com.github.jutionck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AnalyticsResponse.RevenueAnalytics getRevenueAnalytics(
            User user,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders;
        if (user.getRole() == UserRole.ADMIN) {
            orders = orderRepository.findOrdersBetweenDates(start, end);
        } else {
            orders = orderRepository.findSellerOrdersBetweenDates(user.getId(), start, end);
        }

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long deliveredOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        BigDecimal averageOrderValue = deliveredOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate daily revenue
        List<AnalyticsResponse.DailyRevenue> dailyRevenue = calculateDailyRevenue(orders, startDate, endDate);

        return AnalyticsResponse.RevenueAnalytics.builder()
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .startDate(startDate)
                .endDate(endDate)
                .dailyRevenue(dailyRevenue)
                .build();
    }

    public AnalyticsResponse.OrderAnalytics getOrderAnalytics(User user) {
        Map<String, Long> ordersByStatus = new HashMap<>();

        if (user.getRole() == UserRole.ADMIN) {
            for (OrderStatus status : OrderStatus.values()) {
                long count = orderRepository.count();
                ordersByStatus.put(status.name(), count);
            }
        } else {
            for (OrderStatus status : OrderStatus.values()) {
                long count = orderRepository.countBySellerIdAndStatus(user.getId(), status);
                ordersByStatus.put(status.name(), count);
            }
        }

        return AnalyticsResponse.OrderAnalytics.builder()
                .totalOrders(ordersByStatus.values().stream().mapToLong(Long::longValue).sum())
                .pendingOrders(ordersByStatus.getOrDefault(OrderStatus.PENDING.name(), 0L))
                .processingOrders(ordersByStatus.getOrDefault(OrderStatus.PROCESSING.name(), 0L))
                .shippedOrders(ordersByStatus.getOrDefault(OrderStatus.SHIPPED.name(), 0L))
                .deliveredOrders(ordersByStatus.getOrDefault(OrderStatus.DELIVERED.name(), 0L))
                .cancelledOrders(ordersByStatus.getOrDefault(OrderStatus.CANCELLED.name(), 0L))
                .ordersByStatus(ordersByStatus)
                .build();
    }

    public AnalyticsResponse.ProductAnalytics getProductAnalytics(User user, LocalDate startDate, LocalDate endDate) {
        long totalProducts;
        long lowStockProducts;

        if (user.getRole() == UserRole.ADMIN) {
            totalProducts = productRepository.count();
            lowStockProducts = productRepository.findAll().stream()
                    .filter(p -> p.getStock() <= 10)
                    .count();
        } else {
            totalProducts = productRepository.findBySellerId(user.getId(), org.springframework.data.domain.Pageable.unpaged())
                    .getTotalElements();
            lowStockProducts = productRepository.findLowStockProducts(user.getId(), 10).size();
        }

        // Get top selling products
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<Object[]> topProducts = orderItemRepository.findTopSellingProducts(user.getId(), start, end);

        List<AnalyticsResponse.TopProduct> topSellingProducts = new ArrayList<>();
        for (Object[] row : topProducts) {
            if (topSellingProducts.size() >= 10) break; // Top 10
            topSellingProducts.add(AnalyticsResponse.TopProduct.builder()
                    .productId(row[0].toString())
                    .productName((String) row[1])
                    .quantitySold((Long) row[2])
                    .totalRevenue((BigDecimal) row[3])
                    .build());
        }

        return AnalyticsResponse.ProductAnalytics.builder()
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .topSellingProducts(topSellingProducts)
                .build();
    }

    public AnalyticsResponse.CustomerAnalytics getCustomerAnalytics(User user) {
        long totalCustomers;

        if (user.getRole() == UserRole.ADMIN) {
            totalCustomers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.CUSTOMER)
                    .count();
        } else {
            totalCustomers = userRepository.findCustomersBySellerId(
                    user.getId(),
                    org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements();
        }

        // New customers this month
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime monthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        long newCustomersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .filter(u -> u.getCreatedAt().isAfter(monthStart) && u.getCreatedAt().isBefore(monthEnd))
                .count();

        // Average customer value (placeholder - would need order history per customer)
        BigDecimal averageCustomerValue = BigDecimal.ZERO;

        return AnalyticsResponse.CustomerAnalytics.builder()
                .totalCustomers(totalCustomers)
                .newCustomersThisMonth(newCustomersThisMonth)
                .averageCustomerValue(averageCustomerValue)
                .build();
    }

    private List<AnalyticsResponse.DailyRevenue> calculateDailyRevenue(
            List<Order> orders,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<LocalDate, DailyData> dailyMap = new HashMap<>();

        // Initialize all dates in range
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyMap.put(current, new DailyData());
            current = current.plusDays(1);
        }

        // Aggregate data
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                    DailyData data = dailyMap.get(orderDate);
                    data.revenue = data.revenue.add(order.getTotal());
                    data.orderCount++;
                }
            }
        }

        // Convert to list
        List<AnalyticsResponse.DailyRevenue> result = new ArrayList<>();
        current = startDate;
        while (!current.isAfter(endDate)) {
            DailyData data = dailyMap.get(current);
            result.add(AnalyticsResponse.DailyRevenue.builder()
                    .date(current)
                    .revenue(data.revenue)
                    .orderCount(data.orderCount)
                    .build());
            current = current.plusDays(1);
        }

        return result;
    }

    private static class DailyData {
        BigDecimal revenue = BigDecimal.ZERO;
        Integer orderCount = 0;
    }
}
