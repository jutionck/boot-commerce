package com.github.jutionck.repository;

import com.github.jutionck.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralCodeRepository extends JpaRepository<ReferralCode, UUID> {
    // Find referral code by code
    Optional<ReferralCode> findByCode(String code);

    // Find active referral code by code
    Optional<ReferralCode> findByCodeAndIsActive(String code, Boolean isActive);

    // Check if code exists
    boolean existsByCode(String code);

    // Find referral codes by user
    List<ReferralCode> findByUserId(UUID userId);

    // Find active referral codes by user
    List<ReferralCode> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    // Check if user already has active referral code
    boolean existsByUserIdAndIsActive(UUID userId, Boolean isActive);
}
