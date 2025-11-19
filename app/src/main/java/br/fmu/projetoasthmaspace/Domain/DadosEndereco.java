package br.fmu.projetoasthmaspace.Domain;

public class DadosEndereco {
    public String logradouro;
    public String bairro;
    public String cep;
    public String complemento;
    public String numero;
    public String uf;
    public String cidade;

    public DadosEndereco(String logradouro, String bairro, String cep, String complemento,
                         String numero, String uf, String cidade) {
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cep = cep;
        this.complemento = complemento;
        this.numero = numero;
        this.uf = uf;
        this.cidade = cidade;
    }
}