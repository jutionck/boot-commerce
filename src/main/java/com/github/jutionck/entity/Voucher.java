package com.github.jutionck.entity;

import com.github.jutionck.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher extends BaseEntity {
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPurchase;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    private Integer userUsageLimit;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}
