package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
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
import javafx.scene.Node;

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
    private Button relatoriosButton;
    @FXML
    private Button carometroButton;
    @FXML
    private Button bancoQuestoesButton;
    @FXML
    private Button simuladosButton;
    @FXML
    private Button sairButton;

    // --- NOVO CAMPO PARA GERENCIAR O BOTÃO ATIVO ---
    private Button activeButton;

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            refreshUserProfile();
            configurarVisibilidadeMenu(loggedInUser.getRole());

            // Define a tela e o botão inicial com base no perfil
            if (loggedInUser.getRole() == UserRole.ADMIN) {
                onDashboardButtonClick(null); // Inicia na home do admin
            } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
                // Para o professor, vamos unificar. O botão "Minhas Turmas" não existe no FXML principal.
                // Assumiremos que "Gerir Turmas" se torna "Minhas Turmas" para ele.
                onGerirTurmasClick(null);
            }
        }
    }

    // --- NOVO MÉTODO PARA GERENCIAR O ESTILO DO BOTÃO ATIVO ---
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active"); // Remove a classe do botão antigo
        }
        if (button != null) {
            button.getStyleClass().add("active"); // Adiciona a classe ao novo botão
            activeButton = button; // Atualiza a referência
        }
    }

    private void configurarVisibilidadeMenu(UserRole role) {
        List<Button> adminButtons = List.of(gerirUsuariosButton);
        List<Button> professorButtons = List.of(carometroButton, bancoQuestoesButton, simuladosButton);

        setNodesVisible(adminButtons, false);
        setNodesVisible(professorButtons, false);

        if (role == UserRole.ADMIN) {
            setNodesVisible(adminButtons, true);
        } else if (role == UserRole.PROFESSOR) {
            setNodesVisible(professorButtons, true);
            // Renomeia o botão para o professor
            gerirTurmasButton.setText("Minhas Turmas");
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

    // --- MÉTODOS DE CLIQUE ATUALIZADOS ---

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        loadCenterView("dashboard-home-view.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    public void onGerirUsuariosClick(ActionEvent event) {
        loadCenterView("usuarios-view.fxml");
        setActiveButton(gerirUsuariosButton);
    }

    @FXML
    public void onGerirTurmasClick(ActionEvent event) {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            loadCenterView("turmas-view.fxml"); // Admin vê a tabela de gerenciamento
        } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
            loadCenterView("minhas-turmas-view.fxml"); // Professor vê os cards
        }
        setActiveButton(gerirTurmasButton);
    }

    @FXML
    private void onRelatoriosClick(ActionEvent event) {
        loadCenterView("relatorios-view.fxml");
        setActiveButton(relatoriosButton);
    }

    @FXML
    private void onCarometroClick(ActionEvent event) {
        loadCenterView("carometro-view.fxml");
        setActiveButton(carometroButton);
    }

    @FXML
    private void onBancoQuestoesClick(ActionEvent event) {
        // loadCenterView("banco-questoes-view.fxml");
        setActiveButton(bancoQuestoesButton);
    }

    @FXML
    private void onSimuladosClick(ActionEvent event) {
        // loadCenterView("simulados-view.fxml");
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