package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.ImagePattern; // Importação Adicionada
import javafx.scene.shape.Circle; // Importação Modificada (de ImageView)
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DashboardAlunoController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel; // Agora na sidebar
    @FXML
    private Circle userAvatar; // Tipo Modificado (de ImageView para Circle)
    @FXML
    private Button dashboardButton;
    @FXML
    private Button minhasTurmasButton; // NOVO BOTÃO
    @FXML
    private Button simuladosButton;
    @FXML
    private Button sairButton;
    @FXML
    private Button trocarSenhaButton;

    private Button activeButton;

    @FXML
    public void initialize() {
        refreshUserProfile();
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
                if (file.exists() && !file.isDirectory()) {
                    image = new Image(file.toURI().toString());
                } else {
                    image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
            }

            if (image == null || image.isError()) {
                image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
            }

            // Linha Corrigida
            userAvatar.setFill(new ImagePattern(image));

        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar: " + e.getMessage());
            try {
                // Bloco Catch Corrigido
                Image defaultImage = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
                userAvatar.setFill(new ImagePattern(defaultImage));
            } catch (Exception ex) {
                System.err.println("Falha total ao carregar qualquer imagem.");
            }
        }
    }

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        loadCenterView("dashboard-aluno-home-view.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void onMinhasTurmasClick(ActionEvent event) {
        loadCenterView("dashboard-aluno-turmas-view.fxml");
        setActiveButton(minhasTurmasButton);
    }

    @FXML
    private void onSimuladosClick(ActionEvent event) {
        loadCenterView("simulados-aluno-view.fxml");
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
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTrocarSenha() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("trocar-senha-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Alterar Senha");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível carregar a tela de alteração de senha.");
            alert.setContentText("Por favor, tente novamente ou contate o suporte.");
            alert.showAndWait();
        }
    }
}