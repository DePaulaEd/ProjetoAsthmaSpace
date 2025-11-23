package br.fmu.projetoasthmaspace.Domain;

public class DiarioRequest {
    public String intensidade;
    public String descricao;

    public String data;
    public String horario;

    public DiarioRequest(String data, String horario, String intensidade, String descricao) {
        this.data = data;
        this.horario = horario;
        this.intensidade = intensidade;
        this.descricao = descricao;
    }

    public DiarioRequest() {}
}

