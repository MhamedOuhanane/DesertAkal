package com.desertakal.desertakal.Security.user;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return repository.findByEmailOrUsernameWithSecurity(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "identifier", username)
                );
    }


    @Cacheable(value = "users", key = "#uuid")
    public CustomUserDetails loadUserByUuid(String uuid) throws UsernameNotFoundException {
        return repository.findWithSecurityByUuid(UUID.fromString(uuid))
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "identifier", uuid)
                );
    }
}
