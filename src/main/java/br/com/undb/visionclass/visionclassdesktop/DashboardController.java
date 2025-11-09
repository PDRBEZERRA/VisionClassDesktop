package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    private Button dashboardButton;
    @FXML
    private Button gerirUsuariosButton;
    @FXML
    private Button gerirTurmasButton;
    @FXML
    private Button gerirDisciplinasButton;
    @FXML
    private Button relatoriosButton;
    @FXML
    private Button carometroButton;
    @FXML
    private Button bancoQuestoesButton;
    @FXML
    private Button simuladosButton;
    @FXML
    private Button sairButton;

    private Button activeButton;

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            refreshUserProfile();
            configurarVisibilidadeMenu(loggedInUser.getRole());
            if (loggedInUser.getRole() == UserRole.ADMIN) {
                onDashboardButtonClick(null);
            } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
                onGerirTurmasClick(null);
            }
        }
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

    private void configurarVisibilidadeMenu(UserRole role) {
        List<Button> adminButtons = List.of(gerirUsuariosButton, gerirDisciplinasButton);
        List<Button> professorButtons = List.of(carometroButton, bancoQuestoesButton, simuladosButton);

        setNodesVisible(adminButtons, false);
        setNodesVisible(professorButtons, false);

        if (role == UserRole.ADMIN) {
            setNodesVisible(adminButtons, true);
            relatoriosButton.setVisible(true);
            relatoriosButton.setManaged(true);
            gerirTurmasButton.setVisible(true);
            gerirTurmasButton.setManaged(true);
        } else if (role == UserRole.PROFESSOR) {
            setNodesVisible(professorButtons, true);
            gerirTurmasButton.setText("Minhas Turmas");
            relatoriosButton.setVisible(true);
            relatoriosButton.setManaged(true);
            gerirTurmasButton.setVisible(true);
            gerirTurmasButton.setManaged(true);
        }
    }

    private void setNodesVisible(List<Button> nodes, boolean isVisible) {
        for (Node node : nodes) {
            node.setVisible(isVisible);
            node.setManaged(isVisible);
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

            userAvatar.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar no dashboard. Usando imagem padrão. " + e.getMessage());
            try {
                userAvatar.setImage(new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg")));
            } catch (Exception ex) {
                System.err.println("Falha ao carregar até mesmo a imagem padrão.");
            }
        }
    }

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        setActiveButton(dashboardButton);
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    public void onGerirUsuariosClick(ActionEvent event) {
        setActiveButton(gerirUsuariosButton);
        loadCenterView("usuarios-view.fxml");
    }

    @FXML
    public void onGerirTurmasClick(ActionEvent event) {
        setActiveButton(gerirTurmasButton);
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            loadCenterView("turmas-view.fxml");
        } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
            loadCenterView("minhas-turmas-view.fxml");
        }
    }

    @FXML
    private void onGerirDisciplinasClick(ActionEvent event) {
        setActiveButton(gerirDisciplinasButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gerenciar-disciplinas-assuntos-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gerenciar Disciplinas e Assuntos");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Erro ao abrir a tela de gerenciamento de disciplinas.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onRelatoriosClick(ActionEvent event) {
        setActiveButton(relatoriosButton);
        loadCenterView("relatorios-view.fxml");
    }

    @FXML
    private void onCarometroClick(ActionEvent event) {
        setActiveButton(carometroButton);
        loadCenterView("carometro-view.fxml");
    }

    @FXML
    private void onBancoQuestoesClick(ActionEvent event) {
        setActiveButton(bancoQuestoesButton);
        loadCenterView("banco-questoes-view.fxml");
    }

    @FXML
    private void onSimuladosClick(ActionEvent event) {
        setActiveButton(simuladosButton);
        loadCenterView("simulados-view.fxml");
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