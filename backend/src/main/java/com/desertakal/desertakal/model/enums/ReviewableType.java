package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum ReviewableType  {
    TOUR("Review on tour"),
    GUIDE("Review on guide");

    private final String desc;

    ReviewableType (String desc) {
        this.desc = desc;
    }
}