package br.com.undb.visionclass.visionclassdesktop.model;

import java.time.LocalDate;

public class AvaliacaoComportamental {
    private String id;
    private String alunoId;
    private String professorId;
    private LocalDate data;
    private int assiduidade;
    private int participacao;
    private int responsabilidade;
    private int sociabilidade;
    private String observacoes;

    public AvaliacaoComportamental() {
    }

    public AvaliacaoComportamental(String id, String alunoId, String professorId, LocalDate data, int assiduidade, int participacao, int responsabilidade, int sociabilidade, String observacoes) {
        this.id = id;
        this.alunoId = alunoId;
        this.professorId = professorId;
        this.data = data;
        this.assiduidade = assiduidade;
        this.participacao = participacao;
        this.responsabilidade = responsabilidade;
        this.sociabilidade = sociabilidade;
        this.observacoes = observacoes;
    }

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