package com.odoo.plm.service;

import com.odoo.plm.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@plm-system.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify?token=" + token;

            String subject = Constants.VERIFICATION_EMAIL_SUBJECT;
            String body = buildVerificationEmailBody(verificationUrl);

            sendEmail(toEmail, subject, body);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String subject = Constants.PASSWORD_RESET_EMAIL_SUBJECT;
            String body = buildPasswordResetEmailBody(resetUrl);

            sendEmail(toEmail, subject, body);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName, String loginId) {
        try {
            String subject = "Welcome to PLM System!";
            String body = buildWelcomeEmailBody(firstName, loginId);

            sendEmail(toEmail, subject, body);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private String buildVerificationEmailBody(String verificationUrl) {
        return """
                Hello,

                Thank you for registering with PLM System!

                Please verify your email address by clicking the link below:

                %s

                This link will expire in 24 hours.

                If you didn't create an account, please ignore this email.

                Best regards,
                PLM System Team
                """.formatted(verificationUrl);
    }

    private String buildPasswordResetEmailBody(String resetUrl) {
        return """
                Hello,

                We received a request to reset your password for your PLM System account.

                Click the link below to reset your password:

                %s

                This link will expire in 1 hour.

                If you didn't request a password reset, please ignore this email.

                Best regards,
                PLM System Team
                """.formatted(resetUrl);
    }

    private String buildWelcomeEmailBody(String firstName, String loginId) {
        return """
                Hello %s,

                Welcome to PLM System!

                Your email has been successfully verified and your account is now active.

                Your Login ID: %s

                You can now log in and start using the platform.

                Best regards,
                PLM System Team
                """.formatted(firstName != null ? firstName : "", loginId);
    }
}
