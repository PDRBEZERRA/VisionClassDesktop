package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.*;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimuladoFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField tituloTextField;
    @FXML
    private TableView<Questao> questoesTableView;
    @FXML
    private TableColumn<Questao, CheckBox> selecionarQuestaoColumn;
    @FXML
    private TableColumn<Questao, String> enunciadoColumn;
    @FXML
    private TableColumn<Questao, String> disciplinaColumn;
    @FXML
    private TableColumn<Questao, String> nivelColumn;
    @FXML
    private TableView<Turma> turmasTableView;
    @FXML
    private TableColumn<Turma, CheckBox> selecionarTurmaColumn;
    @FXML
    private TableColumn<Turma, String> nomeTurmaColumn;

    private QuestaoDAO questaoDAO = new QuestaoDAO();
    private DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private SimuladoDAO simuladoDAO = new SimuladoDAO();

    private ObservableList<Questao> questoesList = FXCollections.observableArrayList();
    private ObservableList<Turma> turmasList = FXCollections.observableArrayList();

    private final Set<Questao> questoesSelecionadas = new HashSet<>();
    private final Set<Turma> turmasSelecionadas = new HashSet<>();
    private Simulado simuladoParaEditar;

    @FXML
    public void initialize() {
        setupQuestoesTable();
        setupTurmasTable();
        loadData();
    }

    public void setSimuladoParaEditar(Simulado simulado) {
        this.simuladoParaEditar = simulado;
        titleLabel.setText("Editar Simulado");
        tituloTextField.setText(simulado.getTitulo());

    }

    private void setupQuestoesTable() {
        selecionarQuestaoColumn.setCellFactory(param -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    Questao questao = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        questoesSelecionadas.add(questao);
                    } else {
                        questoesSelecionadas.remove(questao);
                    }
                });
            }
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Questao questao = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(questoesSelecionadas.contains(questao));
                    setGraphic(checkBox);
                }
            }
        });

        enunciadoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnunciado()));
        nivelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNivelDificuldade().toString()));

        List<Disciplina> disciplinas = disciplinaDAO.findAll();
        disciplinaColumn.setCellValueFactory(cellData -> {
            String nome = disciplinas.stream()
                    .filter(d -> d.getId() == cellData.getValue().getDisciplinaId())
                    .findFirst().map(Disciplina::getNome).orElse("N/A");
            return new SimpleStringProperty(nome);
        });

        questoesTableView.setItems(questoesList);
    }

    private void setupTurmasTable() {
        selecionarTurmaColumn.setCellFactory(param -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    Turma turma = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        turmasSelecionadas.add(turma);
                    } else {
                        turmasSelecionadas.remove(turma);
                    }
                });
            }
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Turma turma = getTableView().getItems().get(getIndex());
                    // Verifica se a turma já estava selecionada (modo edição)
                    checkBox.setSelected(turmasSelecionadas.contains(turma));
                    setGraphic(checkBox);
                }
            }
        });

        nomeTurmaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        turmasTableView.setItems(turmasList);
    }

    private void loadData() {
        questoesList.setAll(questaoDAO.findByFilters(null, null));
        String professorId = UserSession.getInstance().getLoggedInUser().getId();
        turmasList.setAll(turmaDAO.findByProfessorId(professorId));
    }

    @FXML
    private void handleSalvarSimulado() {
        String titulo = tituloTextField.getText().trim();
        if (titulo.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "O título do simulado não pode estar vazio.");
            return;
        }

        if (questoesSelecionadas.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Selecione pelo menos uma questão para o simulado.");
            return;
        }

        if (turmasSelecionadas.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Selecione pelo menos uma turma para aplicar o simulado.");
            return;
        }

        if (simuladoParaEditar != null) {
            simuladoParaEditar.setTitulo(titulo);
            simuladoParaEditar.setQuestoes(new ArrayList<>(questoesSelecionadas));
            simuladoParaEditar.setTurmas(new ArrayList<>(turmasSelecionadas));


            simuladoDAO.update(simuladoParaEditar);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Simulado \"" + titulo + "\" atualizado com sucesso.");
        } else {
            Simulado novoSimulado = new Simulado();
            novoSimulado.setTitulo(titulo);
            novoSimulado.setDataCriacao(LocalDate.now());

            novoSimulado.setStatus(StatusSimulado.PUBLICADO);

            novoSimulado.setProfessorCriadorId(UserSession.getInstance().getLoggedInUser().getId());
            novoSimulado.setQuestoes(new ArrayList<>(questoesSelecionadas));
            novoSimulado.setTurmas(new ArrayList<>(turmasSelecionadas));

            simuladoDAO.save(novoSimulado);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Simulado \"" + titulo + "\" criado com sucesso.");
        }

        handleCancelar();
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) tituloTextField.getScene().getWindow();
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