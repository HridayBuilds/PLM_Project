package com.odoo.plm.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TokenGenerator {

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }
}
