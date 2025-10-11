package br.com.undb.visionclass.visionclassdesktop.model;

import java.time.LocalDate;

public class AvaliacaoComportamental {

    private String id;
    private String alunoId;
    private String professorId;
    private String turmaId; // Adicionamos este campo
    private LocalDate data;
    private int assiduidade;
    private int participacao;
    private int responsabilidade;
    private int sociabilidade;
    private String observacoes;

    // Getters e Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public String getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(String turmaId) {
        this.turmaId = turmaId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getAssiduidade() {
        return assiduidade;
    }

    public void setAssiduidade(int assiduidade) {
        this.assiduidade = assiduidade;
    }

    public int getParticipacao() {
        return participacao;
    }

    public void setParticipacao(int participacao) {
        this.participacao = participacao;
    }

    public int getResponsabilidade() {
        return responsabilidade;
    }

    public void setResponsabilidade(int responsabilidade) {
        this.responsabilidade = responsabilidade;
    }

    public int getSociabilidade() {
        return sociabilidade;
    }

    public void setSociabilidade(int sociabilidade) {
        this.sociabilidade = sociabilidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}