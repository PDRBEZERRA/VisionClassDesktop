package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class DashboardController {

    // A variável welcomeLabel foi REMOVIDA daqui

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        // A lógica do welcomeLabel foi REMOVIDA daqui
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
        loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onSairButtonClick() {
        UserSession.getInstance().clearSession();
        HelloApplication.getScreenManager().switchTo("login-view.fxml");
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