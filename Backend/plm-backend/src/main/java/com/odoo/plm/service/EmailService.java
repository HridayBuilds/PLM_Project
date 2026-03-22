package com.odoo.plm.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@ecova.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.name:Ecova}")
    private String appName;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify?token=" + token;
            String subject = "Verify your Ecova account";
            String html = buildVerificationEmailHtml(verificationUrl);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String subject = "Reset your Ecova password";
            String html = buildPasswordResetEmailHtml(resetUrl);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName, String loginId) {
        try {
            String subject = "Welcome to Ecova!";
            String html = buildWelcomeEmailHtml(firstName, loginId);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendEcoApprovalNotification(String toEmail, String ecoTitle, String status, String approverName) {
        try {
            String subject = "ECO " + status + ": " + ecoTitle;
            String html = buildEcoNotificationHtml(ecoTitle, status, approverName);
            sendHtmlEmail(toEmail, subject, html);
            log.info("ECO notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send ECO notification email to: {}", toEmail, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String getEmailWrapper(String content) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #0a0a0c;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #0a0a0c; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 480px; background-color: #111114; border-radius: 12px; border: 1px solid #2a2a32;">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 32px 32px 24px 32px; border-bottom: 1px solid #2a2a32;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                            <tr>
                                                <td>
                                                    <div style="display: inline-flex; align-items: center;">
                                                        <div style="width: 36px; height: 36px; background-color: #2dd4bf; border-radius: 8px; display: inline-block; text-align: center; line-height: 36px; margin-right: 12px;">
                                                            <span style="color: white; font-weight: 700; font-size: 16px;">E</span>
                                                        </div>
                                                        <span style="font-size: 24px; font-weight: 700; color: #F0F0F5;">Ecova</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 32px;">
                                        %s
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 32px; border-top: 1px solid #2a2a32; text-align: center;">
                                        <p style="margin: 0; font-size: 12px; color: #55556A;">
                                            Engineering Change Management System
                                        </p>
                                        <p style="margin: 8px 0 0 0; font-size: 12px; color: #55556A;">
                                            &copy; 2026 Ecova. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(content);
    }

    private String buildVerificationEmailHtml(String verificationUrl) {
        String content = """
            <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 600; color: #F0F0F5;">
                Verify your email
            </h2>
            <p style="margin: 0 0 24px 0; font-size: 14px; color: #9090A8; line-height: 1.6;">
                Thanks for signing up! Please verify your email address by clicking the button below.
            </p>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin: 0 0 24px 0;">
                <tr>
                    <td style="background-color: #2dd4bf; border-radius: 6px;">
                        <a href="%s" style="display: inline-block; padding: 12px 24px; font-size: 14px; font-weight: 500; color: white; text-decoration: none;">
                            Verify Email
                        </a>
                    </td>
                </tr>
            </table>
            <p style="margin: 0 0 8px 0; font-size: 12px; color: #55556A;">
                Or copy and paste this link:
            </p>
            <p style="margin: 0 0 24px 0; font-size: 12px; color: #2dd4bf; word-break: break-all;">
                %s
            </p>
            <p style="margin: 0; font-size: 12px; color: #55556A;">
                This link expires in 24 hours. If you didn't create an account, ignore this email.
            </p>
            """.formatted(verificationUrl, verificationUrl);
        return getEmailWrapper(content);
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        String content = """
            <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 600; color: #F0F0F5;">
                Reset your password
            </h2>
            <p style="margin: 0 0 24px 0; font-size: 14px; color: #9090A8; line-height: 1.6;">
                We received a request to reset your password. Click the button below to choose a new one.
            </p>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin: 0 0 24px 0;">
                <tr>
                    <td style="background-color: #2dd4bf; border-radius: 6px;">
                        <a href="%s" style="display: inline-block; padding: 12px 24px; font-size: 14px; font-weight: 500; color: white; text-decoration: none;">
                            Reset Password
                        </a>
                    </td>
                </tr>
            </table>
            <p style="margin: 0 0 8px 0; font-size: 12px; color: #55556A;">
                Or copy and paste this link:
            </p>
            <p style="margin: 0 0 24px 0; font-size: 12px; color: #2dd4bf; word-break: break-all;">
                %s
            </p>
            <p style="margin: 0; font-size: 12px; color: #55556A;">
                This link expires in 1 hour. If you didn't request this, ignore this email.
            </p>
            """.formatted(resetUrl, resetUrl);
        return getEmailWrapper(content);
    }

    private String buildWelcomeEmailHtml(String firstName, String loginId) {
        String displayName = firstName != null && !firstName.isEmpty() ? firstName : "there";
        String content = """
            <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 600; color: #F0F0F5;">
                Welcome to Ecova, %s!
            </h2>
            <p style="margin: 0 0 24px 0; font-size: 14px; color: #9090A8; line-height: 1.6;">
                Your account has been verified and is now active. You can start managing engineering changes right away.
            </p>
            <div style="background-color: #1a1a1f; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                <p style="margin: 0 0 8px 0; font-size: 12px; color: #55556A; text-transform: uppercase; letter-spacing: 0.5px;">
                    Your Login ID
                </p>
                <p style="margin: 0; font-size: 16px; font-weight: 500; color: #2dd4bf; font-family: monospace;">
                    %s
                </p>
            </div>
            <table role="presentation" cellspacing="0" cellpadding="0" style="margin: 0 0 24px 0;">
                <tr>
                    <td style="background-color: #2dd4bf; border-radius: 6px;">
                        <a href="%s/auth" style="display: inline-block; padding: 12px 24px; font-size: 14px; font-weight: 500; color: white; text-decoration: none;">
                            Go to Dashboard
                        </a>
                    </td>
                </tr>
            </table>
            <p style="margin: 0; font-size: 12px; color: #55556A; line-height: 1.6;">
                Need help getting started? Check out our documentation or contact support.
            </p>
            """.formatted(displayName, loginId, frontendUrl);
        return getEmailWrapper(content);
    }

    private String buildEcoNotificationHtml(String ecoTitle, String status, String approverName) {
        String statusColor = status.equalsIgnoreCase("Approved") ? "#22c55e" : "#ef4444";
        String content = """
            <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 600; color: #F0F0F5;">
                ECO Status Update
            </h2>
            <p style="margin: 0 0 24px 0; font-size: 14px; color: #9090A8; line-height: 1.6;">
                An engineering change order has been updated.
            </p>
            <div style="background-color: #1a1a1f; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                <p style="margin: 0 0 12px 0; font-size: 12px; color: #55556A; text-transform: uppercase; letter-spacing: 0.5px;">
                    ECO Title
                </p>
                <p style="margin: 0 0 16px 0; font-size: 16px; font-weight: 500; color: #F0F0F5;">
                    %s
                </p>
                <p style="margin: 0 0 8px 0; font-size: 12px; color: #55556A; text-transform: uppercase; letter-spacing: 0.5px;">
                    Status
                </p>
                <p style="margin: 0 0 16px 0; font-size: 14px; font-weight: 500; color: %s;">
                    %s
                </p>
                <p style="margin: 0 0 8px 0; font-size: 12px; color: #55556A; text-transform: uppercase; letter-spacing: 0.5px;">
                    By
                </p>
                <p style="margin: 0; font-size: 14px; color: #9090A8;">
                    %s
                </p>
            </div>
            <table role="presentation" cellspacing="0" cellpadding="0">
                <tr>
                    <td style="background-color: #2dd4bf; border-radius: 6px;">
                        <a href="%s/ecos" style="display: inline-block; padding: 12px 24px; font-size: 14px; font-weight: 500; color: white; text-decoration: none;">
                            View ECO
                        </a>
                    </td>
                </tr>
            </table>
            """.formatted(ecoTitle, statusColor, status, approverName, frontendUrl);
        return getEmailWrapper(content);
    }
}
