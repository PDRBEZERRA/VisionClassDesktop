package br.com.undb.visionclass.visionclassdesktop.model;

public enum TipoQuestao {
    MULTIPLA_ESCOLHA("Múltipla Escolha"),
    DISCURSIVA("Discursiva");

    private final String descricao;

    TipoQuestao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}