package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AssuntoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.model.*;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.application.Platform;
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
    private Label titleLabel; // Adicionado para mudar o título
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
    private Questao questaoParaEditar; // Armazena a questão em modo de edição

    @FXML
    public void initialize() {
        disciplinaComboBox.getItems().addAll(disciplinaDAO.findAll());
        nivelDificuldadeComboBox.getItems().addAll(NivelDificuldade.values());
        tipoQuestaoComboBox.getItems().addAll(TipoQuestao.values());

        disciplinaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assuntoComboBox.getItems().clear();
            if (newVal != null) {
                assuntoComboBox.getItems().addAll(assuntoDAO.findByDisciplinaId(newVal.getId()));
            }
        });

        tipoQuestaoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isMultiplaEscolha = (newVal == TipoQuestao.MULTIPLA_ESCOLHA);
            alternativasScrollPane.setVisible(isMultiplaEscolha);
            alternativasScrollPane.setManaged(isMultiplaEscolha);
        });
    }

    // --- NOVO MÉTODO PARA ENTRAR EM MODO DE EDIÇÃO ---
    public void setQuestaoParaEditar(Questao questao) {
        this.questaoParaEditar = questao;
        titleLabel.setText("Editar Questão"); // Muda o título da janela

        // Preenche os campos com os dados da questão
        enunciadoTextArea.setText(questao.getEnunciado());
        nivelDificuldadeComboBox.setValue(questao.getNivelDificuldade());
        tipoQuestaoComboBox.setValue(questao.getTipo());

        // Seleciona a disciplina correta
        disciplinaComboBox.getSelectionModel().select(
                disciplinaComboBox.getItems().stream()
                        .filter(d -> d.getId() == questao.getDisciplinaId())
                        .findFirst().orElse(null)
        );

        // A seleção do assunto precisa esperar a disciplina ser processada
        Platform.runLater(() -> {
            assuntoComboBox.getSelectionModel().select(
                    assuntoComboBox.getItems().stream()
                            .filter(a -> a.getId() == questao.getAssuntoId())
                            .findFirst().orElse(null)
            );
        });

        // Preenche as alternativas
        if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            // Limpa as alternativas padrão antes de adicionar as existentes
            alternativasContainer.getChildren().removeIf(node -> node instanceof HBox);
            alternativaNodes.clear();

            for (Alternativa alt : questao.getAlternativas()) {
                adicionarAlternativa(alt.getTexto(), alt.isCorreta());
            }
        }
    }

    @FXML
    private void adicionarAlternativa() {
        adicionarAlternativa("", false); // Chama com valores padrão
    }

    private void adicionarAlternativa(String texto, boolean isCorreta) {
        HBox novaAlternativa = new HBox(10);
        novaAlternativa.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(alternativasToggleGroup);
        radioButton.setSelected(isCorreta);

        TextField textField = new TextField(texto);
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
        if (alternativaNodes.size() > 2) {
            alternativasContainer.getChildren().remove(alternativaNode);
            alternativaNodes.remove(alternativaNode);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aviso", "A questão deve ter no mínimo 2 alternativas.");
        }
    }

    @FXML
    private void handleSalvarQuestao() {
        if (!validarCampos()) return;

        // Se estiver editando, atualiza o objeto existente. Senão, cria um novo.
        Questao questao = (questaoParaEditar != null) ? questaoParaEditar : new Questao();

        questao.setDisciplinaId(disciplinaComboBox.getValue().getId());
        questao.setAssuntoId(assuntoComboBox.getValue().getId());
        questao.setNivelDificuldade(nivelDificuldadeComboBox.getValue());
        questao.setTipo(tipoQuestaoComboBox.getValue());
        questao.setEnunciado(enunciadoTextArea.getText());

        if (questaoParaEditar == null) { // Apenas na criação
            questao.setProfessorCriadorId(UserSession.getInstance().getLoggedInUser().getId());
        }

        if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            if (!validarAlternativas(questao)) return;
        }

        if (questaoParaEditar != null) {
            questaoDAO.update(questao);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Questão atualizada com sucesso!");
        } else {
            questaoDAO.save(questao);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Questão salva com sucesso!");
        }

        handleCancelar();
    }

    private boolean validarCampos() {
        if (disciplinaComboBox.getValue() == null || assuntoComboBox.getValue() == null ||
                nivelDificuldadeComboBox.getValue() == null || tipoQuestaoComboBox.getValue() == null ||
                enunciadoTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Todos os campos devem ser preenchidos.");
            return false;
        }
        return true;
    }

    private boolean validarAlternativas(Questao questao) {
        if (alternativasToggleGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Uma alternativa deve ser marcada como correta.");
            return false;
        }

        List<Alternativa> alternativas = alternativaNodes.stream().map(hbox -> {
            RadioButton rb = (RadioButton) hbox.getChildren().get(0);
            TextField tf = (TextField) hbox.getChildren().get(1);
            return new Alternativa(tf.getText(), rb.isSelected());
        }).collect(Collectors.toList());

        if (alternativas.stream().anyMatch(a -> a.getTexto().trim().isEmpty())) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O texto de todas as alternativas deve ser preenchido.");
            return false;
        }

        questao.setAlternativas(alternativas);
        return true;
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