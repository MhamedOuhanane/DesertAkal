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
        String verificationUrl = apiBaseUrl + "/auth/verify-email?token=" + token;

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
}
