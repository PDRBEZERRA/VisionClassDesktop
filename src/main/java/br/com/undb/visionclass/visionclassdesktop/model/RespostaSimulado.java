package br.com.undb.visionclass.visionclassdesktop.model;

import java.time.LocalDateTime;
import java.util.Map;

public class RespostaSimulado {
    private String id;
    private String simuladoId;
    private String alunoId;
    private Map<String, Object> respostas;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFinalizacao;
    private double nota;
    private int acertos;

    public RespostaSimulado() {
    }

    public RespostaSimulado(String id, String simuladoId, String alunoId, Map<String, Object> respostas, LocalDateTime dataInicio, LocalDateTime dataFinalizacao, double nota, int acertos) {
        this.id = id;
        this.simuladoId = simuladoId;
        this.alunoId = alunoId;
        this.respostas = respostas;
        this.dataInicio = dataInicio;
        this.dataFinalizacao = dataFinalizacao;
        this.nota = nota;
        this.acertos = acertos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSimuladoId() {
        return simuladoId;
    }

    public void setSimuladoId(String simuladoId) {
        this.simuladoId = simuladoId;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public Map<String, Object> getRespostas() {
        return respostas;
    }

    public void setRespostas(Map<String, Object> respostas) {
        this.respostas = respostas;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }
}