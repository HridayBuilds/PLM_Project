package com.odoo.plm.dto.response.admin;

import com.odoo.plm.enums.Role;
import com.odoo.plm.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {

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
    private LocalDateTime updatedAt;
}
