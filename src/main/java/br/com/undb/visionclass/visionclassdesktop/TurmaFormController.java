package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.UUID;

public class TurmaFormController {

    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField anoTextField;
    @FXML
    private ComboBox<String> periodoComboBox;
    @FXML
    private ComboBox<User> professorComboBox;
    @FXML
    private Button salvarButton;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Preenche o ComboBox de períodos
        periodoComboBox.getItems().addAll("Matutino", "Vespertino", "Noturno");

        // Busca e preenche a lista de professores
        List<User> professores = userDAO.findByRole(UserRole.PROFESSOR);
        professorComboBox.getItems().setAll(professores);

        // Define como o nome do professor será exibido no ComboBox
        professorComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getNome();
            }

            @Override
            public User fromString(String string) {
                return null; // Não precisamos converter de volta
            }
        });
    }

    @FXML
    private void onSalvarButtonClick() {
        Turma newTurma = new Turma();
        newTurma.setId(UUID.randomUUID().toString());
        newTurma.setNome(nomeTextField.getText());
        newTurma.setAno(anoTextField.getText());
        newTurma.setPeriodo(periodoComboBox.getValue());

        User selectedProfessor = professorComboBox.getValue();
        if (selectedProfessor != null) {
            newTurma.setProfessorId(selectedProfessor.getId());
        }

        // (outros campos como desempenho podem ser definidos com valores padrão)
        newTurma.setDesempenho(0);

        turmaDAO.save(newTurma);

        closeWindow();
    }

    @FXML
    private void onCancelarButtonClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }
}