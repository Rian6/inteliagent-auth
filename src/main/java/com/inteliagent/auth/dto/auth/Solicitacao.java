package com.inteliagent.auth.dto.auth;

public class Solicitacao {
    private String situacao;
    private int idCede;

    // Getters and Setters
    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public int getIdCede() {
        return idCede;
    }

    public void setIdCede(int idCede) {
        this.idCede = idCede;
    }

    @Override
    public String toString() {
        return "Solicitacao{" +
                "situacao='" + situacao + '\'' +
                ", idCede=" + idCede +
                '}';
    }
}
