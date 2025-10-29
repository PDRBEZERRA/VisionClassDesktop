package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.*;
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
    private Label assiduidadeMediaLabel;
    @FXML
    private Label participacaoMediaLabel;
    @FXML
    private Label responsabilidadeMediaLabel;
    @FXML
    private Label sociabilidadeMediaLabel;

    @FXML
    private Label simuladosDisponiveisLabel;
    @FXML
    private Label simuladosRealizadosLabel;
    @FXML
    private Label mediaGeralLabel;
    @FXML
    private Label posicaoTurmaLabel;

    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();
    private UserDAO userDAO = new UserDAO();
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

        assiduidadeMediaLabel.setText(avaliacaoDAO.getMediaPorDimensao(alunoLogado.getId(), "assiduidade"));
        participacaoMediaLabel.setText(avaliacaoDAO.getMediaPorDimensao(alunoLogado.getId(), "participacao"));
        responsabilidadeMediaLabel.setText(avaliacaoDAO.getMediaPorDimensao(alunoLogado.getId(), "responsabilidade"));
        sociabilidadeMediaLabel.setText(avaliacaoDAO.getMediaPorDimensao(alunoLogado.getId(), "sociabilidade"));
        int simuladosFeitos = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(alunoLogado.getId()).size();
        simuladosRealizadosLabel.setText(String.valueOf(simuladosFeitos));

        double minhaMedia = alunoRespostaDAO.getMediaGeralSimulados(alunoLogado.getId());
        if (minhaMedia >= 0) {
            DecimalFormat df = new DecimalFormat("0.0");
            mediaGeralLabel.setText(df.format(minhaMedia));
        } else {
            mediaGeralLabel.setText("-");
        }

        Turma turmaDoAluno = turmaDAO.findByAlunoId(alunoLogado.getId());
        if (turmaDoAluno != null) {
            int totalSimuladosNaTurma = simuladoDAO.countByTurmaId(turmaDoAluno.getId());
            int disponiveis = Math.max(0, totalSimuladosNaTurma - simuladosFeitos);
            simuladosDisponiveisLabel.setText(String.valueOf(disponiveis));

            List<User> colegasDeTurma = userDAO.findAlunosByTurmaId(turmaDoAluno.getId());
            int posicao = 1;
            int totalAlunosNaTurma = colegasDeTurma.size();

            if (minhaMedia >= 0) {
                for (User colega : colegasDeTurma) {
                    if (!colega.getId().equals(alunoLogado.getId())) {
                        double mediaColega = alunoRespostaDAO.getMediaGeralSimulados(colega.getId());
                        if (mediaColega > minhaMedia) {
                            posicao++;
                        }
                    }
                }
                posicaoTurmaLabel.setText(posicao + "º de " + totalAlunosNaTurma);
            } else {
                posicaoTurmaLabel.setText("-");
            }

        } else {
            simuladosDisponiveisLabel.setText("0");
            posicaoTurmaLabel.setText("N/A");
        }
    }
}