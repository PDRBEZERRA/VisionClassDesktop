package br.com.undb.visionclass.visionclassdesktop.model;

public class DashboardStats {
    private int totalAlunos;
    private int totalProfessores;
    private int turmasAtivas;
    private int simuladosAtivos;

    public DashboardStats() {
    }

    public DashboardStats(int totalAlunos, int totalProfessores, int turmasAtivas, int simuladosAtivos) {
        this.totalAlunos = totalAlunos;
        this.totalProfessores = totalProfessores;
        this.turmasAtivas = turmasAtivas;
        this.simuladosAtivos = simuladosAtivos;
    }

    public int getTotalAlunos() {
        return totalAlunos;
    }

    public void setTotalAlunos(int totalAlunos) {
        this.totalAlunos = totalAlunos;
    }

    public int getTotalProfessores() {
        return totalProfessores;
    }

    public void setTotalProfessores(int totalProfessores) {
        this.totalProfessores = totalProfessores;
    }

    public int getTurmasAtivas() {
        return turmasAtivas;
    }

    public void setTurmasAtivas(int turmasAtivas) {
        this.turmasAtivas = turmasAtivas;
    }

    public int getSimuladosAtivos() {
        return simuladosAtivos;
    }

    public void setSimuladosAtivos(int simuladosAtivos) {
        this.simuladosAtivos = simuladosAtivos;
    }
}