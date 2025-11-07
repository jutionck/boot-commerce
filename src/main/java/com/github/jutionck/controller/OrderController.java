package com.github.jutionck.controller;

import com.github.jutionck.dto.request.CreateOrderRequest;
import com.github.jutionck.dto.request.UpdateOrderStatusRequest;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.service.OrderService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User customer
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                "Order created successfully",
                orderService.createOrder(request, customer)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // Admin sees all orders
        if (user.getRole() == UserRole.ADMIN) {
            return ResponseUtil.buildPageResponse(
                    HttpStatus.OK,
                    "Orders retrieved successfully",
                    orderService.getAllOrders(pageable)
            );
        }

        // Seller sees orders containing their products
        if (user.getRole() == UserRole.SELLER) {
            return ResponseUtil.buildPageResponse(
                    HttpStatus.OK,
                    "Orders retrieved successfully",
                    orderService.getSellerOrders(user, pageable)
            );
        }

        // Customer sees their own orders
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Orders retrieved successfully",
                orderService.getCustomerOrders(user, pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Order retrieved successfully",
                orderService.getOrderById(id, user)
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Order status updated successfully",
                orderService.updateOrderStatus(id, request, user)
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> cancelOrder(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Order cancelled successfully",
                orderService.cancelOrder(id, reason, user)
        );
    }
}
