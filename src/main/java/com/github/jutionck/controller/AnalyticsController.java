package com.github.jutionck.controller;

import com.github.jutionck.dto.response.AnalyticsResponse;
import com.github.jutionck.entity.User;
import com.github.jutionck.service.AnalyticsService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getRevenueAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        AnalyticsResponse.RevenueAnalytics analytics = analyticsService.getRevenueAnalytics(user, startDate, endDate);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Revenue analytics retrieved successfully",
                analytics
        );
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getOrderAnalytics(@AuthenticationPrincipal User user) {
        AnalyticsResponse.OrderAnalytics analytics = analyticsService.getOrderAnalytics(user);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Order analytics retrieved successfully",
                analytics
        );
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getProductAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        AnalyticsResponse.ProductAnalytics analytics = analyticsService.getProductAnalytics(user, startDate, endDate);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Product analytics retrieved successfully",
                analytics
        );
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getCustomerAnalytics(@AuthenticationPrincipal User user) {
        AnalyticsResponse.CustomerAnalytics analytics = analyticsService.getCustomerAnalytics(user);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Customer analytics retrieved successfully",
                analytics
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getDashboardAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get all analytics
        AnalyticsResponse analytics = AnalyticsResponse.builder()
                .revenue(analyticsService.getRevenueAnalytics(user, startDate, endDate))
                .orders(analyticsService.getOrderAnalytics(user))
                .products(analyticsService.getProductAnalytics(user, startDate, endDate))
                .customers(analyticsService.getCustomerAnalytics(user))
                .build();

        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Dashboard analytics retrieved successfully",
                analytics
        );
    }
}
