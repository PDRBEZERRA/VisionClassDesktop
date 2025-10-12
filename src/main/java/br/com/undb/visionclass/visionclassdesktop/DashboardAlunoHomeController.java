package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.*; // Importação Coringa
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.util.List;

public class DashboardAlunoHomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label desempenhoComportamentalLabel;
    @FXML
    private Label simuladosDisponiveisLabel;
    @FXML
    private Label simuladosRealizadosLabel;
    @FXML
    private Label mediaGeralLabel;
    @FXML
    private Label posicaoTurmaLabel;

    // --- DAOs INSTANCIADOS ---
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();
    private UserDAO userDAO = new UserDAO(); // Novo DAO
    private User alunoLogado;

    @FXML
    public void initialize() {
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        if (alunoLogado != null) {
            welcomeLabel.setText("Bem-vindo(a), " + alunoLogado.getNome().split(" ")[0] + "!");
        }

        loadDashboardData();
    }

    private void loadDashboardData() {
        if (alunoLogado == null) return;

        // --- Desempenho Comportamental ---
        String mediaComportamental = avaliacaoDAO.getMediaGeralByAlunoId(alunoLogado.getId());
        desempenhoComportamentalLabel.setText(mediaComportamental);

        // --- Simulados Realizados ---
        int simuladosFeitos = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(alunoLogado.getId()).size();
        simuladosRealizadosLabel.setText(String.valueOf(simuladosFeitos));

        // --- Média Geral (Simulados) ---
        double minhaMedia = alunoRespostaDAO.getMediaGeralSimulados(alunoLogado.getId());
        if (minhaMedia >= 0) {
            DecimalFormat df = new DecimalFormat("#.##");
            mediaGeralLabel.setText(df.format(minhaMedia));
        } else {
            mediaGeralLabel.setText("N/A");
        }

        // --- Posição na Turma e Simulados Disponíveis ---
        Turma turmaDoAluno = turmaDAO.findByAlunoId(alunoLogado.getId());
        if (turmaDoAluno != null) {
            // Simulados Disponíveis
            int totalSimuladosNaTurma = simuladoDAO.countByTurmaId(turmaDoAluno.getId());
            simuladosDisponiveisLabel.setText(String.valueOf(totalSimuladosNaTurma));

            // Lógica de Ranking
            List<User> colegasDeTurma = userDAO.findAlunosByTurmaId(turmaDoAluno.getId());
            int posicao = 1;
            int totalAlunosNaTurma = colegasDeTurma.size();

            if (minhaMedia >= 0) { // Só calcula a posição se o aluno tiver uma média
                for (User colega : colegasDeTurma) {
                    if (!colega.getId().equals(alunoLogado.getId())) { // Não se compara com si mesmo
                        double mediaColega = alunoRespostaDAO.getMediaGeralSimulados(colega.getId());
                        if (mediaColega > minhaMedia) {
                            posicao++;
                        }
                    }
                }
                posicaoTurmaLabel.setText(posicao + "º de " + totalAlunosNaTurma);
            } else {
                posicaoTurmaLabel.setText("-"); // Não tem posição se não fez simulados
            }

        } else {
            // Caso o aluno não esteja em nenhuma turma
            simuladosDisponiveisLabel.setText("0");
            posicaoTurmaLabel.setText("N/A");
        }
    }
}