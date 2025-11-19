package br.fmu.projetoasthmaspace.Domain;

public class LoginRequest {
    public String login;
    public String senha;

    public LoginRequest(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }
}
