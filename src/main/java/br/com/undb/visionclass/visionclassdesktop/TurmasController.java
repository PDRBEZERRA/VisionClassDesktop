package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class TurmasController {

    @FXML
    private TilePane turmasTilePane;

    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    public void initialize() {
        loadTurmasData();
    }

    private void loadTurmasData() {
        turmasTilePane.getChildren().clear();
        List<Turma> turmas = turmaDAO.findAll();

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