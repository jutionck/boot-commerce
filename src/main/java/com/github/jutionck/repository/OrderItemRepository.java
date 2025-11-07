package com.github.jutionck.repository;

import com.github.jutionck.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    // Find order items by product (useful for checking if product can be deleted)
    List<OrderItem> findByProductId(UUID productId);

    // Get top selling products for a seller
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.subtotal) as totalRevenue " +
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "JOIN oi.order o " +
           "WHERE p.seller.id = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> findTopSellingProducts(@Param("sellerId") UUID sellerId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
