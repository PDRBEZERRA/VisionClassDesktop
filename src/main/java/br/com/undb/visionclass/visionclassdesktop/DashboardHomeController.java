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
    // private TurmaDAO turmaDAO = new TurmaDAO(); // Futuramente

    @FXML
    public void initialize() {
        // Busca os números no banco de dados usando o DAO
        int numAlunos = userDAO.countByRole(UserRole.ALUNO);
        int numProfessores = userDAO.countByRole(UserRole.PROFESSOR);
        // int numTurmas = turmaDAO.countAll(); // Futuramente

        // Atualiza os labels na tela com os valores encontrados
        totalAlunosLabel.setText(String.valueOf(numAlunos));
        totalProfessoresLabel.setText(String.valueOf(numProfessores));
        totalTurmasLabel.setText("0"); // Placeholder
        totalSimuladosLabel.setText("0"); // Placeholder
    }
}