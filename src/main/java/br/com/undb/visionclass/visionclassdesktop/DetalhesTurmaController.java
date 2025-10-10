package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DetalhesTurmaController {

    private DashboardProfessorController dashboardController;
    private UserDAO userDAO = new UserDAO(); // Instância do DAO

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private Label detalhesTurmaLabel;
    @FXML
    private Label totalAlunosLabel;
    @FXML
    private Label professorResponsavelLabel;
    @FXML
    private TableView<Object> alunosTableView;
    @FXML
    private TableColumn<Object, String> matriculaColumn;
    @FXML
    private TableColumn<Object, String> nomeColumn;
    @FXML
    private TableColumn<Object, String> emailColumn;
    @FXML
    private TableColumn<Object, Double> mediaColumn;
    @FXML
    private TableColumn<Object, Void> acoesColumn;

    private Turma turmaAtual;

    public void setDashboardController(DashboardProfessorController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setTurma(Turma turma) {
        this.turmaAtual = turma;
        nomeTurmaLabel.setText("Turma: " + turma.getNome());
        detalhesTurmaLabel.setText("Detalhes e gerenciamento da turma ID: " + turma.getId());
        totalAlunosLabel.setText("0"); // Placeholder

        // --- LÓGICA ATUALIZADA ---
        if (turma.getProfessorId() != null && !turma.getProfessorId().isEmpty()) {
            User professor = userDAO.findById(turma.getProfessorId());
            if (professor != null) {
                professorResponsavelLabel.setText(professor.getNome());
            } else {
                professorResponsavelLabel.setText("Não encontrado");
            }
        } else {
            professorResponsavelLabel.setText("Não atribuído");
        }
    }

    @FXML
    public void initialize() {
        // Configuração da tabela virá aqui
    }

    @FXML
    void handleVoltarParaTurmas(ActionEvent event) {
        if (dashboardController != null) {
            dashboardController.onMinhasTurmasClick();
        }
    }
}