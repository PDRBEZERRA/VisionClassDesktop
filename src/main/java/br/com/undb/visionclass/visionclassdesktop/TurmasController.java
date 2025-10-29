package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TurmasController {

    @FXML
    private TableView<Turma> turmasTableView;
    @FXML
    private TableColumn<Turma, String> nomeColumn;
    @FXML
    private TableColumn<Turma, String> professorColumn;
    @FXML
    private TableColumn<Turma, String> alunosColumn;
    @FXML
    private TableColumn<Turma, String> anoColumn;
    @FXML
    private TableColumn<Turma, String> periodoColumn;
    @FXML
    private TableColumn<Turma, Void> acoesColumn;
    @FXML
    private Button adicionarTurmaButton;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Turma> turmasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        anoColumn.setCellValueFactory(new PropertyValueFactory<>("ano"));
        periodoColumn.setCellValueFactory(new PropertyValueFactory<>("periodo"));

        professorColumn.setCellValueFactory(cellData -> {
            User professor = userDAO.findById(cellData.getValue().getProfessorId());
            return new SimpleStringProperty(professor != null ? professor.getNome() : "Não definido");
        });

        alunosColumn.setCellValueFactory(cellData -> {
            int count = turmaDAO.countAlunosByTurmaId(cellData.getValue().getId());
            return new SimpleStringProperty(String.valueOf(count));
        });

        turmasTableView.setItems(turmasList);

        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        boolean isAdmin = (loggedInUser != null && loggedInUser.getRole() == UserRole.ADMIN);

        adicionarTurmaButton.setVisible(isAdmin);
        adicionarTurmaButton.setManaged(isAdmin);

        if (isAdmin) {
            setupAcoesColumn();
        } else {
            acoesColumn.setVisible(false);
        }

        loadTurmasData();
    }

    private void loadTurmasData() {
        List<Turma> turmas = turmaDAO.findAll();
        turmasList.setAll(turmas);
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<Turma, Void>, TableCell<Turma, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Turma, Void> call(final TableColumn<Turma, Void> param) {
                return new TableCell<>() {
                    private final Button btnExcluir = new Button("Excluir");

                    {
                        btnExcluir.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                        btnExcluir.setOnAction(event -> {
                            Turma turmaParaExcluir = getTableView().getItems().get(getIndex());
                            confirmarExclusao(turmaParaExcluir);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnExcluir);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void confirmarExclusao(Turma turma) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir a turma \"" + turma.getNome() + "\"?");
        alert.setContentText("Esta ação é irreversível e removerá todas as matrículas de alunos associadas a esta turma.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            turmaDAO.delete(turma.getId());
            loadTurmasData();
        }
    }

    @FXML
    private void onAdicionarButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-form-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Adicionar Nova Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            loadTurmasData();
        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de nova turma.");
            e.printStackTrace();
        }
    }
}