package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    private Button adicionarButton;
    @FXML
    private Button editarButton;
    @FXML
    private Button excluirButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUsersData();
    }

    private void loadUsersData() {
        List<User> userList = userDAO.findAll();
        ObservableList<User> observableUserList = FXCollections.observableArrayList(userList);
        usersTableView.setItems(observableUserList);
    }

    @FXML
    private void onAdicionarButtonClick() {
        showUserForm(null); // Passa null para indicar que é um novo utilizador
    }

    @FXML
    private void onEditarButtonClick() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um utilizador para editar.");
            return;
        }
        showUserForm(selectedUser); // Passa o utilizador selecionado para o formulário
    }

    private void showUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("usuario-form-view.fxml"));
            Parent root = loader.load();

            // Pega o controlador do formulário ANTES de mostrar a janela
            UsuarioFormController controller = loader.getController();
            if (user != null) {
                // Se estiver a editar, chama o método para preencher os dados
                controller.setUserToEdit(user);
            }

            Stage stage = new Stage();
            stage.setTitle(user == null ? "Adicionar Novo Utilizador" : "Editar Utilizador");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadUsersData(); // Atualiza a tabela após fechar

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
            userDAO.delete(selectedUser.getId());
            loadUsersData();
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Utilizador excluído com sucesso.");
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