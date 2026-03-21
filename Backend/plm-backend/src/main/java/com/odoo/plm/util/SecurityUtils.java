package com.odoo.plm.util;

import com.odoo.plm.entity.User;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        String loginId = authentication.getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user.getRole().name().equals("ADMIN");
    }

    public boolean isEngineeringUser() {
        User user = getCurrentUser();
        return user.getRole().name().equals("ENGINEERING_USER");
    }

    public boolean isApprover() {
        User user = getCurrentUser();
        return user.getRole().name().equals("APPROVER");
    }

    public boolean isOperationsUser() {
        User user = getCurrentUser();
        return user.getRole().name().equals("OPERATIONS_USER");
    }

    public boolean canCreateOrModify() {
        User user = getCurrentUser();
        String role = user.getRole().name();
        return role.equals("ENGINEERING_USER") || role.equals("ADMIN");
    }

    public boolean canApprove() {
        User user = getCurrentUser();
        String role = user.getRole().name();
        return role.equals("APPROVER") || role.equals("ADMIN");
    }
}
