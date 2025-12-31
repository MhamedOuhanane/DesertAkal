package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.entity.EmailVerificationToken;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.repository.EmailTokenRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationTokenServiceImpl implements EmailVerificationTokenService {
    private final EmailTokenRepository repository;
    private final MailService mailService;
    private final UserRepository userRepository;

    @Override
    public void createVerificationToken(@NonNull String email) {
        log.info("Initiating email verification process for User email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Verification failed: User not found for email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        try {
            repository.deleteByUser(user);
            log.debug("Cleared existing verification tokens for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to delete old tokens for user {}: {}", email, e.getMessage());
        }


        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        repository.save(verificationToken);
        log.info("New verification token generated for user [{}]. Expiration: {}",
                user.getEmail(), verificationToken.getExpiresAt());

        mailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    @Override
    public void confirmEmail(@NonNull String tokenValue) {
        log.info("Attempting to verify email with token: {}", tokenValue);

        EmailVerificationToken token = repository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Email verification failed: Token [{}] not found", tokenValue);
                    return new BadRequestException("Invalid verification link.");
                });

        if (token.isUsed()) {
            log.warn("Email verification failed: Token [{}] was already used by user: {}",
                    tokenValue, token.getUser().getEmail());
            throw new BadRequestException("This link has already been used.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Email verification failed: Token expired at {}", token.getExpiresAt());
            throw new BadRequestException("This link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);

        token.setUsed(true);

        log.info("User [{}] successfully verified their email.", user.getEmail());
    }
}
