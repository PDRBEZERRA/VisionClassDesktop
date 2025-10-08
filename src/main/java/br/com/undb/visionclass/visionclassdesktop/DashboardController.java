package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane mainBorderPane;

    // Variáveis para os novos componentes do cabeçalho
    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userAvatar;

    @FXML
    public void initialize() {
        // Busca o utilizador que fez o login
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            // Define o nome do utilizador no Label do cabeçalho
            userNameLabel.setText(loggedInUser.getNome());
            // Futuramente, aqui poderia carregar a imagem do perfil a partir de loggedInUser.getFoto()
        }

        // Carrega a tela inicial do dashboard por defeito
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    private void onGerirUsuariosClick() {
        loadCenterView("usuarios-view.fxml");
    }

    @FXML
    private void onGerirTurmasClick() {
        // Futuramente, isto irá carregar a tela de turmas
        System.out.println("Botão 'Gerir Turmas' clicado!");
        // loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onSairButtonClick() {
        UserSession.getInstance().clearSession();
        HelloApplication.getScreenManager().switchTo("login-view.fxml");
    }

    @FXML
    private void onRelatoriosClick() {
        loadCenterView("relatorios-view.fxml");
    }

    private void loadCenterView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }
}