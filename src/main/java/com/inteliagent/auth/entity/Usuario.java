package com.inteliagent.auth.entity;

public class Usuario {
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

    public byte[] getSecurityPassword() {
        return securityPassword;
    }

    public void setSecurityPassword(byte[] securityPassword) {
        this.securityPassword = securityPassword;
    }
}
