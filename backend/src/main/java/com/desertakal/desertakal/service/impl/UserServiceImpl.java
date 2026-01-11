package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.Security.jwt.JwtService;
import com.desertakal.desertakal.exception.custom.AuthenticationException;
import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.auth.LoginRequestDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.enums.UserStatus;
import com.desertakal.desertakal.model.mapper.TouristMapper;
import com.desertakal.desertakal.model.mapper.UserMapper;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import com.desertakal.desertakal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserMapper mapper;
    private final TouristMapper touristMapper;
    private final EmailVerificationTokenService emailVerificationTokenService;


    @Override
    public void register(@NonNull RegisterDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed: Email {} is already registered", dto.getEmail());
            throw new BadRequestException("Email is already taken.");
        }
        if (repository.existsByUsername(dto.getUsername())) {
            log.warn("Registration failed: Username {} is already taken", dto.getUsername());
            throw new BadRequestException("Username is already taken.");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Registration failed: Password confirmation does not match for email: {}", dto.getEmail());
            throw new BadRequestException("Passwords do not match. Please ensure both passwords are identical.");
        }

        log.debug("Fetching role with UUID: {}", dto.getRoleUuid());
        Role role = roleRepository.findByName("TOURIST")
                        .orElseThrow(() -> {
                            log.error("Registration failed: Role UUID {} not found", "TOURIST");
                            return new ResourceNotFoundException("Role", "name", "TOURIST");
                        });

        Tourist tourist = touristMapper.toEntity(dto);
        tourist.setPassword(passwordEncoder.encode(dto.getPassword()));
        tourist.setRole(role);

        repository.save(tourist);
        emailVerificationTokenService.createVerificationToken(tourist.getEmail());

        log.info("Tourist registered successfully! User UUID: {}, Email: {}",
                tourist.getUuid(), tourist.getEmail());
    }

    @Transactional
    @Override
    public LoginDTO login(@NonNull LoginRequestDTO dto, @NonNull String ipAddress, @NonNull String userAgent) {
        User user = repository.findByEmailOrUsernameWithSecurity(dto.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with username/email: {}", dto.getUsername());
                    return new AuthenticationException("Invalid username or password.");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed: Wrong password for user {}", dto.getUsername());
            throw new AuthenticationException("Invalid username or password.");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("Login failed: User {} is BANNED", dto.getUsername());
            throw new UnauthorizedActionException("This account has been banned. Please contact administration.");
        }

        if (user.getStatus() != UserStatus.ACTIVE || !user.getEmailVerified()) {
            log.warn("Login failed: User {} is not active or not verified", dto.getUsername());
            throw new AuthenticationException("Account is disabled. Please verify your email.");
        }

        log.info("Login successful for user: {}", user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);

        RefreshTokenRequestDTO refRequestDTO = RefreshTokenRequestDTO.builder()
                .userUuid(user.getUuid())
                .deviceId(dto.getDeviceId())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        RefreshTokenDTO refreshToken = refreshTokenService.create(refRequestDTO);

        return LoginDTO.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
