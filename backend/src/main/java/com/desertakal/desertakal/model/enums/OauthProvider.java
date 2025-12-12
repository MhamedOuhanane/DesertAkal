package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum OauthProvider {

    LOCAL("Signed up with email and password"),
    GOOGLE("Signed in with Google"),
    FACEBOOK("Signed in with Facebook"),
    DISCORD("Signed in with Discord");

    private final String descr;

    OauthProvider(String description) {
        this.descr = description;
    }
}

