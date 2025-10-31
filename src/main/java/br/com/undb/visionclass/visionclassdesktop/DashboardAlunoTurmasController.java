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

public class DashboardAlunoTurmasController {

    @FXML
    private TilePane turmasTilePane;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private User alunoLogado;

    @FXML
    public void initialize() {
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        loadTurmasData();
    }

    private void loadTurmasData() {
        turmasTilePane.getChildren().clear();

        if (alunoLogado == null) return;

        List<Turma> turmas = turmaDAO.findAllByAlunoId(alunoLogado.getId());

        if (turmas.isEmpty()) {
            turmasTilePane.getChildren().add(new Label("Você ainda não está matriculado em nenhuma turma."));
        } else {
            for (Turma turma : turmas) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("aluno-turma-card-view.fxml"));
                    VBox cardNode = loader.load();

                    AlunoTurmaCardController cardController = loader.getController();
                    cardController.setData(alunoLogado, turma);

                    turmasTilePane.getChildren().add(cardNode);
                } catch (IOException e) {
                    System.err.println("Erro ao carregar o card de desempenho da turma: " + turma.getNome());
                    e.printStackTrace();
                }
            }
        }
    }
}