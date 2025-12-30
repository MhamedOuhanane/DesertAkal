package com.desertakal.desertakal.service.interfaces;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface EmailVerificationTokenService {
    void createVerificationToken (@NonNull UUID userUuid);
    void confirmEmail(@NonNull String token);
}
