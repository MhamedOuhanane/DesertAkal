package com.desertakal.desertakal.Security.user;

import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.enums.UserStatus;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID uuid;
    private final String email;
    private final String password;
    private final String roleName;
    private final UserStatus status;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.uuid = user.getUuid();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roleName = user.getRole().getName();
        this.status = user.getStatus();
        this.emailVerified = user.getEmailVerified();

        Collection<SimpleGrantedAuthority> auth = user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .collect(Collectors.toList());

        auth.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        this.authorities = auth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return uuid.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && emailVerified;
    }
}
