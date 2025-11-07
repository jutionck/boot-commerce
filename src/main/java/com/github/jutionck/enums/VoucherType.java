package com.github.jutionck.enums;

import lombok.Getter;

public enum VoucherType {
    PERCENTAGE("Percentage"),
    FIXED_AMOUNT("Fixed Amount"),
    FREE_SHIPPING("Free Shipping");

    @Getter
    private final String displayName;

    VoucherType(String displayName) {
        this.displayName = displayName;
    }
}
