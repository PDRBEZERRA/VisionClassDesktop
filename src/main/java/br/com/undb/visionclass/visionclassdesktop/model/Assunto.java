package br.com.undb.visionclass.visionclassdesktop.model;

public class Assunto {
    private int id;
    private String nome;
    private int disciplinaId;

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(int disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    // Usado para exibir o nome do assunto em ComboBoxes
    @Override
    public String toString() {
        return nome;
    }
}