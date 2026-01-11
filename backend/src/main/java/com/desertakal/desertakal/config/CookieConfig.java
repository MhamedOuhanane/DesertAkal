package com.desertakal.desertakal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CookieConfig {
    @Value("${security.cookie.secure:false}")
    private boolean secure;

    @Value("${security.cookie.same-site:Strict}")
    private String sameSite;

    public int getMaxAge() {
        return 30 * 24 * 60 * 60;
    }
}
