package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class DashboardProfessorHomeController {

    @FXML
    private VBox minhasTurmasContainer;

    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    public void initialize() {
        loadMinhasTurmas();
    }

    private void loadMinhasTurmas() {
        minhasTurmasContainer.getChildren().clear();

        User loggedInProfessor = UserSession.getInstance().getLoggedInUser();
        if (loggedInProfessor == null) return;

        List<Turma> turmasDoProfessor = turmaDAO.findByProfessorId(loggedInProfessor.getId());

        if (turmasDoProfessor.isEmpty()) {
            minhasTurmasContainer.getChildren().add(new Label("Você ainda não está responsável por nenhuma turma."));
        } else {
            for (Turma turma : turmasDoProfessor) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-card-view.fxml"));
                    VBox cardNode = loader.load();

                    TurmaCardController cardController = loader.getController();
                    cardController.setData(turma);

                    minhasTurmasContainer.getChildren().add(cardNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}