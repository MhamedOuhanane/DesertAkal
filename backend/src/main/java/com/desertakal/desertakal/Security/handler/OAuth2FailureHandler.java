package com.desertakal.desertakal.Security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String errorCode;
        String errorMessage;

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            String oauthError = oauthEx.getError().getErrorCode();
            log.warn("OAuth2 authentication failed. Error code: {} | Message: {}",
                    oauthError, oauthEx.getMessage());

            errorCode = switch (oauthError) {
                case "access_denied" -> "cancelled";
                case "invalid_token" -> "invalid_token";
                case "server_error" -> "server_error";
                default -> "oauth2_error";
            };
            errorMessage = switch (oauthError) {
                case "access_denied" -> "Login cancelled by user";
                case "invalid_token" -> "Invalid authentication token";
                case "server_error" -> "Provider server error";
                default -> oauthEx.getMessage();
            };
        } else {
            log.error("OAuth2 authentication failed with unexpected exception: {}",
                    exception.getMessage());
            errorCode = "unknown";
            errorMessage = "Authentication failed. Please try again.";
        }

        try {
            request.getSession().invalidate();
        } catch (Exception ignored) {}

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/auth/login")
                .queryParam("error", errorCode)
                .queryParam("message", errorMessage)
                .build()
                .toUriString();

        log.info("OAuth2 failure: Redirecting to {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}