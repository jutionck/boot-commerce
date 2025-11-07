package com.github.jutionck.enums;

import lombok.Getter;

public enum UserRole {
    CUSTOMER("Customer"),
    SELLER("Seller"),
    ADMIN("Admin");

    @Getter
    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
