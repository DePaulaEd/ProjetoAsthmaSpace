package br.fmu.projetoasthmaspace.Core.Domain.Cliente;


import br.fmu.projetoasthmaspace.Core.Domain.Endereco.Endereco;

public class DadosDetalhamentoCliente {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;

//    private int idade;
    private String dataNascimento;
    private String alergias;
    private String sexo;
    private Endereco endereco;
    private String problema_respiratorio;
    private String medicamentos;
    private String contatoEmergencia;

    public DadosDetalhamentoCliente() {}

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getProblema_respiratorio() { return problema_respiratorio; }
    public void setProblema_respiratorio(String problema_respiratorio) { this.problema_respiratorio = problema_respiratorio; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getContatoEmergencia() { return contatoEmergencia; }
    public void setContatoEmergencia(String contatoEmergencia) { this.contatoEmergencia = contatoEmergencia; }


}
