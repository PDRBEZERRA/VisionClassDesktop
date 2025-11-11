package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;

public class DashboardProfessorController {

    private static DashboardProfessorController instance;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private Circle userAvatar;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button minhasTurmasButton;
    @FXML
    private Button carometroButton;
    @FXML
    private Button bancoQuestoesButton;
    @FXML
    private Button simuladosButton;
    @FXML
    private Button relatoriosButton;
    @FXML
    private Button sairButton;

    private Button activeButton;

    @FXML
    public void initialize() {
        instance = this;
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

    public static DashboardProfessorController getInstance() {
        return instance;
    }

    public void showDetalhesTurma(Turma turma) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("detalhes-turma-view.fxml"));
            Parent view = loader.load();

            DetalhesTurmaController detalhesController = loader.getController();
            detalhesController.setTurma(turma);
            detalhesController.setDashboardController(this);

            mainBorderPane.setCenter(view);
            setActiveButton(minhasTurmasButton);
        } catch (IOException e) {
            e.printStackTrace();
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

            userAvatar.setFill(new ImagePattern(image));

        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar no dashboard. Usando imagem padrão. " + e.getMessage());
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/br/com.undb.visionclass/visionclassdesktop/images/avatar.jpg"));
                userAvatar.setFill(new ImagePattern(defaultImage));
            } catch (Exception ex) {
                System.err.println("Falha ao carregar até mesmo a imagem padrão.");
            }
        }
    }

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        setActiveButton(dashboardButton);
        loadCenterView("dashboard-professor-home-view.fxml");
    }

    @FXML
    public void onMinhasTurmasClick(ActionEvent event) {
        setActiveButton(minhasTurmasButton);
        loadCenterView("minhas-turmas-view.fxml");
    }

    @FXML
    public void onCarometroClick(ActionEvent event) {
        setActiveButton(carometroButton);
        loadCenterView("carometro-view.fxml");
    }

    @FXML
    public void onBancoQuestoesClick(ActionEvent event) {
        setActiveButton(bancoQuestoesButton);
        loadCenterView("banco-questoes-view.fxml");
    }

    @FXML
    public void onSimuladosClick(ActionEvent event) {
        setActiveButton(simuladosButton);
        loadCenterView("simulados-view.fxml");
    }

    @FXML
    private void onRelatoriosClick(ActionEvent event) {
        setActiveButton(relatoriosButton);
        loadCenterView("relatorios-professor-view.fxml");
    }

    @FXML
    private void onSairButtonClick() {
        setActiveButton(sairButton);
        UserSession.getInstance().clearSession();
        HelloApplication.getScreenManager().switchTo("login-view.fxml");
    }

    private void loadCenterView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardProfessorHomeController) {
                ((DashboardProfessorHomeController) controller).setMainController(this);
            }

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a vista: " + fxmlFile);
            e.printStackTrace();
        }
    }
}