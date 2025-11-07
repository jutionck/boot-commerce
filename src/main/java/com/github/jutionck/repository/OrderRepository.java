package com.github.jutionck.repository;

import com.github.jutionck.entity.Order;
import com.github.jutionck.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    // Find orders by customer
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    // Find orders by customer and status
    Page<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);

    // Find orders containing products from a seller
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId")
    Page<Order> findOrdersBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

    // Find orders by seller and status
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId AND o.status = :status")
    Page<Order> findOrdersBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") OrderStatus status, Pageable pageable);

    // Check if order contains seller's product
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE o.id = :orderId AND p.seller.id = :sellerId")
    boolean orderContainsSellerProduct(@Param("orderId") UUID orderId, @Param("sellerId") UUID sellerId);

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Count orders by status for seller
    @Query("SELECT COUNT(DISTINCT o) FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId AND o.status = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") OrderStatus status);

    // Get orders within date range for analytics
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get seller's orders within date range
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findSellerOrdersBetweenDates(@Param("sellerId") UUID sellerId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
