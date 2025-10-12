package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.*;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class SimuladoFormController {

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

    // --- LÓGICA DE SELEÇÃO CORRIGIDA ---
    // Usaremos Sets para armazenar os itens selecionados de forma confiável
    private final Set<Questao> questoesSelecionadas = new HashSet<>();
    private final Set<Turma> turmasSelecionadas = new HashSet<>();

    @FXML
    public void initialize() {
        setupQuestoesTable();
        setupTurmasTable();
        loadData();
    }

    private void setupQuestoesTable() {
        // Usamos CellFactory para ter controle sobre a célula e adicionar um listener
        selecionarQuestaoColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Questao, CheckBox> call(TableColumn<Questao, CheckBox> param) {
                return new TableCell<>() {
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
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });

        enunciadoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnunciado()));
        nivelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNivelDificuldade().toString()));

        List<Disciplina> disciplinas = disciplinaDAO.findAll();
        disciplinaColumn.setCellValueFactory(cellData -> {
            String nome = disciplinas.stream()
                    .filter(d -> d.getId() == cellData.getValue().getDisciplinaId())
                    .findFirst()
                    .map(Disciplina::getNome)
                    .orElse("N/A");
            return new SimpleStringProperty(nome);
        });

        questoesTableView.setItems(questoesList);
    }

    private void setupTurmasTable() {
        selecionarTurmaColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Turma, CheckBox> call(TableColumn<Turma, CheckBox> param) {
                return new TableCell<>() {
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
                            setGraphic(checkBox);
                        }
                    }
                };
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

        Simulado novoSimulado = new Simulado();
        novoSimulado.setTitulo(titulo);
        novoSimulado.setDataCriacao(LocalDate.now());
        novoSimulado.setStatus(StatusSimulado.RASCUNHO);
        novoSimulado.setProfessorCriadorId(UserSession.getInstance().getLoggedInUser().getId());
        novoSimulado.setQuestoes(new ArrayList<>(questoesSelecionadas));
        novoSimulado.setTurmas(new ArrayList<>(turmasSelecionadas));

        simuladoDAO.save(novoSimulado);

        showAlert(Alert.AlertType.INFORMATION, "Sucesso!", "Simulado \"" + titulo + "\" criado com sucesso.");
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