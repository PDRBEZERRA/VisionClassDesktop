package br.com.undb.visionclass.visionclassdesktop.model;

import java.util.List;

public class Turma {
    private String id;
    private String nome;
    private String ano;
    private String periodo;
    private String professorId;
    private List<String> alunosIds;
    private double desempenho;

    public Turma() {
    }

    public Turma(String id, String nome, String ano, String periodo, String professorId, List<String> alunosIds, double desempenho) {
        this.id = id;
        this.nome = nome;
        this.ano = ano;
        this.periodo = periodo;
        this.professorId = professorId;
        this.alunosIds = alunosIds;
        this.desempenho = desempenho;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public List<String> getAlunosIds() {
        return alunosIds;
    }

    public void setAlunosIds(List<String> alunosIds) {
        this.alunosIds = alunosIds;
    }

    public double getDesempenho() {
        return desempenho;
    }

    public void setDesempenho(double desempenho) {
        this.desempenho = desempenho;
    }
}