package br.fmu.projetoasthmaspace.Domain;

public class DadosDetalhamentoCliente {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private int idade;
    private String sexo;
    private DadosEndereco endereco;
    private String problema_respiratorio;
    private String medicamentos;
    private String contatoEmergencia;


    // + outros campos que existirem na sua API

    public DadosDetalhamentoCliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public DadosEndereco getEndereco() {
        return endereco;
    }

    public void setEndereco(DadosEndereco endereco) {
        this.endereco = endereco;
    }

    public String getProblema_respiratorio() {
        return problema_respiratorio;
    }

    public void setProblema_respiratorio(String problema_respiratorio) {
        this.problema_respiratorio = problema_respiratorio;
    }

    public String getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(String medicamentos) {
        this.medicamentos = medicamentos;
    }

    public String getContatoEmergencia() {
        return contatoEmergencia;
    }

    public void setContatoEmergencia(String contatoEmergencia) {
        this.contatoEmergencia = contatoEmergencia;
    }
}
