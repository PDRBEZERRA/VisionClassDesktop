package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class MinhasTurmasController {

    @FXML
    private TilePane turmasTilePane;

    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    public void initialize() {
        loadTurmasData();
    }

    private void loadTurmasData() {
        turmasTilePane.getChildren().clear();

        User loggedInUser = UserSession.getInstance().getLoggedInUser();
        if (loggedInUser == null) return;

        List<Turma> turmas = turmaDAO.findByProfessorId(loggedInUser.getId());

        if (turmas.isEmpty()) {
            turmasTilePane.getChildren().add(new Label("Você ainda não está associado a nenhuma turma."));
        } else {
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
    }
}