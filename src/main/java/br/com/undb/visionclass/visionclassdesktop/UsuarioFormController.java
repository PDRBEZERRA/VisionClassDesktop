package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class UsuarioFormController {

    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField senhaPasswordField;
    @FXML
    private TextField matriculaTextField;
    @FXML
    private TextField cpfTextField;
    @FXML
    private ComboBox<UserRole> roleComboBox;
    @FXML
    private Button salvarButton;
    @FXML
    private Button cancelarButton;

    private UserDAO userDAO = new UserDAO();
    private User userToEdit; // Variável para guardar o utilizador em modo de edição

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(UserRole.values());
    }

    /**
     * Método público para receber o utilizador a ser editado.
     * Este método será chamado pelo UsuariosController antes de a janela abrir.
     */
    public void setUserToEdit(User user) {
        this.userToEdit = user;
        // Preenche os campos do formulário com os dados do utilizador
        nomeTextField.setText(user.getNome());
        emailTextField.setText(user.getEmail());
        matriculaTextField.setText(user.getMatricula());
        cpfTextField.setText(user.getCpf());
        roleComboBox.setValue(user.getRole());

        // Desativa o campo de senha durante a edição para não a alterar acidentalmente
        senhaPasswordField.setDisable(true);
        senhaPasswordField.setPromptText("Senha não pode ser alterada aqui");
    }

    @FXML
    private void onSalvarButtonClick() {
        // Verifica se os campos essenciais estão preenchidos
        if (nomeTextField.getText().isEmpty() || emailTextField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Nome e Email são campos obrigatórios.");
            return;
        }

        if (userToEdit == null) {
            // MODO CRIAÇÃO: Cria um novo utilizador
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setNome(nomeTextField.getText());
            newUser.setEmail(emailTextField.getText());
            newUser.setSenha(senhaPasswordField.getText());
            newUser.setMatricula(matriculaTextField.getText());
            newUser.setCpf(cpfTextField.getText());
            newUser.setRole(roleComboBox.getValue());
            userDAO.save(newUser);
        } else {
            // MODO EDIÇÃO: Atualiza o utilizador existente
            userToEdit.setNome(nomeTextField.getText());
            userToEdit.setEmail(emailTextField.getText());
            userToEdit.setMatricula(matriculaTextField.getText());
            userToEdit.setCpf(cpfTextField.getText());
            userToEdit.setRole(roleComboBox.getValue());
            userDAO.update(userToEdit);
        }

        closeWindow();
    }

    @FXML
    private void onCancelarButtonClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}