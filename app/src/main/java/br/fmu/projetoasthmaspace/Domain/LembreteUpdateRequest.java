package br.fmu.projetoasthmaspace.Domain;

public class LembreteUpdateRequest {
    public Long id;
    public boolean concluido;

    public LembreteUpdateRequest(Long id, boolean concluido) {
        this.id = id;
        this.concluido = concluido;
    }
}

