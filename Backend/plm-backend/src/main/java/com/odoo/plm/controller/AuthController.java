package com.odoo.plm.controller;

import com.odoo.plm.dto.request.*;
import com.odoo.plm.dto.response.AuthResponse;
import com.odoo.plm.dto.response.MessageResponse;
import com.odoo.plm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for login ID: {}", request.getLoginId());
        MessageResponse response = authService.signup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for login ID: {}", request.getLoginId());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email with token
     * GET /api/auth/verify?token=xxx
     */
    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        log.info("Email verification request received");
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Resend verification email
     * POST /api/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestParam String email) {
        log.info("Resend verification request for email: {}", email);
        MessageResponse response = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot password - send reset email
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password with token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request received");
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if login ID is available
     * GET /api/auth/check-login-id?loginId=xxx
     */
    @GetMapping("/check-login-id")
    public ResponseEntity<MessageResponse> checkLoginId(@RequestParam String loginId) {
        MessageResponse response = authService.checkLoginIdAvailability(loginId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if email is available
     * GET /api/auth/check-email?email=xxx
     */
    @GetMapping("/check-email")
    public ResponseEntity<MessageResponse> checkEmail(@RequestParam String email) {
        MessageResponse response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(response);
    }
}
