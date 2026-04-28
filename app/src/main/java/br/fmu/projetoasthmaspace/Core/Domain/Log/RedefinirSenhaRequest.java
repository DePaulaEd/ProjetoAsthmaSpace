package br.fmu.projetoasthmaspace.Core.Domain.Log;


public class RedefinirSenhaRequest {
    private String tokenRedefinicao;
    private String novaSenha;

    public String getTokenRedefinicao() { return tokenRedefinicao; }
    public void setTokenRedefinicao(String t) { this.tokenRedefinicao = t; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String s) { this.novaSenha = s; }
}
