package com.inteliagent.auth.dto.auth;

public class LoginRequest {
    private String username;
    private String password;

    // Construtores, getters e setters
    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}