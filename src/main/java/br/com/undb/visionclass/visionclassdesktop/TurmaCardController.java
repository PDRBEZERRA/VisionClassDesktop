package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.Turma;
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

    public void setData(Turma turma) {
        nomeTurmaLabel.setText(turma.getNome());
        nomeProfessorLabel.setText("Prof. ID: " + turma.getProfessorId());
        desempenhoProgressBar.setProgress(turma.getDesempenho() / 100.0);
        totalAlunosLabel.setText("0 Alunos"); // Placeholder
    }
}