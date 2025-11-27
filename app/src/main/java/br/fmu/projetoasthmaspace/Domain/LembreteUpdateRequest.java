package br.fmu.projetoasthmaspace.Domain;

public class LembreteUpdateRequest {
    public Long id;
    public boolean concluido;

    public String titulo;
    public String data;
    public String horario;

    public LembreteUpdateRequest(Long id, boolean concluido) {
        this.id = id;
        this.concluido = concluido;
    }
    public LembreteUpdateRequest(Long id, String titulo, String data, String horario, boolean concluido) {
        this.id = id;
        this.titulo = titulo;
        this.data = data;
        this.horario = horario;
        this.concluido = concluido;
    }
    public LembreteUpdateRequest(Long id) {
        this.id = id;
    }

    public LembreteUpdateRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

