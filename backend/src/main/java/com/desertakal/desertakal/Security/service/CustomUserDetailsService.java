package com.desertakal.desertakal.Security.service;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.debug("Loading user by username/email: {}", username);

        return repository.findByEmailOrUsernameWithSecurity(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "identifier", username)
                );
    }


    @Transactional(readOnly = true)
//    @Cacheable(value = "users", key = "#uuid")
    public CustomUserDetails loadUserByUuid(String uuid) throws UsernameNotFoundException {
        log.debug("Loading user by UUID from DB (Cache Miss): {}", uuid);

        return repository.findWithSecurityByUuid(UUID.fromString(uuid))
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "identifier", uuid)
                );
    }
}
