package com.litesoftwares.coingecko.constant;

public enum TokenType {
    DEMO("x-cg-demo-api-key"),
    PRO("x-cg-pro-api-key");

    private final String headerName;

    TokenType(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
