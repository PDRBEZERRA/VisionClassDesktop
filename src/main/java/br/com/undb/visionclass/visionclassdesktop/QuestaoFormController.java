package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AssuntoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.model.*;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestaoFormController {

    @FXML
    private ComboBox<Disciplina> disciplinaComboBox;
    @FXML
    private ComboBox<Assunto> assuntoComboBox;
    @FXML
    private ComboBox<NivelDificuldade> nivelDificuldadeComboBox;
    @FXML
    private TextArea enunciadoTextArea;
    @FXML
    private ComboBox<TipoQuestao> tipoQuestaoComboBox;
    @FXML
    private ScrollPane alternativasScrollPane;
    @FXML
    private VBox alternativasContainer;

    private DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    private AssuntoDAO assuntoDAO = new AssuntoDAO();
    private QuestaoDAO questaoDAO = new QuestaoDAO();

    private ToggleGroup alternativasToggleGroup = new ToggleGroup();
    private List<HBox> alternativaNodes = new ArrayList<>();

    @FXML
    public void initialize() {
        // Carrega os ComboBoxes com os dados iniciais
        disciplinaComboBox.getItems().addAll(disciplinaDAO.findAll());
        nivelDificuldadeComboBox.getItems().addAll(NivelDificuldade.values());
        tipoQuestaoComboBox.getItems().addAll(TipoQuestao.values());

        // Configura a lógica dos ComboBoxes dependentes
        disciplinaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assuntoComboBox.getItems().clear();
            if (newVal != null) {
                assuntoComboBox.getItems().addAll(assuntoDAO.findByDisciplinaId(newVal.getId()));
            }
        });

        // Mostra/esconde a seção de alternativas
        tipoQuestaoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isMultiplaEscolha = (newVal == TipoQuestao.MULTIPLA_ESCOLHA);
            alternativasScrollPane.setVisible(isMultiplaEscolha);
            alternativasScrollPane.setManaged(isMultiplaEscolha);
        });

        // Adiciona as duas primeiras alternativas por padrão
        adicionarAlternativa();
        adicionarAlternativa();
    }

    @FXML
    private void adicionarAlternativa() {
        HBox novaAlternativa = new HBox(10);
        novaAlternativa.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(alternativasToggleGroup);

        TextField textField = new TextField();
        textField.setPromptText("Texto da alternativa");
        HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);

        Button removerButton = new Button("X");
        removerButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        removerButton.setOnAction(event -> removerAlternativa(novaAlternativa));

        novaAlternativa.getChildren().addAll(radioButton, textField, removerButton);
        alternativasContainer.getChildren().add(alternativasContainer.getChildren().size() - 1, novaAlternativa);
        alternativaNodes.add(novaAlternativa);
    }

    private void removerAlternativa(HBox alternativaNode) {
        if (alternativaNodes.size() > 2) { // Não permite remover as duas primeiras
            alternativasContainer.getChildren().remove(alternativaNode);
            alternativaNodes.remove(alternativaNode);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aviso", "A questão deve ter no mínimo 2 alternativas.");
        }
    }

    @FXML
    private void handleSalvarQuestao() {
        // 1. Validar campos
        if (disciplinaComboBox.getValue() == null || assuntoComboBox.getValue() == null ||
                nivelDificuldadeComboBox.getValue() == null || tipoQuestaoComboBox.getValue() == null ||
                enunciadoTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos devem ser preenchidos.");
            return;
        }

        // 2. Criar o objeto Questao
        Questao novaQuestao = new Questao();
        novaQuestao.setDisciplinaId(disciplinaComboBox.getValue().getId());
        novaQuestao.setAssuntoId(assuntoComboBox.getValue().getId());
        novaQuestao.setNivelDificuldade(nivelDificuldadeComboBox.getValue());
        novaQuestao.setTipo(tipoQuestaoComboBox.getValue());
        novaQuestao.setEnunciado(enunciadoTextArea.getText());
        novaQuestao.setProfessorCriadorId(UserSession.getInstance().getLoggedInUser().getId());

        // 3. Adicionar alternativas se for múltipla escolha
        if (novaQuestao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            if (alternativasToggleGroup.getSelectedToggle() == null) {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Uma alternativa deve ser marcada como correta.");
                return;
            }

            List<Alternativa> alternativas = alternativaNodes.stream().map(hbox -> {
                RadioButton rb = (RadioButton) hbox.getChildren().get(0);
                TextField tf = (TextField) hbox.getChildren().get(1);
                return new Alternativa(tf.getText(), rb.isSelected());
            }).collect(Collectors.toList());

            // Valida se as alternativas não estão vazias
            if (alternativas.stream().anyMatch(a -> a.getTexto().trim().isEmpty())) {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O texto de todas as alternativas deve ser preenchido.");
                return;
            }
            novaQuestao.setAlternativas(alternativas);
        }

        // 4. Salvar no banco de dados
        questaoDAO.save(novaQuestao);

        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Questão salva com sucesso!");
        handleCancelar();
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) disciplinaComboBox.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}