package br.fmu.projetoasthmaspace.Core.Util;

import br.fmu.projetoasthmaspace.Core.Domain.Endereco.Endereco;

public class AtualizarRequest {
    public String nome;
    public String telefone;
    public String sexo;
    public String dataNascimento;
    public String medicamentos;

    public String contatoEmergencia;
    public String problema_respiratorio;
    public Endereco endereco;
    public String cpf;


    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
