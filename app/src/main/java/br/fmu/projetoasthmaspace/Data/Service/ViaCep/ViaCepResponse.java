package br.fmu.projetoasthmaspace.Data.Service.ViaCep;

public class ViaCepResponse {
    public String logradouro;
    public String bairro;
    public String localidade; // cidade
    public String uf;
    public String complemento;
    public String erro;       // "true" se CEP não encontrado
}
