package com.inteliagent.auth.dto.auth;

public class RefreshTokenRequest {
    private String refreshToken;

    // Construtores, getters e setters
    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
