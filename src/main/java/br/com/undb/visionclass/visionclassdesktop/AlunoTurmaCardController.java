package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class AlunoTurmaCardController {

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private Label nomeProfessorLabel;
    @FXML
    private Label mediaLabel;
    @FXML
    private Label posicaoLabel;
    @FXML
    private Label simuladosLabel;

    private UserDAO userDAO = new UserDAO();
    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    private User aluno;
    private Turma turma;

    public void setData(User aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;

        nomeTurmaLabel.setText(turma.getNome());

        if (turma.getProfessorId() != null && !turma.getProfessorId().isEmpty()) {
            User professor = userDAO.findById(turma.getProfessorId());
            nomeProfessorLabel.setText(professor != null ? "Prof. " + professor.getNome() : "Prof. não atribuído");
        } else {
            nomeProfessorLabel.setText("Prof. não atribuído");
        }

        calcularDesempenhoNaTurma();
    }

    private void calcularDesempenhoNaTurma() {
        List<Simulado> simuladosDaTurma = simuladoDAO.findSimuladosByTurmaId(turma.getId());
        List<Integer> simuladosDaTurmaIds = simuladosDaTurma.stream()
                .map(Simulado::getId)
                .collect(Collectors.toList());

        int totalSimuladosTurma = simuladosDaTurmaIds.size();

        List<Integer> meusSimuladosFeitosIds = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(aluno.getId());

        long simuladosFeitosNestaTurma = meusSimuladosFeitosIds.stream()
                .filter(simuladosDaTurmaIds::contains)
                .count();

        simuladosLabel.setText("Simulados: " + simuladosFeitosNestaTurma + "/" + totalSimuladosTurma);

        double minhaMediaNaTurma = alunoRespostaDAO.getMediaGeralSimuladosPorLista(aluno.getId(), simuladosDaTurmaIds);

        if (minhaMediaNaTurma >= 0) {
            DecimalFormat df = new DecimalFormat("0.0");
            mediaLabel.setText(df.format(minhaMediaNaTurma) + "/10");

            List<User> colegas = userDAO.findAlunosByTurmaId(turma.getId());
            int minhaPosicao = 1;
            for (User colega : colegas) {
                if (!colega.getId().equals(aluno.getId())) {
                    double mediaColega = alunoRespostaDAO.getMediaGeralSimuladosPorLista(colega.getId(), simuladosDaTurmaIds);
                    if (mediaColega > minhaMediaNaTurma) {
                        minhaPosicao++;
                    }
                }
            }
            posicaoLabel.setText(minhaPosicao + "º de " + colegas.size());

        } else {
            mediaLabel.setText("-");
            posicaoLabel.setText("-");
        }
    }
}