package com.github.jutionck.controller;

import com.github.jutionck.entity.User;
import com.github.jutionck.service.CustomerService;
import com.github.jutionck.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getMyCustomers(
            @AuthenticationPrincipal User seller,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Customers retrieved successfully",
                customerService.getSellerCustomers(seller, pageable)
        );
    }
}
