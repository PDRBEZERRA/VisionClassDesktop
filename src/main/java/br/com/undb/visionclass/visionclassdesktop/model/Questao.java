package br.com.undb.visionclass.visionclassdesktop.model;

import java.util.ArrayList;
import java.util.List;

public class Questao {
    private int id;
    private String enunciado;
    private TipoQuestao tipo;
    private NivelDificuldade nivelDificuldade;
    private int disciplinaId;
    private int assuntoId;
    private String professorCriadorId;
    private List<Alternativa> alternativas = new ArrayList<>();

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public NivelDificuldade getNivelDificuldade() {
        return nivelDificuldade;
    }

    public void setNivelDificuldade(NivelDificuldade nivelDificuldade) {
        this.nivelDificuldade = nivelDificuldade;
    }

    public int getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(int disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public int getAssuntoId() {
        return assuntoId;
    }

    public void setAssuntoId(int assuntoId) {
        this.assuntoId = assuntoId;
    }

    public String getProfessorCriadorId() {
        return professorCriadorId;
    }

    public void setProfessorCriadorId(String professorCriadorId) {
        this.professorCriadorId = professorCriadorId;
    }

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }
}