package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class TrocarSenhaController {

    @FXML
    private PasswordField senhaAtualField;
    @FXML
    private PasswordField novaSenhaField;
    @FXML
    private PasswordField confirmarNovaSenhaField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleAtualizarSenha() {
        String senhaAtual = senhaAtualField.getText();
        String novaSenha = novaSenhaField.getText();
        String confirmarNovaSenha = confirmarNovaSenhaField.getText();

        User loggedInUser = UserSession.getInstance().getLoggedInUser();


        String hashedPasswordFromSession = loggedInUser.getSenha();

        if (!PasswordUtil.checkPassword(senhaAtual, hashedPasswordFromSession)) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A senha atual está incorreta.");
            return;
        }

        if (novaSenha.isEmpty() || novaSenha.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A nova senha deve ter pelo menos 6 caracteres.");
            return;
        }
        if (!novaSenha.equals(confirmarNovaSenha)) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A nova senha e a confirmação não correspondem.");
            return;
        }

        String novaSenhaHashed = PasswordUtil.hashPassword(novaSenha);

        userDAO.updatePassword(loggedInUser.getId(), novaSenhaHashed);

        loggedInUser.setSenha(novaSenhaHashed);

        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Sua senha foi atualizada com sucesso!");

        Stage stage = (Stage) senhaAtualField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}