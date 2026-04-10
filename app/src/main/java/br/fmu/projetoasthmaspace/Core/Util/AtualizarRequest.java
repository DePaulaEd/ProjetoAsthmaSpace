package br.fmu.projetoasthmaspace.Core.Util;

import br.fmu.projetoasthmaspace.Core.Domain.Endereco.Endereco;

public class AtualizarRequest {
    public String nome;
    public String telefone;
    public String sexo;
    public Integer idade;
    public String medicamentos;

    public String contatoEmergencia;
    public String problema_respiratorio;
    public Endereco endereco;

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
