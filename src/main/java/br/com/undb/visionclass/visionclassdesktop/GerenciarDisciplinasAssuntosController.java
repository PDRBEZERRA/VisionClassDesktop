package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AssuntoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.DisciplinaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Assunto;
import br.com.undb.visionclass.visionclassdesktop.model.Disciplina;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class GerenciarDisciplinasAssuntosController {

    @FXML
    private ListView<Disciplina> disciplinasListView;
    @FXML
    private Label assuntosLabel;
    @FXML
    private ListView<Assunto> assuntosListView;
    @FXML
    private Button novoAssuntoButton;

    private DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    private AssuntoDAO assuntoDAO = new AssuntoDAO();

    private ObservableList<Disciplina> disciplinasList = FXCollections.observableArrayList();
    private ObservableList<Assunto> assuntosList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Vincula as listas observáveis às ListViews
        disciplinasListView.setItems(disciplinasList);
        assuntosListView.setItems(assuntosList);

        // Listener para quando uma disciplina é selecionada
        disciplinasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Se uma disciplina for selecionada, carrega seus assuntos
                assuntosLabel.setText("Assuntos de " + newVal.getNome());
                novoAssuntoButton.setDisable(false); // Habilita o botão "+ Novo Assunto"
                carregarAssuntos(newVal.getId());
            } else {
                // Se nada for selecionado, limpa a lista de assuntos
                assuntosLabel.setText("Assuntos");
                novoAssuntoButton.setDisable(true); // Desabilita o botão
                assuntosList.clear();
            }
        });

        carregarDisciplinas();
    }

    private void carregarDisciplinas() {
        disciplinasList.setAll(disciplinaDAO.findAll());
    }

    private void carregarAssuntos(int disciplinaId) {
        assuntosList.setAll(assuntoDAO.findByDisciplinaId(disciplinaId));
    }

    @FXML
    private void handleNovaDisciplina() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nova Disciplina");
        dialog.setHeaderText("Adicionar uma nova disciplina ao banco de questões");
        dialog.setContentText("Nome da Disciplina:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nome -> {
            if (!nome.trim().isEmpty()) {
                Disciplina novaDisciplina = new Disciplina();
                novaDisciplina.setNome(nome.trim());
                disciplinaDAO.save(novaDisciplina);
                carregarDisciplinas(); // Atualiza a lista
            }
        });
    }

    @FXML
    private void handleRemoverDisciplina() {
        Disciplina selecionada = disciplinasListView.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione uma disciplina para remover.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Disciplina: " + selecionada.getNome());
        confirmacao.setContentText("Atenção! Esta ação é irreversível e removerá todos os assuntos e questões associados a esta disciplina. Deseja continuar?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            disciplinaDAO.delete(selecionada.getId());
            carregarDisciplinas(); // Atualiza a lista de disciplinas (e a de assuntos será limpa pelo listener)
        }
    }

    @FXML
    private void handleNovoAssunto() {
        Disciplina disciplinaSelecionada = disciplinasListView.getSelectionModel().getSelectedItem();
        if (disciplinaSelecionada == null) return; // Segurança extra

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Novo Assunto");
        dialog.setHeaderText("Adicionar um novo assunto para a disciplina: " + disciplinaSelecionada.getNome());
        dialog.setContentText("Nome do Assunto:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nome -> {
            if (!nome.trim().isEmpty()) {
                Assunto novoAssunto = new Assunto();
                novoAssunto.setNome(nome.trim());
                novoAssunto.setDisciplinaId(disciplinaSelecionada.getId());
                assuntoDAO.save(novoAssunto);
                carregarAssuntos(disciplinaSelecionada.getId()); // Atualiza a lista
            }
        });
    }

    @FXML
    private void handleRemoverAssunto() {
        Assunto selecionado = assuntosListView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Seleção", "Por favor, selecione um assunto para remover.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Assunto: " + selecionado.getNome());
        confirmacao.setContentText("Atenção! Esta ação removerá todas as questões associadas a este assunto. Deseja continuar?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            assuntoDAO.delete(selecionado.getId());
            carregarAssuntos(selecionado.getDisciplinaId()); // Atualiza a lista de assuntos
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}