package com.github.jutionck.repository;

import com.github.jutionck.entity.User;
import com.github.jutionck.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndRole(String email, UserRole role);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);

    // For Customer analytics - get customers who ordered from a specific seller
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN Order o ON o.customer.id = u.id " +
           "JOIN OrderItem oi ON oi.order.id = o.id " +
           "JOIN Product p ON oi.product.id = p.id " +
           "WHERE p.seller.id = :sellerId AND u.role = 'CUSTOMER'")
    Page<User> findCustomersBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);
}
