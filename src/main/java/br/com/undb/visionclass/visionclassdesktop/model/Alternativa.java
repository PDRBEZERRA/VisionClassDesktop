package br.com.undb.visionclass.visionclassdesktop.model;

public class Alternativa {
    private int id;
    private String texto;
    private boolean correta;
    private int questaoId;

    public Alternativa(String texto, boolean correta) {
        this.texto = texto;
        this.correta = correta;
    }

    public Alternativa() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public int getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(int questaoId) {
        this.questaoId = questaoId;
    }
}