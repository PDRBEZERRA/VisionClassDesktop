package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO; // Importar TurmaDAO
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class TurmaCardController {

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private Label nomeProfessorLabel;
    @FXML
    private ProgressBar desempenhoProgressBar;
    @FXML
    private Label totalAlunosLabel;
    @FXML
    private Button verTurmaButton;

    private Turma turma;
    private UserDAO userDAO = new UserDAO();
    // --- INÍCIO DA ALTERAÇÃO ---
    private TurmaDAO turmaDAO = new TurmaDAO(); // Instância do TurmaDAO
    // --- FIM DA ALTERAÇÃO ---

    public void setData(Turma turma) {
        this.turma = turma;
        nomeTurmaLabel.setText(turma.getNome());
        desempenhoProgressBar.setProgress(turma.getDesempenho() / 100.0);

        // --- LÓGICA ATUALIZADA ---
        // Contar e exibir o total de alunos
        int total = turmaDAO.countAlunosByTurmaId(turma.getId());
        totalAlunosLabel.setText(total + (total == 1 ? " Aluno" : " Alunos"));

        if (turma.getProfessorId() != null && !turma.getProfessorId().isEmpty()) {
            User professor = userDAO.findById(turma.getProfessorId());
            if (professor != null) {
                nomeProfessorLabel.setText("Prof. " + professor.getNome());
            } else {
                nomeProfessorLabel.setText("Professor não encontrado");
            }
        } else {
            nomeProfessorLabel.setText("Professor não atribuído");
        }
    }

    @FXML
    void handleVerTurma(ActionEvent event) {
        DashboardProfessorController.getInstance().showDetalhesTurma(this.turma);
    }
}