package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.File;
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
        refreshUserProfile();
        loadCenterView("dashboard-home-view.fxml");
    }

    public void refreshUserProfile() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            userNameLabel.setText(loggedInUser.getNome());
            loadUserAvatar(loggedInUser.getFoto());
        }
    }

    private void loadUserAvatar(String photoFileName) {
        try {
            Image image;
            if (photoFileName != null && !photoFileName.isEmpty()) {
                File file = new File("user_photos/" + photoFileName);
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            userAvatar.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar no dashboard.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    public void onGerirUsuariosClick() {
        loadCenterView("usuarios-view.fxml");
    }

    @FXML
    public void onGerirTurmasClick() { // Alterado para public
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

            Object controller = loader.getController();
            if (controller instanceof DashboardHomeController) {
                ((DashboardHomeController) controller).setMainController(this);
            } else if (controller instanceof UsuariosController) {
                ((UsuariosController) controller).setMainDashboardController(this);
            }

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }
}