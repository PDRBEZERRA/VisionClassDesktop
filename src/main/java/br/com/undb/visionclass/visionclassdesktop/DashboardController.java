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

    /**
     * Atualiza o nome de utilizador e a imagem do avatar no cabeçalho.
     * Este método é público para poder ser chamado por outros controladores.
     */
    public void refreshUserProfile() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            userNameLabel.setText(loggedInUser.getNome());
            loadUserAvatar(loggedInUser.getFoto());
        }
    }

    /**
     * Carrega a imagem do avatar no ImageView, com uma imagem padrão caso falhe.
     * @param photoPath O caminho para o ficheiro da imagem do utilizador.
     */
    private void loadUserAvatar(String photoPath) {
        try {
            if (photoPath != null && !photoPath.isEmpty()) {
                File file = new File(photoPath);
                if (file.exists()) {
                    userAvatar.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }
            // Carrega a imagem padrão se o caminho for nulo, vazio ou o ficheiro não existir.
            Image defaultImage = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            userAvatar.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar: " + photoPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    public void onGerirUsuariosClick() { // Mantido como public
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

            Object controller = loader.getController();
            if (controller instanceof DashboardHomeController) {
                ((DashboardHomeController) controller).setMainController(this);
            } else if (controller instanceof UsuariosController) {
                // Passa a referência deste DashboardController para o UsuariosController
                ((UsuariosController) controller).setMainDashboardController(this);
            }

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }
}