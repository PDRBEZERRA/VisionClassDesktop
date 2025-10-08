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

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userAvatar;

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            userNameLabel.setText(loggedInUser.getNome());
        }
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    public void onGerirUsuariosClick() { // Mudado para public para ser acessível
        loadCenterView("usuarios-view.fxml");
    }

    @FXML
    private void onGerirTurmasClick() {
        loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onRelatoriosClick() {
        loadCenterView("relatorios-view.fxml");
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

            // LIGAÇÃO ENTRE CONTROLADORES
            Object controller = loader.getController();
            if (controller instanceof DashboardHomeController) {
                ((DashboardHomeController) controller).setMainController(this);
            }

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }
}