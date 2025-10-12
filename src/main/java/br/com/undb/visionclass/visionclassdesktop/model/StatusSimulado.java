package br.com.undb.visionclass.visionclassdesktop.model;

public enum StatusSimulado {
    RASCUNHO("Rascunho"),
    PUBLICADO("Publicado"),
    CONCLUIDO("Concluído");

    private final String descricao;

    StatusSimulado(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}