package com.mymealserver.common.enums;

import lombok.Getter;

@Getter
public enum ProviderType {
    EMAIL("email"),
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String value;

    ProviderType(String value) {
        this.value = value;
    }

    public static ProviderType from(String value) {
        for (ProviderType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider type: " + value);
    }
}
