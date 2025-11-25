package br.fmu.projetoasthmaspace.Domain;

public class DiarioResponse {
    public Long id;
    public String data;
    public String horario;
    public String intensidade;
    public String descricao;

    public Long getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getHorario() {
        return horario;
    }

    public String getIntensidade() {
        return intensidade;
    }

    public String getDescricao() {
        return descricao;
    }
}
