package com.github.jutionck.enums;

import lombok.Getter;

public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    @Getter
    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
