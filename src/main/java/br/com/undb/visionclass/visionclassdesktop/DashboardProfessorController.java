package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.Turma;
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

    // --- INÍCIO DAS ALTERAÇÕES ---
    private static DashboardProfessorController instance;
    // --- FIM DAS ALTERAÇÕES ---

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    public void initialize() {
        // --- INÍCIO DAS ALTERAÇÕES ---
        instance = this; // Salva a instância atual para ser acessada globalmente
        // --- FIM DAS ALTERAÇÕES ---

        refreshUserProfile();
        loadCenterView("dashboard-professor-home-view.fxml");
    }

    // --- INÍCIO DAS ALTERAÇÕES ---
    public static DashboardProfessorController getInstance() {
        return instance;
    }

    public void showDetalhesTurma(Turma turma) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("detalhes-turma-view.fxml"));
            Parent view = loader.load();

            // Pega o controller da tela de detalhes
            DetalhesTurmaController detalhesController = loader.getController();
            detalhesController.setTurma(turma); // Passa o objeto da turma
            detalhesController.setDashboardController(this); // Passa a si mesmo para o botão "voltar" funcionar

            mainBorderPane.setCenter(view); // Coloca a nova tela no centro
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // --- FIM DAS ALTERAÇÕES ---

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

    @FXML
    private void onDashboardButtonClick() {
        loadCenterView("dashboard-professor-home-view.fxml");
    }

    @FXML
    public void onMinhasTurmasClick() { // Mudei para public para poder chamar de fora
        loadCenterView("turmas-view.fxml");
    }

    @FXML
    private void onCarometroClick() {
        System.out.println("Navegar para Carômetro");
    }

    @FXML
    private void onBancoQuestoesClick() {
        System.out.println("Navegar para Banco de Questões");
    }

    @FXML
    private void onSimuladosClick() {
        System.out.println("Navegar para Simulados");
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