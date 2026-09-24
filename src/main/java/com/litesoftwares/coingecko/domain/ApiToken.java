package com.litesoftwares.coingecko.domain;

import com.litesoftwares.coingecko.constant.TokenType;

public class ApiToken {

    private TokenType type;

    private String value;

    public ApiToken(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public static ApiToken demo(String value) {
        return new ApiToken(TokenType.DEMO, value);
    }

    public static ApiToken pro(String value) {
        return new ApiToken(TokenType.PRO, value);
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
