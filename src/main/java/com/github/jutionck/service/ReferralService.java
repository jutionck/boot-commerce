package com.github.jutionck.service;

import com.github.jutionck.dto.response.ReferralCodeResponse;
import com.github.jutionck.entity.ReferralCode;
import com.github.jutionck.entity.User;
import com.github.jutionck.exceptions.ResourceDuplicateException;
import com.github.jutionck.exceptions.ResourceNotFoundException;
import com.github.jutionck.repository.ReferralCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferralService {
    private final ReferralCodeRepository referralCodeRepository;

    @Transactional
    public ReferralCodeResponse generateReferralCode(User user) {
        log.info("Generating referral code for user: {}", user.getEmail());

        // Check if user already has an active referral code
        if (referralCodeRepository.existsByUserIdAndIsActive(user.getId(), true)) {
            throw new ResourceDuplicateException("User already has an active referral code");
        }

        String code = generateUniqueCode(user);

        ReferralCode referralCode = ReferralCode.builder()
                .code(code)
                .user(user)
                .build();

        referralCode = referralCodeRepository.save(referralCode);
        return mapToResponse(referralCode);
    }

    public ReferralCodeResponse getReferralCode(User user) {
        ReferralCode referralCode = referralCodeRepository
                .findByUserIdAndIsActive(user.getId(), true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active referral code found"));

        return mapToResponse(referralCode);
    }

    public ReferralCodeResponse validateReferralCode(String code) {
        ReferralCode referralCode = referralCodeRepository.findByCodeAndIsActive(code, true)
                .orElseThrow(() -> new ResourceNotFoundException("Referral code not found or inactive"));

        return mapToResponse(referralCode);
    }

    private String generateUniqueCode(User user) {
        // Generate code from email and UUID
        String baseCode = user.getEmail().split("@")[0].toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .substring(0, Math.min(6, user.getEmail().split("@")[0].length()));

        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String code = baseCode + uniquePart;

        // Ensure code is unique
        int counter = 1;
        String originalCode = code;
        while (referralCodeRepository.existsByCode(code)) {
            code = originalCode + counter++;
        }

        return code;
    }

    private ReferralCodeResponse mapToResponse(ReferralCode referralCode) {
        return ReferralCodeResponse.builder()
                .id(referralCode.getId())
                .code(referralCode.getCode())
                .usageCount(referralCode.getUsageCount())
                .rewardAmount(referralCode.getRewardAmount())
                .totalEarnings(referralCode.getTotalEarnings())
                .isActive(referralCode.getIsActive())
                .createdAt(referralCode.getCreatedAt())
                .updatedAt(referralCode.getUpdatedAt())
                .build();
    }
}
