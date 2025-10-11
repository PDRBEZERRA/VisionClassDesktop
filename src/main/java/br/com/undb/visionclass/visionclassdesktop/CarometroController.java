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
        // Configura o ComboBox para exibir o nome da Turma
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "" : turma.getNome();
            }

            @Override
            public Turma fromString(String string) {
                return null; // Não precisamos converter de String para Turma
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
        alunosTilePane.getChildren().clear(); // Limpa os alunos da turma anterior

        List<User> alunos = userDAO.findAlunosByTurmaId(turma.getId());

        for (User aluno : alunos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("aluno-card-view.fxml"));
                VBox alunoCard = loader.load();
                AlunoCardController controller = loader.getController();

                // --- ESTA É A LINHA QUE MUDOU ---
                controller.setData(aluno, turma); // Agora passa a turma junto com o aluno
                // --- FIM DA ALTERAÇÃO ---

                alunosTilePane.getChildren().add(alunoCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}