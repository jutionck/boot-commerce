package com.github.jutionck.controller;

import com.github.jutionck.dto.request.VoucherRequest;
import com.github.jutionck.entity.User;
import com.github.jutionck.service.VoucherService;
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
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "Voucher management endpoints")
public class VoucherController {
    private final VoucherService voucherService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> createVoucher(
            @Valid @RequestBody VoucherRequest request,
            @AuthenticationPrincipal User seller
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                "Voucher created successfully",
                voucherService.createVoucher(request, seller)
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllVouchers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Vouchers retrieved successfully",
                voucherService.getAllVouchers(pageable)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveVouchers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Active vouchers retrieved successfully",
                voucherService.getActiveVouchers(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVoucherById(@PathVariable UUID id) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Voucher retrieved successfully",
                voucherService.getVoucherById(id)
        );
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateVoucher(@PathVariable String code) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Voucher is valid",
                voucherService.validateVoucherCode(code)
        );
    }

    @GetMapping("/my-vouchers")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getMyVouchers(
            @AuthenticationPrincipal User seller,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Vouchers retrieved successfully",
                voucherService.getSellerVouchers(seller, pageable)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> updateVoucher(
            @PathVariable UUID id,
            @Valid @RequestBody VoucherRequest request,
            @AuthenticationPrincipal User seller
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Voucher updated successfully",
                voucherService.updateVoucher(id, request, seller)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> deleteVoucher(
            @PathVariable UUID id,
            @AuthenticationPrincipal User seller
    ) {
        voucherService.deleteVoucher(id, seller);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Voucher deleted successfully",
                null
        );
    }
}
