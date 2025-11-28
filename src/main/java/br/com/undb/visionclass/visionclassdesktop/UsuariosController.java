package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UsuariosController {

    @FXML
    private TableView<User> usersTableView;
    @FXML
    private TableColumn<User, String> nomeColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> matriculaColumn;
    @FXML
    private TableColumn<User, UserRole> roleColumn;

    @FXML
    private ProgressIndicator loadingIndicator;

    private UserDAO userDAO = new UserDAO();
    private DashboardController mainDashboardController;

    public void setMainDashboardController(DashboardController controller) {
        this.mainDashboardController = controller;
    }

    @FXML
    public void initialize() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTableView.setItems(FXCollections.observableArrayList());

        loadUsersData();
    }

    public void loadUsersData() {
        Task<List<User>> loadUsersTask = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userDAO.findAll();
            }
        };

        loadUsersTask.setOnRunning(e -> {
            usersTableView.setDisable(true);
            usersTableView.setItems(FXCollections.observableArrayList());
            loadingIndicator.setVisible(true);
        });

        loadUsersTask.setOnSucceeded(e -> {
            List<User> userList = loadUsersTask.getValue();
            ObservableList<User> observableUserList = FXCollections.observableArrayList(userList);
            usersTableView.setItems(observableUserList);

            loadingIndicator.setVisible(false);
            usersTableView.setDisable(false);
        });

        loadUsersTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            usersTableView.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Erro de Carregamento", "Não foi possível carregar a lista de utilizadores.");
            loadUsersTask.getException().printStackTrace();
        });

        new Thread(loadUsersTask).start();
    }

    @FXML
    private void onAdicionarButtonClick() {
        showUserForm(null);
    }

    @FXML
    private void onEditarButtonClick() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um utilizador para editar.");
            return;
        }
        showUserForm(selectedUser);
    }

    private void showUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("usuario-form-view.fxml"));
            Parent root = loader.load();

            UsuarioFormController controller = loader.getController();
            if (user != null) {
                controller.setUserToEdit(user);
            }
            controller.setMainDashboardController(this.mainDashboardController);

            Stage stage = new Stage();
            stage.setTitle(user == null ? "Adicionar Novo Utilizador" : "Editar Utilizador");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadUsersData();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de utilizador.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onExcluirButtonClick() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um utilizador para excluir.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmar Exclusão");
        confirmationAlert.setHeaderText("Excluir Utilizador: " + selectedUser.getNome());
        confirmationAlert.setContentText("Tem a certeza de que deseja excluir este utilizador? Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userDAO.delete(selectedUser.getId());
                    return null;
                }
            };

            deleteTask.setOnRunning(e -> {
                loadingIndicator.setVisible(true);
                usersTableView.setDisable(true);
            });

            deleteTask.setOnSucceeded(e -> {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Utilizador excluído com sucesso.");
                loadUsersData();
            });

            deleteTask.setOnFailed(e -> {
                loadingIndicator.setVisible(false);
                usersTableView.setDisable(false);
                showAlert(Alert.AlertType.ERROR, "Erro ao Excluir", "Não foi possível excluir o utilizador.");
                deleteTask.getException().printStackTrace();
            });

            new Thread(deleteTask).start();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}