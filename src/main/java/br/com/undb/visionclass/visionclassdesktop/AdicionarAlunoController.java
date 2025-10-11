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
        // Carrega os alunos disponíveis assim que a janela é aberta
        buscarAlunos();
    }

    @FXML
    public void initialize() {
        // 1. Configurar as colunas para exibir os dados dos alunos
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));

        // 2. Configurar o listener para a barra de busca
        buscaAlunoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarAlunos();
        });

        // 3. Configurar a coluna de ação para ter um botão "Adicionar"
        setupAcaoColumn();

        // 4. Ligar a lista observável à tabela
        alunosTableView.setItems(alunosDisponiveis);
    }

    private void buscarAlunos() {
        if (turmaAtual == null) return;
        String termoBusca = buscaAlunoTextField.getText();
        List<User> alunosEncontrados = userDAO.findAlunosNotInTurmaByName(termoBusca, turmaAtual.getId());
        alunosDisponiveis.setAll(alunosEncontrados); // Atualiza a lista da tabela
    }

    private void setupAcaoColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Adicionar");

                    {
                        btn.getStyleClass().add("primary-button"); // Adiciona um estilo se quiser
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

        // Remove o aluno da lista para não ser adicionado novamente
        alunosDisponiveis.remove(aluno);

        // Opcional: Mostrar um feedback para o usuário
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