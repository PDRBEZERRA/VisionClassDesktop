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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
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

    private ObservableList<Questao> todasAsQuestoes = FXCollections.observableArrayList();
    private List<Disciplina> todasAsDisciplinas;
    private List<Assunto> todosOsAssuntos;


    @FXML
    public void initialize() {
        setupTableColumns();
        loadInitialData();
        setupFilters();

        questoesTableView.setItems(todasAsQuestoes);
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
    }

    private void loadInitialData() {
        todasAsDisciplinas = disciplinaDAO.findAll();
        todosOsAssuntos = todasAsDisciplinas.stream()
                .flatMap(d -> assuntoDAO.findByDisciplinaId(d.getId()).stream())
                .collect(Collectors.toList());

        disciplinaComboBox.getItems().clear();
        disciplinaComboBox.getItems().add(null);
        disciplinaComboBox.getItems().addAll(todasAsDisciplinas);

        refreshTable();
    }

    // Método para recarregar apenas os dados da tabela
    private void refreshTable() {
        todasAsQuestoes.setAll(questaoDAO.findAll());
    }

    private void setupFilters() {
        disciplinaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assuntoComboBox.getItems().clear();
            if (newVal != null) {
                assuntoComboBox.getItems().add(null);
                assuntoComboBox.getItems().addAll(assuntoDAO.findByDisciplinaId(newVal.getId()));
            }
        });
    }

    // --- LÓGICA ATUALIZADA AQUI ---
    @FXML
    private void handleNovaQuestao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("questao-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Adicionar Nova Questão");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Mostra a janela e espera ela ser fechada
            stage.showAndWait();

            // Após fechar, atualiza a tabela para mostrar a nova questão
            refreshTable();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de nova questão.");
            e.printStackTrace();
        }
    }
}