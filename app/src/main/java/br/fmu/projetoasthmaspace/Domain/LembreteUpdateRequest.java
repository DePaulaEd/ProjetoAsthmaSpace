package br.fmu.projetoasthmaspace.Domain;

public class LembreteUpdateRequest {

    public boolean concluido;
    public String titulo;
    public String data;
    public String horario;

    public LembreteUpdateRequest(boolean concluido) {
        this.concluido = concluido;
    }

    public LembreteUpdateRequest(String titulo, String data, String horario, boolean concluido) {
        this.titulo = titulo;
        this.data = data;
        this.horario = horario;
        this.concluido = concluido;
    }

    public LembreteUpdateRequest() {}
}
