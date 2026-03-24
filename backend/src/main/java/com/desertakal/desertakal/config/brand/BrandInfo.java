package com.desertakal.desertakal.config.brand;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BrandInfo {

    public static final String COMPANY_NAME = "Desert Akal";
    public static final String TAGLINE = "Travel & Desert Adventures";
    public static final String EMAIL = "contact@desertakal.app";
    public static final String PHONE = "+212 600 000 000";
    public static final String DOMAIN = "desertakal.app";
    public static final String LOGO_PATH = "public/desert_akal_logo.png";
    public static final String DISCLAIMER =
            "This document is electronically generated and valid without signature.";

    @Value("${app.frontend.url:https://desertakal.com}")
    private String frontendUrl;

    @Value("${app.api.base-url:https://api.desertakal.app}")
    private String apiUrl;

    @Value("${app.minio.external-url:http://localhost:9000}")
    private String MinioUrl;

    public String getFullWebsite() {
        return "www." + DOMAIN;
    }

    public String getContactLine() {
        return EMAIL + "  |  " + getFullWebsite();
    }
}