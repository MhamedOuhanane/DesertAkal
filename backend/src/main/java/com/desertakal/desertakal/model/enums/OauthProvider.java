package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum OauthProvider {

    LOCAL("Email & Password"),
    GOOGLE("Google Account"),
    FACEBOOK("Facebook Account"),
    DISCORD("Discord Account");

    private final String descr;

    OauthProvider(String description) {
        this.descr = description;
    }
}

