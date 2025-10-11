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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DetalhesTurmaController {

    private DashboardProfessorController dashboardController;
    private UserDAO userDAO = new UserDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private Label detalhesTurmaLabel;
    @FXML
    private Label totalAlunosLabel;
    @FXML
    private Label professorResponsavelLabel;
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
        refreshDetalhes();
    }

    @FXML
    public void initialize() {
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        alunosTableView.setItems(alunosDaTurma);

        // --- INÍCIO DA LÓGICA DE REMOÇÃO ---
        // Configura a coluna de ações para ter um botão "Remover"
        setupAcoesColumn();
        // --- FIM DA LÓGICA DE REMOÇÃO ---
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btnRemover = new Button("Remover");

                    {
                        btnRemover.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;"); // Estilo do botão
                        btnRemover.setOnAction(event -> {
                            User alunoParaRemover = getTableView().getItems().get(getIndex());
                            confirmarRemocao(alunoParaRemover);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnRemover);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void confirmarRemocao(User aluno) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover aluno da turma");
        alert.setContentText("Tem a certeza de que deseja remover " + aluno.getNome() + " desta turma?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            turmaDAO.removeAlunoFromTurma(turmaAtual.getId(), aluno.getId());
            refreshDetalhes(); // Atualiza a tabela e os contadores
        }
    }

    private void refreshDetalhes() {
        if (turmaAtual == null) return;
        int total = turmaDAO.countAlunosByTurmaId(turmaAtual.getId());
        totalAlunosLabel.setText(String.valueOf(total));
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
            modalStage.showAndWait();
            refreshDetalhes();
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