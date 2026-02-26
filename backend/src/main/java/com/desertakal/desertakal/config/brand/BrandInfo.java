package com.desertakal.desertakal.config.brand;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Desert Akal Brand Information
 * المعلومات اللي كنستعملوهم في PDF + Emails + كل البلايص
 */
@Component
@Getter
public class BrandInfo {

    public static final String COMPANY_NAME = "Desert Akal";
    public static final String TAGLINE = "Travel & Desert Adventures";
    public static final String EMAIL = "contact@desertakal.com";
    public static final String PHONE = "+212 600 000 000";
    public static final String DOMAIN = "desertakal.com";
    public static final String LOGO_PATH = "public/desert_akal_logo.png";
    public static final String DISCLAIMER =
            "This document is electronically generated and valid without signature.";

    @Value("${app.frontend.url:https://desertakal.com}")
    private String frontendUrl;

    @Value("${app.api.base-url:https://api.desertakal.com}")
    private String apiUrl;

    @Value("${minio.url:http://localhost:9000}")
    private String MinioUrl;

    public String getFullWebsite() {
        return "www." + DOMAIN;
    }

    public String getContactLine() {
        return EMAIL + "  |  " + getFullWebsite();
    }
}