package br.com.undb.visionclass.visionclassdesktop.model;

import java.util.List;

public class Questao {
    private String id;
    private String disciplina;
    private String tema;
    private NivelDificuldade dificuldade;
    private TipoQuestao tipo;
    private String enunciado;
    private List<String> alternativas;
    private int alternativaCorreta;
    private boolean respostaCorreta;
    private List<String> tags;
    private String professorId;

    public Questao() {
    }

    public Questao(String id, String disciplina, String tema, NivelDificuldade dificuldade, TipoQuestao tipo, String enunciado, List<String> alternativas, int alternativaCorreta, boolean respostaCorreta, List<String> tags, String professorId) {
        this.id = id;
        this.disciplina = disciplina;
        this.tema = tema;
        this.dificuldade = dificuldade;
        this.tipo = tipo;
        this.enunciado = enunciado;
        this.alternativas = alternativas;
        this.alternativaCorreta = alternativaCorreta;
        this.respostaCorreta = respostaCorreta;
        this.tags = tags;
        this.professorId = professorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public NivelDificuldade getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(NivelDificuldade dificuldade) {
        this.dificuldade = dificuldade;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }

    public int getAlternativaCorreta() {
        return alternativaCorreta;
    }

    public void setAlternativaCorreta(int alternativaCorreta) {
        this.alternativaCorreta = alternativaCorreta;
    }

    public boolean isRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(boolean respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }
}