package com.odoo.plm.dto.response;

import com.odoo.plm.enums.Role;
import com.odoo.plm.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String loginId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private UserStatus status;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Computed field for frontend compatibility
    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }
}
