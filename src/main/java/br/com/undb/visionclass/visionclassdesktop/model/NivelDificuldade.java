package br.com.undb.visionclass.visionclassdesktop.model;

public enum NivelDificuldade {
    FACIL("Fácil"),
    MEDIA("Média"),
    DIFICIL("Difícil");

    private final String descricao;

    NivelDificuldade(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}