package br.com.undb.visionclass.visionclassdesktop.model;

public class Disciplina {
    private int id;
    private String nome;

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

    // Usado para exibir o nome da disciplina em ComboBoxes
    @Override
    public String toString() {
        return nome;
    }
}