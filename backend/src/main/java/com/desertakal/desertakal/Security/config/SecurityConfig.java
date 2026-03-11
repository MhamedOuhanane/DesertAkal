package com.desertakal.desertakal.Security.config;

import com.desertakal.desertakal.Security.handler.CustomAccessDeniedHandler;
import com.desertakal.desertakal.Security.handler.JwtAuthenticationEntryPoint;
import com.desertakal.desertakal.Security.handler.OAuth2SuccessHandler;
import com.desertakal.desertakal.Security.jwt.JwtAuthenticationFilter;
import com.desertakal.desertakal.config.brand.BrandInfo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final BrandInfo brandInfo;
    private final DeviceIdOAuth2Filter deviceIdOAuth2Filter;

    @Bean
    public SecurityFilterChain filterChain(@NonNull HttpSecurity http) {
        http
                .addFilterAfter(deviceIdOAuth2Filter, LogoutFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/guides/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tourists/*/reviews").permitAll()

                        .requestMatchers("/api/admins/**").hasRole("ADMIN")

                        .requestMatchers("/api/guides/**").hasAnyRole("ADMIN", "GUIDE", "TOURIST")
                        .requestMatchers("/api/tourists/**").hasAnyRole("ADMIN", "TOURIST")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl(brandInfo.getFrontendUrl() + "/auth/login?error=oauth2_cancelled")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
