package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole; // Importação adicionada
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button; // Importação adicionada
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node; // Importação adicionada

import java.io.File;
import java.io.IOException;
import java.util.List; // Importação adicionada

public class DashboardController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView userAvatar;

    // --- Botões da Barra Lateral ---
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

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            refreshUserProfile();
            configurarVisibilidadeMenu(loggedInUser.getRole());
            if (loggedInUser.getRole() == UserRole.ADMIN) {
                loadCenterView("dashboard-home-view.fxml");
            } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
                loadCenterView("turmas-view.fxml"); // Professor começa na tela de turmas
            }
        }
    }

    private void configurarVisibilidadeMenu(UserRole role) {
        // Guarda todos os botões numa lista para facilitar
        List<Button> adminButtons = List.of(gerirUsuariosButton);
        List<Button> professorButtons = List.of(carometroButton, bancoQuestoesButton, simuladosButton);
        List<Button> commonButtons = List.of(dashboardButton, gerirTurmasButton, relatoriosButton, sairButton);

        // Esconde todos os botões específicos de perfis
        setNodesVisible(adminButtons, false);
        setNodesVisible(professorButtons, false);

        // Mostra os botões com base na função
        if (role == UserRole.ADMIN) {
            setNodesVisible(adminButtons, true);
        } else if (role == UserRole.PROFESSOR) {
            setNodesVisible(professorButtons, true);
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

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-home-view.fxml");
    }

    @FXML
    public void onGerirUsuariosClick() {
        loadCenterView("usuarios-view.fxml");
    }

    @FXML
    public void onGerirTurmasClick() {
        loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onRelatoriosClick() {
        loadCenterView("relatorios-view.fxml");
    }

    // --- MÉTODOS EM FALTA ADICIONADOS AQUI ---
    @FXML
    private void onCarometroClick() {
        System.out.println("Botão 'Carômetro' clicado!");
        // loadCenterView("carometro-view.fxml");
    }

    @FXML
    private void onBancoQuestoesClick() {
        System.out.println("Botão 'Banco de Questões' clicado!");
        // loadCenterView("banco-questoes-view.fxml");
    }

    @FXML
    private void onSimuladosClick() {
        System.out.println("Botão 'Simulados' clicado!");
        // loadCenterView("simulados-view.fxml");
    }
    // --- FIM DOS MÉTODOS ADICIONADOS ---

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