package br.fmu.projetoasthmaspace.Domain;

public class LembreteRequest {
    public String titulo;
    public String data;
    public String horario;

    public LembreteRequest(String titulo, String data, String horario) {
        this.titulo = titulo;
        this.data = data;
        this.horario = horario;
    }
}


