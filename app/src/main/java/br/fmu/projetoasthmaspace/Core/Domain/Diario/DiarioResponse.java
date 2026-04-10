package br.fmu.projetoasthmaspace.Core.Domain.Diario;

public class DiarioResponse {
    public Long id;
    public String data;
    public String horario;
    public String intensidade;
    public String descricao;

    public Long getId() { return id; }
    public String getData() { return data; }
    public String getIntensidade() { return intensidade; }
    public String getDescricao() { return descricao; }

    public String getHorario() { return horario; }

    public String getHorarioFormatado() {
        if (horario == null) return "";
        return horario.length() >= 5 ? horario.substring(0, 5) : horario;
    }
}