package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AssuntoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Assunto;
import br.com.undb.visionclass.visionclassdesktop.model.Disciplina;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BancoQuestoesController {

    @FXML
    private ComboBox<Disciplina> disciplinaComboBox;
    @FXML
    private ComboBox<Assunto> assuntoComboBox;
    @FXML
    private TableView<Questao> questoesTableView;
    @FXML
    private TableColumn<Questao, Integer> idColumn;
    @FXML
    private TableColumn<Questao, String> enunciadoColumn;
    @FXML
    private TableColumn<Questao, String> disciplinaColumn;
    @FXML
    private TableColumn<Questao, String> assuntoColumn;
    @FXML
    private TableColumn<Questao, String> nivelColumn;
    @FXML
    private TableColumn<Questao, Void> acoesColumn;

    private DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    private AssuntoDAO assuntoDAO = new AssuntoDAO();
    private QuestaoDAO questaoDAO = new QuestaoDAO();

    private ObservableList<Questao> questoesList = FXCollections.observableArrayList();
    private List<Disciplina> todasAsDisciplinas;
    private List<Assunto> todosOsAssuntos;


    @FXML
    public void initialize() {
        setupTableColumns();
        loadInitialData();
        setupFilters();

        questoesTableView.setItems(questoesList);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        enunciadoColumn.setCellValueFactory(new PropertyValueFactory<>("enunciado"));
        nivelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNivelDificuldade().toString()));

        disciplinaColumn.setCellValueFactory(cellData -> {
            int disciplinaId = cellData.getValue().getDisciplinaId();
            String nomeDisciplina = todasAsDisciplinas.stream()
                    .filter(d -> d.getId() == disciplinaId)
                    .findFirst()
                    .map(Disciplina::getNome)
                    .orElse("Não encontrada");
            return new SimpleStringProperty(nomeDisciplina);
        });

        assuntoColumn.setCellValueFactory(cellData -> {
            int assuntoId = cellData.getValue().getAssuntoId();
            String nomeAssunto = todosOsAssuntos.stream()
                    .filter(a -> a.getId() == assuntoId)
                    .findFirst()
                    .map(Assunto::getNome)
                    .orElse("Não encontrado");
            return new SimpleStringProperty(nomeAssunto);
        });

        setupAcoesColumn();
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<Questao, Void>, TableCell<Questao, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Questao, Void> call(final TableColumn<Questao, Void> param) {
                return new TableCell<>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnRemover = new Button("Remover");
                    private final HBox pane = new HBox(btnEditar, btnRemover);

                    {
                        pane.setSpacing(10);
                        pane.setAlignment(Pos.CENTER);
                        btnEditar.getStyleClass().add("primary-button");
                        btnRemover.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Questao questao = getTableView().getItems().get(getIndex());
                            handleEditarQuestao(questao);
                        });

                        btnRemover.setOnAction(event -> {
                            Questao questao = getTableView().getItems().get(getIndex());
                            confirmarRemocao(questao);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void confirmarRemocao(Questao questao) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Questão ID: " + questao.getId());
        alert.setContentText("Tem a certeza de que deseja remover esta questão permanentemente?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            questaoDAO.delete(questao.getId());
            applyFiltersAndRefreshTable();
        }
    }

    private void loadInitialData() {
        todasAsDisciplinas = disciplinaDAO.findAll();
        todosOsAssuntos = todasAsDisciplinas.stream()
                .flatMap(d -> assuntoDAO.findByDisciplinaId(d.getId()).stream())
                .collect(Collectors.toList());

        disciplinaComboBox.getItems().clear();
        disciplinaComboBox.getItems().add(null);
        disciplinaComboBox.getItems().addAll(todasAsDisciplinas);

        applyFiltersAndRefreshTable();
    }

    private void applyFiltersAndRefreshTable() {
        Disciplina disciplinaSelecionada = disciplinaComboBox.getValue();
        Assunto assuntoSelecionado = assuntoComboBox.getValue();

        Integer disciplinaId = (disciplinaSelecionada != null) ? disciplinaSelecionada.getId() : null;
        Integer assuntoId = (assuntoSelecionado != null) ? assuntoSelecionado.getId() : null;

        questoesList.setAll(questaoDAO.findByFilters(disciplinaId, assuntoId));
    }

    private void setupFilters() {
        disciplinaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assuntoComboBox.getItems().clear();
            assuntoComboBox.setValue(null);
            if (newVal != null) {
                assuntoComboBox.getItems().add(null);
                assuntoComboBox.getItems().addAll(assuntoDAO.findByDisciplinaId(newVal.getId()));
            }
            applyFiltersAndRefreshTable();
        });

        assuntoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFiltersAndRefreshTable();
        });
    }

    @FXML
    private void handleNovaQuestao() {
        abrirFormularioQuestao(null);
    }

    private void handleEditarQuestao(Questao questao) {
        Questao questaoCompleta = questaoDAO.findById(questao.getId());
        if (questaoCompleta != null) {
            abrirFormularioQuestao(questaoCompleta);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível carregar a questão.");
            alert.setContentText("A questão com ID " + questao.getId() + " não foi encontrada no banco de dados.");
            alert.showAndWait();
        }
    }

    private void abrirFormularioQuestao(Questao questaoParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("questao-form-view.fxml"));
            Parent root = loader.load();

            QuestaoFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            if (questaoParaEditar != null) {
                stage.setTitle("Editar Questão");
                controller.setQuestaoParaEditar(questaoParaEditar);
            } else {
                stage.setTitle("Adicionar Nova Questão");
            }

            stage.showAndWait();

            loadInitialData();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de questão.");
            e.printStackTrace();
        }
    }
}