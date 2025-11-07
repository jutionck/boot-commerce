package com.github.jutionck.repository;

import com.github.jutionck.entity.Voucher;
import com.github.jutionck.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    // Find voucher by code
    Optional<Voucher> findByCode(String code);

    // Check if code exists
    boolean existsByCode(String code);

    // Check if code exists for another voucher
    boolean existsByCodeAndIdNot(String code, UUID id);

    // Find vouchers by seller
    Page<Voucher> findBySellerId(UUID sellerId, Pageable pageable);

    // Find active vouchers by seller
    Page<Voucher> findBySellerIdAndIsActive(UUID sellerId, Boolean isActive, Pageable pageable);

    // Check if seller owns voucher
    boolean existsByIdAndSellerId(UUID voucherId, UUID sellerId);

    // Find active vouchers
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND " +
           "v.startDate <= :now AND v.endDate >= :now")
    Page<Voucher> findActiveVouchers(@Param("now") LocalDateTime now, Pageable pageable);

    // Find active vouchers by type
    Page<Voucher> findByIsActiveAndType(Boolean isActive, VoucherType type, Pageable pageable);
}
