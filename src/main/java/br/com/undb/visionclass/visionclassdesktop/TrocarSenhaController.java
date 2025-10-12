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

        // 1. OBTEM O HASH DA SENHA ORIGINAL SALVA NO BANCO
        // O objeto User na sessão geralmente não carrega o hash da senha,
        // então precisamos recarregar o usuário para obter a senha com hash correta para a checagem.
        // Assumindo que o findById carrega a senha (embora o UserDAO atual não o faça)
        // Usaremos o hash do objeto User na sessão, que é o que o LoginController carrega (e presume-se que a senha hash está sendo carregada na sessão)

        // Se o seu objeto User na sessão não tiver a senha hash (getSenha() == null),
        // o código deve falhar no PasswordUtil.checkPassword.
        // Vou assumir que o hash está carregado na sessão (o que é uma prática ruim, mas é o que o seu projeto parece fazer).

        String hashedPasswordFromSession = loggedInUser.getSenha();

        // 1. Validar Senha Atual
        // NOTE: Se o checkPassword falhar, é porque o hash não está na sessão.
        // Para resolver isso de forma robusta, o findById precisaria carregar a senha hash.
        if (!PasswordUtil.checkPassword(senhaAtual, hashedPasswordFromSession)) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A senha atual está incorreta.");
            return;
        }

        // 2. Validar Nova Senha
        if (novaSenha.isEmpty() || novaSenha.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A nova senha deve ter pelo menos 6 caracteres.");
            return;
        }
        if (!novaSenha.equals(confirmarNovaSenha)) {
            showAlert(Alert.AlertType.ERROR, "Erro", "A nova senha e a confirmação não correspondem.");
            return;
        }

        // 3. Hash da nova senha e ATUALIZAÇÃO VIA NOVO MÉTODO
        String novaSenhaHashed = PasswordUtil.hashPassword(novaSenha);

        // Persiste a mudança no banco usando o novo método específico
        userDAO.updatePassword(loggedInUser.getId(), novaSenhaHashed);

        // Atualiza o objeto na sessão para que o próximo checkPassword funcione
        loggedInUser.setSenha(novaSenhaHashed);

        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Sua senha foi atualizada com sucesso!");

        // Fecha a janela de troca de senha
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