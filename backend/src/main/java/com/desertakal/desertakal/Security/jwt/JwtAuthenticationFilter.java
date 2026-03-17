package com.desertakal.desertakal.Security.jwt;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.Security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {

                String uuid = jwtService.extractSub(token);
                if (uuid != null) {
                    CustomUserDetails userDetails = userDetailsService.loadUserByUuid(uuid);

                    if (jwtService.isValidToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                try {
                    request.getSession(false);
                    if (request.getSession(false) != null) {
                        request.getSession().invalidate();
                    }
                } catch (Exception ignored) {}
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
