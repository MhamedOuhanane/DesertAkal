package com.desertakal.desertakal.Security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DeviceIdOAuth2Filter extends OncePerRequestFilter {
    public static final String DEVICE_ID_SESSION_KEY = "oauth2_device_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/oauth2/authorization/")) {
            String deviceId = request.getParameter("device_id");

            if (deviceId != null && !deviceId.isEmpty()) {
                request.getSession(true).setAttribute(DEVICE_ID_SESSION_KEY, deviceId);
                log.info("OAuth2 flow detected. Device ID stored in session: {}", deviceId);
            } else {
                log.warn("OAuth2 login started but no device_id found in request params!");
            }
        }
        filterChain.doFilter(request, response);
    }
}
