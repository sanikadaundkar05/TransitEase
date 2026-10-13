package com.transitease.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    PASSENGER,
    DOCUMENT_VERIFICATION_OFFICER,
    PASS_APPROVAL_OFFICER,
    ROUTE_FARE_MANAGER,
    CUSTOMER_SUPPORT,
    ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        if (clean.startsWith("ROLE_")) {
            clean = clean.substring(5);
        }
        return Role.valueOf(clean);
    }
}