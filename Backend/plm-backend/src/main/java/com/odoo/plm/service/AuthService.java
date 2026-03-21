package com.odoo.plm.service;

import com.odoo.plm.dto.request.*;
import com.odoo.plm.dto.response.AuthResponse;
import com.odoo.plm.dto.response.MessageResponse;
import com.odoo.plm.entity.PasswordResetToken;
import com.odoo.plm.entity.User;
import com.odoo.plm.entity.VerificationToken;
import com.odoo.plm.enums.Role;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.repository.PasswordResetTokenRepository;
import com.odoo.plm.repository.UserRepository;
import com.odoo.plm.repository.VerificationTokenRepository;
import com.odoo.plm.security.JwtService;
import com.odoo.plm.util.Constants;
import com.odoo.plm.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public MessageResponse signup(SignupRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Constants.PASSWORDS_DO_NOT_MATCH);
        }

        // Check if login ID exists
        if (userRepository.existsByLoginId(request.getLoginId().toUpperCase())) {
            throw new DuplicateResourceException(Constants.LOGIN_ID_EXISTS);
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException(Constants.EMAIL_EXISTS);
        }

        // Create new user
        User user = User.builder()
                .loginId(request.getLoginId().toUpperCase())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ENGINEERING_USER) // Default role
                .isVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getLoginId());

        // Generate verification token
        String token = tokenGenerator.generateVerificationToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(Constants.VERIFICATION_TOKEN_EXPIRATION_HOURS))
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return MessageResponse.success(Constants.SIGNUP_SUCCESS);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLoginId().toUpperCase(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Check if account is verified
            if (!user.getIsVerified()) {
                throw new DisabledException(Constants.ACCOUNT_NOT_VERIFIED);
            }

            // Check if account is active
            if (!user.getIsActive()) {
                throw new DisabledException(Constants.ACCOUNT_DISABLED);
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);

            log.info("User logged in: {}", user.getLoginId());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .loginId(user.getLoginId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .isVerified(user.getIsVerified())
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for login ID: {}", request.getLoginId());
            throw new BadCredentialsException(Constants.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        if (verificationToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        if (verificationToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        verificationToken.setIsUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getLoginId());

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), user.getLoginId());

        return MessageResponse.success(Constants.VERIFICATION_SUCCESS);
    }

    @Transactional
    public MessageResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        if (user.getIsVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Delete existing token
        verificationTokenRepository.deleteByUser(user);

        // Generate new token
        String token = tokenGenerator.generateVerificationToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(Constants.VERIFICATION_TOKEN_EXPIRATION_HOURS))
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return MessageResponse.success("Verification email has been resent");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        // Delete existing password reset tokens
        passwordResetTokenRepository.deleteByUser(user);

        // Generate new token
        String token = tokenGenerator.generatePasswordResetToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(Constants.PASSWORD_RESET_TOKEN_EXPIRATION_HOURS))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset email sent to: {}", user.getEmail());

        return MessageResponse.success(Constants.PASSWORD_RESET_EMAIL_SENT);
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(Constants.PASSWORDS_DO_NOT_MATCH);
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        if (resetToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset for user: {}", user.getLoginId());

        return MessageResponse.success(Constants.PASSWORD_RESET_SUCCESS);
    }

    public MessageResponse checkLoginIdAvailability(String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId.toUpperCase());
        if (exists) {
            return MessageResponse.error("Login ID is already taken");
        }
        return MessageResponse.success("Login ID is available");
    }

    public MessageResponse checkEmailAvailability(String email) {
        boolean exists = userRepository.existsByEmail(email.toLowerCase());
        if (exists) {
            return MessageResponse.error("Email is already registered");
        }
        return MessageResponse.success("Email is available");
    }
}
