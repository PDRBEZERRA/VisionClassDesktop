package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
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
    private DashboardController mainController;

    @FXML
    public void initialize() {
        int numAlunos = userDAO.countByRole(UserRole.ALUNO);
        int numProfessores = userDAO.countByRole(UserRole.PROFESSOR);

        totalAlunosLabel.setText(String.valueOf(numAlunos));
        totalProfessoresLabel.setText(String.valueOf(numProfessores));
        totalTurmasLabel.setText("0");
        totalSimuladosLabel.setText("0");
    }

    public void setMainController(DashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onCadastrarUsuarioClick() {
        if (mainController != null) {
            mainController.onGerirUsuariosClick();
        }
    }
}