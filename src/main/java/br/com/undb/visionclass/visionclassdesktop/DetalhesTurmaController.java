package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

public class DetalhesTurmaController {

    private DashboardProfessorController dashboardController;
    private UserDAO userDAO = new UserDAO();
    // --- INÍCIO DAS ALTERAÇÕES ---
    private TurmaDAO turmaDAO = new TurmaDAO(); // Instanciar TurmaDAO
    // --- FIM DAS ALTERAÇÕES ---

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private Label detalhesTurmaLabel;
    @FXML
    private Label totalAlunosLabel;
    @FXML
    private Label professorResponsavelLabel;

    // --- INÍCIO DAS ALTERAÇÕES ---
    // A tabela agora espera objetos do tipo User
    @FXML
    private TableView<User> alunosTableView;
    @FXML
    private TableColumn<User, String> matriculaColumn;
    @FXML
    private TableColumn<User, String> nomeColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, Double> mediaColumn;
    @FXML
    private TableColumn<User, Void> acoesColumn;
    // --- FIM DAS ALTERAÇÕES ---

    private Turma turmaAtual;
    private ObservableList<User> alunosDaTurma = FXCollections.observableArrayList();

    public void setDashboardController(DashboardProfessorController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setTurma(Turma turma) {
        this.turmaAtual = turma;
        nomeTurmaLabel.setText("Turma: " + turma.getNome());
        detalhesTurmaLabel.setText("Detalhes e gerenciamento da turma ID: " + turma.getId());

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

        // --- INÍCIO DAS ALTERAÇÕES ---
        // Carrega os dados da turma assim que a tela é definida
        refreshDetalhes();
        // --- FIM DAS ALTERAÇÕES ---
    }

    @FXML
    public void initialize() {
        // --- INÍCIO DAS ALTERAÇÕES ---
        // Configura as colunas da tabela para saberem de onde pegar os dados do objeto User
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        // A média e ações serão implementadas no futuro

        // Liga a lista de alunos à tabela
        alunosTableView.setItems(alunosDaTurma);
        // --- FIM DAS ALTERAÇÕES ---
    }

    // --- NOVO MÉTODO PARA ATUALIZAR A TELA ---
    private void refreshDetalhes() {
        if (turmaAtual == null) return;

        // 1. Atualizar a contagem de alunos
        int total = turmaDAO.countAlunosByTurmaId(turmaAtual.getId());
        totalAlunosLabel.setText(String.valueOf(total));

        // 2. Atualizar a lista de alunos na tabela
        List<User> alunos = userDAO.findAlunosByTurmaId(turmaAtual.getId());
        alunosDaTurma.setAll(alunos);
    }

    @FXML
    void handleAdicionarAluno(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adicionar-aluno-view.fxml"));
            Parent root = loader.load();

            AdicionarAlunoController controller = loader.getController();
            controller.setTurma(turmaAtual);

            Stage modalStage = new Stage();
            modalStage.initStyle(StageStyle.DECORATED);
            modalStage.setTitle("Adicionar Aluno à Turma");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait(); // O código para aqui até a janela ser fechada

            // --- INÍCIO DA ALTERAÇÃO ---
            // Assim que a janela é fechada, o código continua daqui e atualiza a tela!
            refreshDetalhes();
            // --- FIM DA ALTERAÇÃO ---

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleVoltarParaTurmas(ActionEvent event) {
        if (dashboardController != null) {
            dashboardController.onMinhasTurmasClick();
        }
    }
}