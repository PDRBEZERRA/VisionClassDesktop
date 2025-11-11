package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField senhaPasswordField;

    @FXML
    private Button entrarButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    protected void onEntrarButtonClick() {
        String email = emailTextField.getText();
        String senha = senhaPasswordField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha o email e a senha.");
            return;
        }

        User usuarioAutenticado = userDAO.findByEmailAndPassword(email, senha);

        if (usuarioAutenticado != null) {
            UserSession.getInstance().setLoggedInUser(usuarioAutenticado);
            showAlert(Alert.AlertType.INFORMATION, "Login Bem-sucedido", "Bem-vindo, " + usuarioAutenticado.getNome() + "!");

            switch (usuarioAutenticado.getRole()) {
                case ADMIN:
                    HelloApplication.getScreenManager().switchTo("dashboard-view.fxml");
                    break;
                case PROFESSOR:
                    HelloApplication.getScreenManager().switchTo("dashboard-professor-view.fxml");
                    break;
                case ALUNO:
                    HelloApplication.getScreenManager().switchTo("dashboard-aluno-view.fxml");
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Erro de Acesso", "Função de utilizador desconhecida.");
                    break;
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Falha no Login", "Email ou senha incorretos.");
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