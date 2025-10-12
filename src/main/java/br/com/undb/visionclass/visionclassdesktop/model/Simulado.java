package br.com.undb.visionclass.visionclassdesktop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Simulado {

    private int id;
    private String titulo;
    private LocalDate dataCriacao;
    private StatusSimulado status;
    private String professorCriadorId;

    // Listas para armazenar os relacionamentos
    private List<Questao> questoes = new ArrayList<>();
    private List<Turma> turmas = new ArrayList<>();

    // Getters e Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public StatusSimulado getStatus() {
        return status;
    }

    public void setStatus(StatusSimulado status) {
        this.status = status;
    }

    public String getProfessorCriadorId() {
        return professorCriadorId;
    }

    public void setProfessorCriadorId(String professorCriadorId) {
        this.professorCriadorId = professorCriadorId;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void setTurmas(List<Turma> turmas) {
        this.turmas = turmas;
    }
}