package com.github.jutionck.controller;

import com.github.jutionck.dto.request.ProductRequest;
import com.github.jutionck.dto.request.ProductUpdateRequest;
import com.github.jutionck.entity.User;
import com.github.jutionck.service.ProductService;
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

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User seller
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                "Product created successfully",
                productService.createProduct(request, seller)
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (search != null || category != null || brand != null || minPrice != null || maxPrice != null) {
            return ResponseUtil.buildPageResponse(
                    HttpStatus.OK,
                    "Products retrieved successfully",
                    productService.getProductsByFilters(search, category, brand, minPrice, maxPrice, pageable)
            );
        }

        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Products retrieved successfully",
                productService.getAllProducts(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable UUID id) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Product retrieved successfully",
                productService.getProductById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal User seller
    ) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Product updated successfully",
                productService.updateProduct(id, request, seller)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> deleteProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal User seller
    ) {
        productService.deleteProduct(id, seller);
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Product deleted successfully",
                null
        );
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getProductsBySeller(
            @PathVariable UUID sellerId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Products retrieved successfully",
                productService.getSellerProducts(sellerId, pageable)
        );
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> getMyProducts(
            @AuthenticationPrincipal User seller,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseUtil.buildPageResponse(
                HttpStatus.OK,
                "Products retrieved successfully",
                productService.getCurrentSellerProducts(seller, pageable)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Categories retrieved successfully",
                productService.getCategories()
        );
    }

    @GetMapping("/brands")
    public ResponseEntity<?> getBrands() {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.OK,
                "Brands retrieved successfully",
                productService.getBrands()
        );
    }
}
