package com.inteliagent.auth.entity;

public class Usuario {
    private String refreshToken;
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    private java.util.Map<String,Boolean> permissions = java.util.Map.of("createPlanning",false,"editPlanning",false,"addVisits",false,"reorderVisits",false);
    public java.util.Map<String,Boolean> getPermissions() { return permissions; }
    public void setPermissions(java.util.Map<String,Boolean> value) { permissions=value; }
    private String role = "AGENT";
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    private int tokenVersion;
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int value) { tokenVersion = value; }
    private int id;
    private String nome;
    private String email;
    private String senha;
    private byte[] securityPassword;
    private int idCede;
    private int situacaoAprovacao;
    private String codigoEmail;
    private int situacao;
    private String token;
    private int idImagemPerfilUsuario;
    private ImagemPerfilUsuario imagemPerfilUsuario;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getIdCede() {
        return idCede;
    }

    public void setIdCede(int idCede) {
        this.idCede = idCede;
    }

    public int getSituacaoAprovacao() {
        return situacaoAprovacao;
    }

    public void setSituacaoAprovacao(int situacaoAprovacao) {
        this.situacaoAprovacao = situacaoAprovacao;
    }

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    public String getCodigoEmail() {
        return codigoEmail;
    }

    public void setCodigoEmail(String codigoEmail) {
        this.codigoEmail = codigoEmail;
    }

    public int getSituacao() {
        return situacao;
    }

    public void setSituacao(int situacao) {
        this.situacao = situacao;
    }

    public ImagemPerfilUsuario getImagemPerfilUsuario() {
        return imagemPerfilUsuario;
    }

    public void setImagemPerfilUsuario(ImagemPerfilUsuario imagemPerfilUsuario) {
        this.imagemPerfilUsuario = imagemPerfilUsuario;
    }

    public int getIdImagemPerfilUsuario() {
        return idImagemPerfilUsuario;
    }

    public void setIdImagemPerfilUsuario(int idImagemPerfilUsuario) {
        this.idImagemPerfilUsuario = idImagemPerfilUsuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public byte[] getSecurityPassword() {
        return securityPassword;
    }

    public void setSecurityPassword(byte[] securityPassword) {
        this.securityPassword = securityPassword;
    }
}
