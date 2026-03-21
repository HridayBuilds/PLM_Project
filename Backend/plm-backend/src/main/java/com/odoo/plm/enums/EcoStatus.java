package com.odoo.plm.enums;

public enum EcoStatus {
    DRAFT,          // ECO created but not submitted
    IN_PROGRESS,    // ECO submitted and moving through stages
    APPROVED,       // ECO approved but not yet applied
    APPLIED         // ECO applied to master data
}
