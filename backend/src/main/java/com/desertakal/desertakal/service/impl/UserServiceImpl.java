package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.auth.LoginRequestDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.mapper.TouristMapper;
import com.desertakal.desertakal.model.mapper.UserMapper;
import com.desertakal.desertakal.repository.RefreshTokenRepository;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import com.desertakal.desertakal.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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

    @Override
    public LoginDTO login(@NonNull LoginRequestDTO dto) {
        return null;
    }
}
