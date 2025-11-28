package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class CarometroController {

    @FXML
    private ComboBox<Turma> turmaComboBox;
    @FXML
    private TilePane alunosTilePane;

    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label infoLabel;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "Selecione uma turma..." : turma.getNome();
            }

            @Override
            public Turma fromString(String string) {
                return null;
            }
        });

        turmaComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            carregarAlunosDaTurma(newValue);
        });

        infoLabel.setText("Selecione uma turma para ver os alunos.");
        infoLabel.setVisible(true);
        loadingIndicator.setVisible(false);

        carregarTurmasDoProfessor();
    }

    private void carregarTurmasDoProfessor() {
        Task<List<Turma>> loadTurmasTask = new Task<>() {
            @Override
            protected List<Turma> call() throws Exception {
                User professorLogado = UserSession.getInstance().getLoggedInUser();
                if (professorLogado != null) {
                    return turmaDAO.findByProfessorId(professorLogado.getId());
                }
                return FXCollections.observableArrayList();
            }
        };

        turmaComboBox.setDisable(true);
        turmaComboBox.setPromptText("Carregando turmas...");

        loadTurmasTask.setOnSucceeded(event -> {
            turmaComboBox.setItems(FXCollections.observableArrayList(loadTurmasTask.getValue()));
            turmaComboBox.setDisable(false);
            turmaComboBox.setPromptText("Selecione uma turma...");
        });

        loadTurmasTask.setOnFailed(event -> {
            turmaComboBox.setDisable(false);
            turmaComboBox.setPromptText("Erro ao carregar turmas");
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar as turmas.");
            loadTurmasTask.getException().printStackTrace();
        });

        new Thread(loadTurmasTask).start();
    }

    private void carregarAlunosDaTurma(Turma turma) {
        if (turma == null) {
            alunosTilePane.getChildren().clear();
            infoLabel.setText("Selecione uma turma para ver os alunos.");
            infoLabel.setVisible(true);
            loadingIndicator.setVisible(false);
            return;
        }

        Task<List<User>> loadAlunosTask = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userDAO.findAlunosByTurmaId(turma.getId());
            }
        };

        alunosTilePane.getChildren().clear();
        infoLabel.setVisible(false);
        loadingIndicator.setVisible(true);

        loadAlunosTask.setOnSucceeded(event -> {
            List<User> alunos = loadAlunosTask.getValue();
            loadingIndicator.setVisible(false);

            if (alunos.isEmpty()) {
                infoLabel.setText("Nenhum aluno encontrado nesta turma.");
                infoLabel.setVisible(true);
            } else {
                for (User aluno : alunos) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("aluno-card-view.fxml"));
                        VBox alunoCard = loader.load();
                        AlunoCardController controller = loader.getController();
                        controller.setData(aluno, turma);
                        alunosTilePane.getChildren().add(alunoCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        loadAlunosTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            infoLabel.setText("Erro ao carregar alunos.");
            infoLabel.setVisible(true);
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar os alunos da turma.");
            loadAlunosTask.getException().printStackTrace();
        });

        new Thread(loadAlunosTask).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}