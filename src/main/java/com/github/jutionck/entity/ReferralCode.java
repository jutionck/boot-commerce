package com.github.jutionck.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "referral_codes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCode extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal rewardAmount = BigDecimal.valueOf(5.00);

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
