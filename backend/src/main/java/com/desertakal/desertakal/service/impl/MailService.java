package com.desertakal.desertakal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.api.base-url}")
    private String apiBaseUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = apiBaseUrl + "/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Vérifiez votre adresse e-mail - DesertAkal");
        message.setSubject("Verify your email address - DesertAkal");

        String emailText = "Welcome to DesertAkal!\n\n" +
                "Thank you for joining us. To complete your registration and activate your account, " +
                "please click the link below:\n\n" +
                verificationUrl + "\n\n" +
                "This link is valid for 24 hours.\n" +
                "If you did not create an account, you can safely ignore this email.\n\n" +
                "Best regards,\n" +
                "The DesertAkal Team";

        message.setText(emailText);

        try {
            mailSender.send(message);
            log.info("Verification email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendGuideWelcomeEmail(String to, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to DesertAkal - Your Guide Account Details");

        String emailText = "Hello,\n\n" +
                "Welcome to the DesertAkal family! Your account as a Guide has been created successfully.\n\n" +
                "Here are your login credentials:\n" +
                "Email: " + to + "\n" +
                "Password: " + rawPassword + "\n\n" +
                "For security reasons, we recommend that you change your password after your first login.\n\n" +
                "You can log in at: " + apiBaseUrl + "/login\n\n" +
                "Best regards,\n" +
                "The DesertAkal Team";

        message.setText(emailText);

        try {
            mailSender.send(message);
            log.info("Guide welcome email with credentials sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }
}
