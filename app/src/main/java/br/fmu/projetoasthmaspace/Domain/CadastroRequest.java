package br.fmu.projetoasthmaspace.Domain;

public class CadastroRequest {
    public String nome;
    public String email;
    public String telefone;
    public String cpf;
    public String senha;

    Endereco endereco;
    String problema_respiratorio;
    String medicamentos;
    String alergias;
    String contatoEmergencia;

    public CadastroRequest(String nome, String email, String telefone, String cpf, String senha,
    Endereco endereco, String problema_respiratorio, String alergias, String contatoEmergencia, String medicamentos) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.endereco = endereco;
        this.problema_respiratorio = problema_respiratorio;
        this.alergias = alergias;
        this.contatoEmergencia = contatoEmergencia;
        this.medicamentos = medicamentos;

    }
}
