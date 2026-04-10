package br.fmu.projetoasthmaspace.Core.Util;

public class AlterarSenhaRequest {
    private String senhaAtual;
    private String novaSenha;
    private String confirmarSenha;

    public AlterarSenhaRequest(String senhaAtual, String novaSenha, String confirmarSenha) {
        this.senhaAtual      = senhaAtual;
        this.novaSenha       = novaSenha;
        this.confirmarSenha  = confirmarSenha;
    }

    public String getSenhaAtual()     { return senhaAtual; }
    public String getNovaSenha()      { return novaSenha; }
    public String getConfirmarSenha() { return confirmarSenha; }
}
