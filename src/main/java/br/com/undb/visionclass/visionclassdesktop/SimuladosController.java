package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
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
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SimuladosController {

    @FXML
    private TableView<Simulado> simuladosTableView;
    @FXML
    private TableColumn<Simulado, String> tituloColumn;
    @FXML
    private TableColumn<Simulado, String> dataCriacaoColumn;
    @FXML
    private TableColumn<Simulado, String> statusColumn;
    @FXML
    private TableColumn<Simulado, Number> questoesColumn;
    @FXML
    private TableColumn<Simulado, Void> acoesColumn;

    private SimuladoDAO simuladoDAO = new SimuladoDAO();
    private ObservableList<Simulado> simuladosList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSimulados();
    }

    private void setupTableColumns() {
        tituloColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dataCriacaoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataCriacao().format(formatter)));

        // --- COLUNA DE QUESTÕES CORRIGIDA ---
        questoesColumn.setCellValueFactory(cellData -> {
            int count = simuladoDAO.countQuestoesBySimuladoId(cellData.getValue().getId());
            return new SimpleIntegerProperty(count);
        });

        simuladosTableView.setItems(simuladosList);

        // --- LÓGICA DE EXCLUSÃO ---
        setupAcoesColumn();
    }

    private void setupAcoesColumn() {
        Callback<TableColumn<Simulado, Void>, TableCell<Simulado, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Simulado, Void> call(final TableColumn<Simulado, Void> param) {
                return new TableCell<>() {
                    private final Button btnExcluir = new Button("Excluir");

                    {
                        btnExcluir.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                        btnExcluir.setOnAction(event -> {
                            Simulado simulado = getTableView().getItems().get(getIndex());
                            confirmarExclusao(simulado);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnExcluir);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        };
        acoesColumn.setCellFactory(cellFactory);
    }

    private void confirmarExclusao(Simulado simulado) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir o simulado \"" + simulado.getTitulo() + "\"?");
        alert.setContentText("Esta ação é irreversível.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            simuladoDAO.delete(simulado.getId());
            loadSimulados(); // Atualiza a tabela
        }
    }

    private void loadSimulados() {
        String professorId = UserSession.getInstance().getLoggedInUser().getId();
        simuladosList.setAll(simuladoDAO.findByProfessorId(professorId));
    }

    @FXML
    private void handleNovoSimulado() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simulado-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Criar Novo Simulado");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.showAndWait();

            loadSimulados();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de novo simulado.");
            e.printStackTrace();
        }
    }
}