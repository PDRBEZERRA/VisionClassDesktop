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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;

public class DashboardProfessorController {

    private static DashboardProfessorController instance;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView userAvatar;
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

    private Button activeButton;

    @FXML
    public void initialize() {
        instance = this;
        refreshUserProfile();
        onDashboardButtonClick(null); // Carrega a home view inicial
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
            detalhesController.setDashboardController(this); // Passa a referência deste controller

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
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    // Tenta carregar do classpath se não encontrar no filesystem
                    image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
                }
            } else {
                // Tenta carregar do classpath como fallback padrão
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            // Garante que a imagem padrão seja carregada se tudo falhar
            if (image == null || image.isError()) {
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            userAvatar.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar no dashboard professor. Usando imagem padrão.");
            try {
                userAvatar.setImage(new Image(getClass().getResourceAsStream("images/avatar.jpg")));
            } catch (Exception ex) {
                System.err.println("Falha ao carregar até mesmo a imagem padrão.");
                // O ImageView ficará vazio ou com um placeholder de erro do JavaFX
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void onDashboardButtonClick(ActionEvent event) {
        loadCenterView("dashboard-professor-home-view.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    public void onMinhasTurmasClick(ActionEvent event) {
        loadCenterView("minhas-turmas-view.fxml");
        setActiveButton(minhasTurmasButton);
    }

    @FXML
    public void onCarometroClick(ActionEvent event) { // Tornar público se já não for
        loadCenterView("carometro-view.fxml");
        setActiveButton(carometroButton);
    }

    @FXML
    public void onBancoQuestoesClick(ActionEvent event) { // Tornar público se já não for
        loadCenterView("banco-questoes-view.fxml");
        setActiveButton(bancoQuestoesButton);
    }

    @FXML
    public void onSimuladosClick(ActionEvent event) { // Tornar público se já não for
        loadCenterView("simulados-view.fxml");
        setActiveButton(simuladosButton);
    }

    @FXML
    private void onRelatoriosClick(ActionEvent event) {
        loadCenterView("relatorios-professor-view.fxml");
        setActiveButton(relatoriosButton);
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

            // Adicionado: Verifica se o controller carregado é o HomeController e injeta a referência
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