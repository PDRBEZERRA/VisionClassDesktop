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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimuladosAlunoController {

    @FXML
    private TableView<SimuladoTurmaWrapper> simuladosTableView;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, String> tituloColumn;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, String> turmaColumn;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, String> professorColumn;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, Number> questoesColumn;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, String> statusColumn;
    @FXML
    private TableColumn<SimuladoTurmaWrapper, Void> acoesColumn;

    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    private User alunoLogado;
    private List<Turma> turmasDoAluno;
    private Map<String, String> professoresNomesCache;

    private ObservableList<SimuladoTurmaWrapper> simuladosList = FXCollections.observableArrayList();

    public static class SimuladoTurmaWrapper {
        private Simulado simulado;
        private String nomesDasTurmas;

        public SimuladoTurmaWrapper(Simulado simulado, String nomesDasTurmas) {
            this.simulado = simulado;
            this.nomesDasTurmas = nomesDasTurmas;
        }

        public Simulado getSimulado() { return simulado; }
        public String getNomesDasTurmas() { return nomesDasTurmas; }
    }


    @FXML
    public void initialize() {
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        this.turmasDoAluno = turmaDAO.findAllByAlunoId(alunoLogado.getId());

        setupTableColumns();
        loadSimulados();
    }

    private void setupTableColumns() {
        tituloColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSimulado().getTitulo()));
        turmaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomesDasTurmas()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getSimuladoStatus(cellData.getValue().getSimulado())));

        questoesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(simuladoDAO.countQuestoesBySimuladoId(cellData.getValue().getSimulado().getId())));

        professorColumn.setCellValueFactory(cellData -> {
            String professorId = cellData.getValue().getSimulado().getProfessorCriadorId();
            String nome = professoresNomesCache.getOrDefault(professorId, "N/A");
            return new SimpleStringProperty(nome);
        });

        simuladosTableView.setItems(simuladosList);
        setupAcoesColumn();
    }

    private String getSimuladoStatus(Simulado simulado) {
        List<Integer> realizados = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(alunoLogado.getId());

        if (realizados.contains(simulado.getId())) {
            return "Finalizado";
        } else if (simulado.getStatus() == StatusSimulado.PUBLICADO) {
            return "Disponível";
        } else {
            return "Em Rascunho";
        }
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<SimuladoTurmaWrapper, Void>, TableCell<SimuladoTurmaWrapper, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnAcao = new Button();

            {
                btnAcao.getStyleClass().add("primary-button");

                btnAcao.setOnAction(event -> {
                    SimuladoTurmaWrapper wrapper = getTableView().getItems().get(getIndex());
                    handleAcaoSimulado(wrapper.getSimulado());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Simulado simulado = getTableRow().getItem().getSimulado();
                    setGraphic(btnAcao);
                    setAlignment(Pos.CENTER);

                    String status = getSimuladoStatus(simulado);

                    if (status.equals("Finalizado")) {
                        btnAcao.setText("Ver Nota");
                        btnAcao.setDisable(false);
                    } else if (status.equals("Disponível")) {
                        btnAcao.setText("Iniciar");
                        btnAcao.setDisable(false);
                    } else {
                        btnAcao.setText("...");
                        btnAcao.setDisable(true);
                    }
                }
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void handleAcaoSimulado(Simulado simulado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simulado-aluno-acao-view.fxml"));
            Parent root = loader.load();

            SimuladoAlunoViewOuExecController controller = loader.getController();
            controller.setSimulado(simulado);

            Stage modalStage = new Stage();
            modalStage.setTitle("Ação de Simulado: " + simulado.getTitulo());
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setResizable(false);

            modalStage.showAndWait();

            loadSimulados();

        } catch (IOException e) {
            System.err.println("Erro ao abrir a tela de ação do simulado: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de ação do simulado.");
        }
    }

    private void loadSimulados() {
        simuladosList.clear();

        if (turmasDoAluno == null || turmasDoAluno.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sem Turma", "O aluno não está associado a nenhuma turma. Não há simulados para exibir.");
            return;
        }


        Map<Integer, Simulado> simuladoMap = new HashMap<>();
        Map<Integer, List<Turma>> simuladoParaTurmasMap = new HashMap<>();

        for (Turma turma : turmasDoAluno) {
            List<Simulado> simuladosDaTurma = simuladoDAO.findSimuladosByTurmaId(turma.getId());
            for (Simulado s : simuladosDaTurma) {
                simuladoMap.putIfAbsent(s.getId(), s);
                simuladoParaTurmasMap.putIfAbsent(s.getId(), new ArrayList<>());
                simuladoParaTurmasMap.get(s.getId()).add(turma);
            }
        }

        professoresNomesCache = simuladoMap.values().stream()
                .map(Simulado::getProfessorCriadorId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userDAO.findById(id).getNome()
                ));

        List<SimuladoTurmaWrapper> wrappers = new ArrayList<>();
        for (Simulado simulado : simuladoMap.values()) {
            List<Turma> turmasDesteSimulado = simuladoParaTurmasMap.get(simulado.getId());

            String nomesDasTurmas;
            if (turmasDesteSimulado.size() > 1) {
                nomesDasTurmas = "Várias Turmas";
            } else {
                nomesDasTurmas = turmasDesteSimulado.get(0).getNome();
            }

            wrappers.add(new SimuladoTurmaWrapper(simulado, nomesDasTurmas));
        }

        wrappers.sort(Comparator.comparing((SimuladoTurmaWrapper w) -> w.getSimulado().getDataCriacao()).reversed());

        simuladosList.setAll(wrappers);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}