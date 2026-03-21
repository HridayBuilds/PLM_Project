package com.odoo.plm.enums;

public enum UserStatus {
    PENDING,    // User registered but not yet activated by Admin
    ACTIVE,     // User activated and can access the system
    INACTIVE    // User deactivated
}
