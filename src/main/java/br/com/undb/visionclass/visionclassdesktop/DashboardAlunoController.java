package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;

public class DashboardAlunoController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel; // Agora na sidebar
    @FXML
    private ImageView userAvatar; // Agora na sidebar
    @FXML
    private Button dashboardButton;
    @FXML
    private Button simuladosButton;
    @FXML
    private Button sairButton; // Adicionado do FXML

    private Button activeButton;

    @FXML
    public void initialize() {
        refreshUserProfile();
        // Define o Dashboard como a tela inicial do aluno
        onDashboardButtonClick(null);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        if (button != null) {
            button.getStyleClass().add("active");
            activeButton = button;
        }
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
                    // Usar a imagem padrão se o arquivo não for encontrado
                    image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
                }
            } else {
                // Usar a imagem padrão se não houver nome de arquivo
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            userAvatar.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        // Carrega o dashboard-aluno-home-view.fxml (tela de estatísticas)
        loadCenterView("dashboard-aluno-home-view.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void onSimuladosClick(ActionEvent event) {
        // Usando relatorios-view.fxml como placeholder
        loadCenterView("relatorios-view.fxml");
        setActiveButton(simuladosButton);
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