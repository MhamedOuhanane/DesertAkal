package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("User is active"),
    SUSPENDED("User is suspended"),
    BANNED("User is banned");

    private final String desc;

    UserStatus(String desc) {
        this.desc = desc;
    }
}
