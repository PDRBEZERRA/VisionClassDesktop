package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TurmasController {

    @FXML
    private TilePane turmasTilePane;

    @FXML
    private Button adicionarTurmaButton; // Ligação ao botão do FXML

    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    public void initialize() {
        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser != null) {
            // Lógica para esconder o botão se o utilizador não for ADMIN
            boolean isAdmin = (loggedInUser.getRole() == UserRole.ADMIN);
            adicionarTurmaButton.setVisible(isAdmin);
            adicionarTurmaButton.setManaged(isAdmin);
        }

        loadTurmasData();
    }

    private void loadTurmasData() {
        turmasTilePane.getChildren().clear();

        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser == null) return;

        List<Turma> turmas;
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            turmas = turmaDAO.findAll();
        } else if (loggedInUser.getRole() == UserRole.PROFESSOR) {
            turmas = turmaDAO.findByProfessorId(loggedInUser.getId());
        } else {
            turmas = Collections.emptyList();
        }

        for (Turma turma : turmas) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-card-view.fxml"));
                VBox cardNode = loader.load();
                TurmaCardController cardController = loader.getController();
                cardController.setData(turma);
                turmasTilePane.getChildren().add(cardNode);
            } catch (IOException e) {
                System.err.println("Erro ao carregar o card da turma: " + turma.getNome());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onAdicionarButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Adicionar Nova Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadTurmasData();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de nova turma.");
            e.printStackTrace();
        }
    }
}