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

public class DashboardProfessorController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    public void initialize() {
        refreshUserProfile();
        // A tela inicial do professor agora é o seu dashboard home
        loadCenterView("dashboard-professor-home-view.fxml");
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
            System.err.println("Erro ao carregar a imagem do avatar.");
            e.printStackTrace();
        }
    }

    // --- Métodos de Navegação do Professor ---

    @FXML
    private void onDashboardButtonClick() {
        // O botão Dashboard agora carrega a tela inicial do professor
        loadCenterView("dashboard-professor-home-view.fxml");
    }

    @FXML
    private void onMinhasTurmasClick() {
        loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onCarometroClick() {
        System.out.println("Navegar para Carômetro");
        // loadCenterView("carometro-view.fxml");
    }

    @FXML
    private void onBancoQuestoesClick() {
        System.out.println("Navegar para Banco de Questões");
        // loadCenterView("banco-questoes-view.fxml");
    }

    @FXML
    private void onSimuladosClick() {
        System.out.println("Navegar para Simulados");
        // loadCenterView("simulados-view.fxml");
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
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}