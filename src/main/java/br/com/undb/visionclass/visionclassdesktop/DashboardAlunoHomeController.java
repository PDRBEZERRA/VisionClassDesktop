package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.*;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
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

        List<Turma> turmasDoAluno = turmaDAO.findAllByAlunoId(alunoLogado.getId());

        if (turmasDoAluno != null && !turmasDoAluno.isEmpty()) {

            List<Integer> meusSimuladosFeitos = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(alunoLogado.getId());

            List<Integer> todosSimuladosDasMinhasTurmasIds = new ArrayList<>();

            for (Turma turma : turmasDoAluno) {
                simuladoDAO.findSimuladosByTurmaId(turma.getId()).stream()
                        .map(Simulado::getId)
                        .forEach(todosSimuladosDasMinhasTurmasIds::add);
            }

            long totalSimuladosUnicos = todosSimuladosDasMinhasTurmasIds.stream().distinct().count();

            long disponiveis = totalSimuladosUnicos - simuladosFeitos;
            simuladosDisponiveisLabel.setText(String.valueOf(Math.max(0, disponiveis)));


        } else {
            simuladosDisponiveisLabel.setText("0");
        }
    }
}