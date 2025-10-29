package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class CarometroController {

    @FXML
    private ComboBox<Turma> turmaComboBox;
    @FXML
    private TilePane alunosTilePane;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "" : turma.getNome();
            }

            @Override
            public Turma fromString(String string) {
                return null;
            }
        });

        carregarTurmasDoProfessor();

        turmaComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null) {
                carregarAlunosDaTurma(newValue);
            }
        });
    }

    private void carregarTurmasDoProfessor() {
        User professorLogado = UserSession.getInstance().getLoggedInUser();
        if (professorLogado != null) {
            List<Turma> turmas = turmaDAO.findByProfessorId(professorLogado.getId());
            turmaComboBox.getItems().setAll(turmas);
        }
    }

    private void carregarAlunosDaTurma(Turma turma) {
        alunosTilePane.getChildren().clear();

        List<User> alunos = userDAO.findAlunosByTurmaId(turma.getId());

        for (User aluno : alunos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("aluno-card-view.fxml"));
                VBox alunoCard = loader.load();
                AlunoCardController controller = loader.getController();

                controller.setData(aluno, turma);

                alunosTilePane.getChildren().add(alunoCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}