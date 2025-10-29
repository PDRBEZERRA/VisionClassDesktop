package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;

public class AdicionarAlunoController {

    @FXML
    private Label nomeTurmaLabel;
    @FXML
    private TextField buscaAlunoTextField;
    @FXML
    private TableView<User> alunosTableView;
    @FXML
    private TableColumn<User, String> matriculaColumn;
    @FXML
    private TableColumn<User, String> nomeColumn;
    @FXML
    private TableColumn<User, Void> acaoColumn;

    private Turma turmaAtual;
    private UserDAO userDAO = new UserDAO();
    private TurmaDAO turmaDAO = new TurmaDAO();
    private ObservableList<User> alunosDisponiveis = FXCollections.observableArrayList();

    public void setTurma(Turma turma) {
        this.turmaAtual = turma;
        nomeTurmaLabel.setText("Buscando alunos para adicionar à turma: " + turma.getNome());
        buscarAlunos();
    }

    @FXML
    public void initialize() {
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));

        buscaAlunoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarAlunos();
        });

        setupAcaoColumn();

        alunosTableView.setItems(alunosDisponiveis);
    }

    private void buscarAlunos() {
        if (turmaAtual == null) return;
        String termoBusca = buscaAlunoTextField.getText();
        List<User> alunosEncontrados = userDAO.findAlunosNotInTurmaByName(termoBusca, turmaAtual.getId());
        alunosDisponiveis.setAll(alunosEncontrados);
    }

    private void setupAcaoColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Adicionar");

                    {
                        btn.getStyleClass().add("primary-button");
                        btn.setOnAction(event -> {
                            User aluno = getTableView().getItems().get(getIndex());
                            adicionarAlunoNaTurma(aluno);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
                return cell;
            }
        };

        acaoColumn.setCellFactory(cellFactory);
    }

    private void adicionarAlunoNaTurma(User aluno) {
        System.out.println("Adicionando aluno " + aluno.getNome() + " à turma " + turmaAtual.getNome());
        turmaDAO.addAlunoToTurma(turmaAtual.getId(), aluno.getId());

        alunosDisponiveis.remove(aluno);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText("Aluno " + aluno.getNome() + " adicionado com sucesso!");
        alert.showAndWait();
    }

    @FXML
    private void onFecharButtonClick() {
        Stage stage = (Stage) nomeTurmaLabel.getScene().getWindow();
        stage.close();
    }
}