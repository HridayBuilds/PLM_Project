package com.odoo.plm.service;

import com.odoo.plm.dto.request.ChangePasswordRequest;
import com.odoo.plm.dto.request.UpdateProfileRequest;
import com.odoo.plm.dto.response.MessageResponse;
import com.odoo.plm.dto.response.UserResponse;
import com.odoo.plm.entity.User;
import com.odoo.plm.enums.Role;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.UserRepository;
import com.odoo.plm.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    }

    public UserResponse getCurrentUserProfile(UUID userId) {
        User user = getUserById(userId);
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getLoginId());

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public MessageResponse changePassword(UUID userId, ChangePasswordRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Constants.PASSWORDS_DO_NOT_MATCH);
        }

        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException(Constants.PASSWORD_MISMATCH);
        }

        // Validate new password is different
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getLoginId());

        return MessageResponse.success(Constants.PASSWORD_CHANGE_SUCCESS);
    }

    // Admin functions
    @Transactional
    public MessageResponse updateUserRole(UUID adminId, UUID userId, Role newRole) {
        User admin = getUserById(adminId);

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can update user roles");
        }

        User user = getUserById(userId);
        Role oldRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);

        log.info("Role updated for user {} from {} to {} by admin {}",
                user.getLoginId(), oldRole, newRole, admin.getLoginId());

        return MessageResponse.success("User role updated successfully");
    }

    @Transactional
    public MessageResponse toggleUserStatus(UUID adminId, UUID userId) {
        User admin = getUserById(adminId);

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can update user status");
        }

        User user = getUserById(userId);
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        String status = user.getIsActive() ? "activated" : "deactivated";
        log.info("User {} {} by admin {}", user.getLoginId(), status, admin.getLoginId());

        return MessageResponse.success("User " + status + " successfully");
    }

    public List<UserResponse> getAllUsers(UUID adminId) {
        User admin = getUserById(adminId);

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can view all users");
        }

        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
}
