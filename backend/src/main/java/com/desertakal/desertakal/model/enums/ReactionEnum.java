package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum ReactionEnum  {
    HEART("❤️"),
    LIKE("👍"),
    ANGRY("😡"),
    LAUGH("😂"),
    WOW("😮"),
    SAD("😢");

    private final String desc;

    ReactionEnum (String desc) {
        this.desc = desc;
    }
}