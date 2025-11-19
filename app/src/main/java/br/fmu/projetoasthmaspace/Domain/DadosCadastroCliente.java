package br.fmu.projetoasthmaspace.Domain;

public class DadosCadastroCliente {
    public String nome;
    public String email;
    public String telefone;
    public String cpf;
    public String senha;
    public Integer idade;
    public String sexo;

    public DadosEndereco endereco;

    public String problema_respiratorio;
    public String medicamentos;

    public String contatoEmergencia;

    public DadosCadastroCliente(String nome, String email, String telefone, String cpf, String senha,
                                Integer idade, String sexo, DadosEndereco endereco,
                                String problema_respiratorio, String medicamentos,
                                String contatoEmergencia) {

        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.idade = idade;
        this.sexo = sexo;
        this.endereco = endereco;
        this.problema_respiratorio = problema_respiratorio;
        this.medicamentos = medicamentos;
        this.contatoEmergencia = contatoEmergencia;
    }
    public DadosCadastroCliente() {}
}
