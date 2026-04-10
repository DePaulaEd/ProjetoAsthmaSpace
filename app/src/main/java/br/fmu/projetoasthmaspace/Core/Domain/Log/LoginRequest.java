package br.fmu.projetoasthmaspace.Core.Domain.Log;

public class LoginRequest {
    public String login;
    public String senha;

    public LoginRequest(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }
}
