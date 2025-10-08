package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession; // <-- Importação adicionada
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

    // Criamos uma instância do nosso DAO para usar no método
    private UserDAO userDAO = new UserDAO();

    @FXML
    protected void onEntrarButtonClick() {
        String email = emailTextField.getText();
        String senha = senhaPasswordField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha o email e a senha.");
            return;
        }

        // --- LÓGICA DE AUTENTICAÇÃO REAL COM O BANCO DE DADOS ---
        User usuarioAutenticado = userDAO.findByEmailAndPassword(email, senha);

        // Se o DAO retornou um usuário (não nulo), o login foi bem-sucedido.
        if (usuarioAutenticado != null) {
            // GUARDA O UTILIZADOR NA SESSÃO ANTES DE NAVEGAR
            UserSession.getInstance().setLoggedInUser(usuarioAutenticado);

            // Mostra um alerta de boas-vindas com o nome do usuário vindo do banco de dados
            showAlert(Alert.AlertType.INFORMATION, "Login Bem-sucedido", "Bem-vindo, " + usuarioAutenticado.getNome() + "!");

            // Navega para a tela de dashboard
            HelloApplication.getScreenManager().switchTo("dashboard-view.fxml");
        } else {
            // Se o DAO retornou nulo, as credenciais estão incorretas.
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