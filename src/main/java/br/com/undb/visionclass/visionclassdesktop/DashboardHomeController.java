package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardHomeController {

    @FXML
    private Label totalAlunosLabel;
    @FXML
    private Label totalProfessoresLabel;
    @FXML
    private Label totalTurmasLabel;
    @FXML
    private Label totalSimuladosLabel;

    private UserDAO userDAO = new UserDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private DashboardController mainController;

    @FXML
    public void initialize() {
        int numAlunos = userDAO.countByRole(UserRole.ALUNO);
        int numProfessores = userDAO.countByRole(UserRole.PROFESSOR);
        int numTurmas = turmaDAO.countAll();

        totalAlunosLabel.setText(String.valueOf(numAlunos));
        totalProfessoresLabel.setText(String.valueOf(numProfessores));
        totalTurmasLabel.setText(String.valueOf(numTurmas));
        totalSimuladosLabel.setText("0");
    }

    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onCadastrarUsuarioClick() {
        if (mainController != null) {
            mainController.onGerirUsuariosClick(null);
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardHomeController.");
        }
    }

    @FXML
    private void onCriarTurmaClick() {
        if (mainController != null) {
            mainController.onGerirTurmasClick(null);
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardHomeController.");
        }
    }

    @FXML
    private void onVerRelatoriosClick(ActionEvent event) {
        if (mainController != null) {
            mainController.onRelatoriosClick(event);
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardHomeController.");
        }
    }
}
