package com.github.jutionck.enums;

import lombok.Getter;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    @Getter
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
}
