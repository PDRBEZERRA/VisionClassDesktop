package br.com.undb.visionclass.visionclassdesktop.model;

import java.time.LocalDateTime;
import java.util.List;

public class Simulado {
    private String id;
    private String nome;
    private List<String> turmasIds;
    private List<String> questoesIds;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private int duracao;
    private String professorId;
    private StatusSimulado status;

    public Simulado() {
    }

    public Simulado(String id, String nome, List<String> turmasIds, List<String> questoesIds, LocalDateTime dataInicio, LocalDateTime dataFim, int duracao, String professorId, StatusSimulado status) {
        this.id = id;
        this.nome = nome;
        this.turmasIds = turmasIds;
        this.questoesIds = questoesIds;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.duracao = duracao;
        this.professorId = professorId;
        this.status = status;
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

    public List<String> getTurmasIds() {
        return turmasIds;
    }

    public void setTurmasIds(List<String> turmasIds) {
        this.turmasIds = turmasIds;
    }

    public List<String> getQuestoesIds() {
        return questoesIds;
    }

    public void setQuestoesIds(List<String> questoesIds) {
        this.questoesIds = questoesIds;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public StatusSimulado getStatus() {
        return status;
    }

    public void setStatus(StatusSimulado status) {
        this.status = status;
    }
}