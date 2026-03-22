package com.odoo.plm.controller;

import com.odoo.plm.dto.request.ChangePasswordRequest;
import com.odoo.plm.dto.request.UpdateProfileRequest;
import com.odoo.plm.dto.request.admin.UpdateUserRoleRequest;
import com.odoo.plm.dto.response.MessageResponse;
import com.odoo.plm.dto.response.UserResponse;
import com.odoo.plm.entity.User;
import com.odoo.plm.enums.Role;
import com.odoo.plm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.info("Get profile request for user: {}", currentUser.getLoginId());
        UserResponse response = userService.getCurrentUserProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile
     * PUT /api/users/update-profile
     */
    @PutMapping("/update-profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request for user: {}", currentUser.getLoginId());
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * POST /api/users/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request for user: {}", currentUser.getLoginId());
        MessageResponse response = userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    // Admin endpoints

    /**
     * Get all users (Admin only)
     * GET /api/users/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal User admin) {
        log.info("Get all users request by admin: {}", admin.getLoginId());
        List<UserResponse> users = userService.getAllUsers(admin.getId());
        return ResponseEntity.ok(users);
    }

    /**
     * Get pending users awaiting activation (Admin only)
     * GET /api/users/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getPendingUsers(@AuthenticationPrincipal User admin) {
        log.info("Get pending users request by admin: {}", admin.getLoginId());
        List<UserResponse> users = userService.getPendingUsers(admin.getId());
        return ResponseEntity.ok(users);
    }

    /**
     * Activate a pending user with a role (Admin only)
     * POST /api/users/{userId}/activate
     */
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activateUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID userId,
            @RequestParam Role role) {

        log.info("Activate user request for user {} with role {} by admin: {}", userId, role, admin.getLoginId());
        MessageResponse response = userService.activateUser(admin.getId(), userId, role);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user role (Admin only)
     * PUT /api/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateUserRole(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        log.info("Update role request for user {} by admin: {}", userId, admin.getLoginId());
        MessageResponse response = userService.updateUserRole(admin.getId(), userId, request.getRole());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all approvers (Admin only)
     * GET /api/users/approvers
     */
    @GetMapping("/approvers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getApprovers() {
        List<UserResponse> approvers = userService.getUsersByRole(Role.APPROVER);
        return ResponseEntity.ok(approvers);
    }

    /**
     * Toggle user active status (Admin only)
     * PUT /api/users/{userId}/toggle-status
     */
    @PutMapping("/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> toggleUserStatus(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID userId) {

        log.info("Toggle status request for user {} by admin: {}", userId, admin.getLoginId());
        MessageResponse response = userService.toggleUserStatus(admin.getId(), userId);
        return ResponseEntity.ok(response);
    }
}
