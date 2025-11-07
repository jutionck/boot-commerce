package com.github.jutionck.repository;

import com.github.jutionck.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    // Find products by seller
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    // Check if seller owns product
    boolean existsByIdAndSellerId(UUID productId, UUID sellerId);

    // Get unique categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();

    // Get unique brands
    @Query("SELECT DISTINCT p.brand FROM Product p ORDER BY p.brand")
    List<String> findDistinctBrands();

    // Search products by name or description
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    // Get low stock products for a seller
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.stock <= :threshold ORDER BY p.stock ASC")
    List<Product> findLowStockProducts(@Param("sellerId") UUID sellerId, @Param("threshold") Integer threshold);
}
