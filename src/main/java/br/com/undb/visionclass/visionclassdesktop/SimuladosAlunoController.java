package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.StatusSimulado;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimuladosAlunoController {

    @FXML
    private TableView<Simulado> simuladosTableView;
    @FXML
    private TableColumn<Simulado, String> tituloColumn;
    @FXML
    private TableColumn<Simulado, String> professorColumn;
    @FXML
    private TableColumn<Simulado, Number> questoesColumn;
    @FXML
    private TableColumn<Simulado, String> statusColumn;
    @FXML
    private TableColumn<Simulado, Void> acoesColumn;

    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();

    private User alunoLogado;
    private Turma turmaDoAluno;
    private Map<String, String> professoresNomesCache; // Cache para nomes de professores

    private ObservableList<Simulado> simuladosList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        // 1. Encontra a turma do aluno (Necessário para a busca)
        this.turmaDoAluno = turmaDAO.findByAlunoId(alunoLogado.getId());

        setupTableColumns();
        loadSimulados();
    }

    private void setupTableColumns() {
        tituloColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getSimuladoStatus(cellData.getValue())));

        // Usa o countQuestoesBySimuladoId que está no SimuladoDAO
        questoesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(simuladoDAO.countQuestoesBySimuladoId(cellData.getValue().getId())));

        // Busca o nome do professor a partir do ID
        professorColumn.setCellValueFactory(cellData -> {
            String professorId = cellData.getValue().getProfessorCriadorId();
            // Utiliza o cache para evitar múltiplas chamadas ao banco de dados
            String nome = professoresNomesCache.getOrDefault(professorId, "N/A");
            return new SimpleStringProperty(nome);
        });

        simuladosTableView.setItems(simuladosList);
        setupAcoesColumn();
    }

    // Método para determinar o status do simulado (Disponível, Feito, etc.)
    private String getSimuladoStatus(Simulado simulado) {
        // A lógica completa de Status (Em Andamento, Finalizado, etc.)
        // requer a implementação do SimuladoAlunoRespostaDAO.
        // Por enquanto, apenas checa se foi realizado.
        List<Integer> realizados = new AlunoRespostaDAO().findSimuladosRealizadosIdsByAluno(alunoLogado.getId());

        if (realizados.contains(simulado.getId())) {
            // Se foi realizado, precisa de lógica para saber se está 'Corrigido'/'Finalizado'
            return "Finalizado";
        } else if (simulado.getStatus() == StatusSimulado.PUBLICADO) {
            return "Disponível";
        } else {
            return "Em Rascunho";
        }
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<Simulado, Void>, TableCell<Simulado, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnAcao = new Button("Iniciar / Ver Nota");

            {
                btnAcao.getStyleClass().add("primary-button");
                btnAcao.setOnAction(event -> {
                    Simulado simulado = getTableView().getItems().get(getIndex());
                    handleAcaoSimulado(simulado);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnAcao);
                    setAlignment(Pos.CENTER);
                    // Lógica para mudar o texto do botão
                    String status = getSimuladoStatus(getTableView().getItems().get(getIndex()));
                    if (status.equals("Disponível")) {
                        btnAcao.setText("Iniciar");
                    } else {
                        btnAcao.setText("Ver Nota");
                    }
                }
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void handleAcaoSimulado(Simulado simulado) {
        // Alerta simples para simular a ação (a tela de execução do simulado não existe)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ação de Simulado");
        alert.setHeaderText("Simulado: " + simulado.getTitulo());
        alert.setContentText("A funcionalidade de Iniciar/Ver Nota para este simulado (ID: " + simulado.getId() + ") será implementada em breve.");
        alert.showAndWait();
    }

    private void loadSimulados() {
        simuladosList.clear();

        if (turmaDoAluno == null) {
            showAlert(Alert.AlertType.WARNING, "Sem Turma", "O aluno não está associado a nenhuma turma. Não há simulados para exibir.");
            return;
        }

        List<Simulado> simulados = simuladoDAO.findSimuladosByTurmaId(turmaDoAluno.getId());

        // Pré-carrega os nomes dos professores
        professoresNomesCache = simulados.stream()
                .map(Simulado::getProfessorCriadorId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userDAO.findById(id).getNome()
                ));

        simuladosList.setAll(simulados);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}