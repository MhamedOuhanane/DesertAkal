package com.desertakal.desertakal.Security.service;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service("ownerSecurityService")
@RequiredArgsConstructor
@Slf4j
public class OwnerSecurityService {
    public boolean isOwner(UUID resourceUuid, Authentication authentication, boolean allowAdmin) {
        if (authentication == null || !authentication.isAuthenticated())
            return false;

        if (allowAdmin) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
            if (isAdmin) return true;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            boolean isSameUser = Objects.equals(resourceUuid, userDetails.getUuid());
            log.debug("Security Check - Resource UUID: {}, User UUID: {}, Match: {}",
                    resourceUuid, userDetails.getUuid(), isSameUser);
            return isSameUser;
        }

        return false;
    }
}
