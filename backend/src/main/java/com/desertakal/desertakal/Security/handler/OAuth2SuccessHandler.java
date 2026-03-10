package com.desertakal.desertakal.Security.handler;

import com.desertakal.desertakal.Security.jwt.JwtService;
import com.desertakal.desertakal.config.CookieConfig;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.enums.OauthProvider;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.repository.UserOAuthRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository repository;
    private final UserOAuthRepository oAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final ObjectMapper mapper;
    private final CookieConfig cookieConfig;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken authToke =
                (OAuth2AuthenticationToken) authentication;
        String registrationId =  authToke.getAuthorizedClientRegistrationId();

        OauthUserInfo userInfo = extractUserInfo(authToke, registrationId);

        log.info("OAuth2 login success for provider: {} | Email: {}", registrationId, userInfo.email());

        Role role = roleRepository.findByName("TOURIST").orElseThrow(() -> {
            log.error("Critical: Role TOURIST not found in database");
            return new ResourceNotFoundException("Role", "name", "TOURIST");
        });

        User user = repository.findByEmailOrUsernameWithSecurity(userInfo.email())
                .orElseGet(()-> {
                    log.info("Creating new Tourist user from OAuth2: {}", userInfo.email());
                    return repository.save(
                            Tourist.builder()
                                    .uuid(UUID.randomUUID())
                                    .firstName(userInfo.firstName())
                                    .lastName(userInfo.lastName())
                                    .username(userInfo.email())
                                    .email(userInfo.email())
                                    .emailVerified(true)
                                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                    .role(role)
                                    .lastLoginAt(LocalDateTime.now())
                                    .language(userInfo.langName())
                                    .build()
                    );
                });

        oAuthRepository.findByUserAndProvider(user, userInfo.provider())
                .orElseGet(() -> {
                    log.info("Linking new OAuth2 provider {} to user {}", userInfo.provider().name(), user.getEmail());

                    return oAuthRepository.save(
                            UserOAuth.builder()
                                    .uuid(UUID.randomUUID())
                                    .user(user)
                                    .provider(userInfo.provider())
                                    .providerId(userInfo.providerId())
                                    .build()
                    );
                });


        log.info("JWT generated for OAuth2 user: {}", user.getEmail());

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        String deviceId = request.getHeader("X-Device-ID") != null ? request.getHeader("X-Device-ID") : "OAuth2-Session";

        String accessToken = jwtService.generateAccessToken(user);
        RefreshTokenDTO refreshToken = refreshTokenService.create(
                RefreshTokenRequestDTO.builder()
                        .userUuid(user.getUuid())
                        .deviceId(deviceId)
                        .userAgent(userAgent)
                        .ipAddress(ipAddress)
                        .build()
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path("/api/auth")
                .maxAge(cookieConfig.getMaxAge())
                .sameSite(cookieConfig.getSameSite())
                .build();

        user.setLastLoginAt(LocalDateTime.now());
        repository.save(user);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("userUuid", user.getUuid().toString())
                .queryParam("username", user.getUsername())
                .queryParam("fullName", user.getFirstName() + " " + user.getLastName())
                .queryParam("photo", user.getPhoto())
                .queryParam("role", user.getRole().getName())
                .build().toUriString();

        log.info("OAuth2 Success: Redirecting user {} to frontend", user.getEmail());

        response.sendRedirect(targetUrl);
    }

    private OauthUserInfo extractUserInfo(OAuth2AuthenticationToken authToken, String registrationId) {
        String email;
        String providerId;
        String firstName = "User";
        String lastName = "OAuth";
        String langCode = "en";
        OauthProvider provider;

        if (registrationId.equalsIgnoreCase("google")) {
            OidcUser oidcUser = (OidcUser) authToken.getPrincipal();

            email = oidcUser.getEmail();
            providerId = oidcUser.getSubject();
            firstName = oidcUser.getGivenName();
            lastName = oidcUser.getFamilyName();
            langCode = oidcUser.getLocale() != null ? oidcUser.getLocale() : "en";
            provider = OauthProvider.GOOGLE;
        } else {
            OAuth2User oAuth2User = authToken.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            System.out.println((String) attributes.get("email") + " || " + oAuth2User.getName());
            email = (String) attributes.get("email");
            providerId = oAuth2User.getName();
            langCode = (String) attributes.getOrDefault("locale", "en");

            String name = (String) attributes.get("name");
            if (name == null) {
                name = (String) attributes.get("global_name");
            }
            if (name == null) {
                name = (String) attributes.get("username");
            }

            if (name.contains(" ")) {
                String[] parts = name.split(" ", 2);
                firstName = parts[0];
                lastName = parts[1];
            } else
                firstName = name;

            if (email == null) {
                email = providerId + "@" + registrationId + ".com";
            }
            provider = registrationId.equalsIgnoreCase("facebook")
                    ? OauthProvider.FACEBOOK
                    : OauthProvider.DISCORD;
        }

        String langName = gerFullLanguageName(langCode);

        return new OauthUserInfo(
                email,
                providerId,
                firstName,
                lastName,
                langName,
                provider
        );
    }

    private String gerFullLanguageName(String longCode) {
        if (longCode == null || longCode.isEmpty())
            return "English";

        Locale locale = Locale.forLanguageTag(longCode);
        return locale.getDisplayLanguage(Locale.ENGLISH);
    }

    private record OauthUserInfo(String email, String providerId, String firstName, String lastName, String langName, OauthProvider provider) {}
}
