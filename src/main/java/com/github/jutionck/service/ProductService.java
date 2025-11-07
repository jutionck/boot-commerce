package com.github.jutionck.service;

import com.github.jutionck.dto.request.ProductRequest;
import com.github.jutionck.dto.request.ProductUpdateRequest;
import com.github.jutionck.dto.response.ProductResponse;
import com.github.jutionck.entity.Product;
import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import com.github.jutionck.exceptions.ResourceNotFoundException;
import com.github.jutionck.exceptions.UnauthorizedException;
import com.github.jutionck.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, User seller) {
        log.info("Creating product: {} by seller: {}", request.getName(), seller.getEmail());

        if (seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only sellers can create products");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .category(request.getCategory())
                .brand(request.getBrand())
                .stock(request.getStock())
                .images(request.getImages())
                .seller(seller)
                .specifications(request.getSpecifications())
                .build();

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    public ProductResponse getProductById(UUID productId) {
        Product product = findProductById(productId);
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getProductsByFilters(
            String search,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Specification<Product> spec = Specification.where(null);

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("category")), category.toLowerCase())
            );
        }

        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("brand")), brand.toLowerCase())
            );
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice)
            );
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice)
            );
        }

        return productRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getSellerProducts(UUID sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getCurrentSellerProducts(User seller, Pageable pageable) {
        return productRepository.findBySellerId(seller.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request, User seller) {
        Product product = findProductById(productId);

        // Check if seller owns this product
        if (!product.getSeller().getId().equals(seller.getId()) && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to update this product");
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCompareAtPrice() != null) {
            product.setCompareAtPrice(request.getCompareAtPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getSpecifications() != null) {
            product.setSpecifications(request.getSpecifications());
        }

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional
    public void deleteProduct(UUID productId, User seller) {
        Product product = findProductById(productId);

        // Check if seller owns this product
        if (!product.getSeller().getId().equals(seller.getId()) && seller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You don't have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: {} by seller: {}", productId, seller.getEmail());
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    public List<String> getBrands() {
        return productRepository.findDistinctBrands();
    }

    // Helper methods
    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.SellerInfo sellerInfo = ProductResponse.SellerInfo.builder()
                .id(product.getSeller().getId())
                .firstName(product.getSeller().getFirstName())
                .lastName(product.getSeller().getLastName())
                .email(product.getSeller().getEmail())
                .build();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .stock(product.getStock())
                .images(product.getImages())
                .seller(sellerInfo)
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .specifications(product.getSpecifications())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
