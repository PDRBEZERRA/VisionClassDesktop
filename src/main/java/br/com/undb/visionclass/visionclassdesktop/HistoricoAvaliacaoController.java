package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.model.AvaliacaoComportamental;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoricoAvaliacaoController {

    @FXML
    private Label nomeAlunoLabel;
    @FXML
    private ListView<AvaliacaoComportamental> historicoListView;

    private User aluno;
    private Turma turma;
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();

    public void setData(User aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
        nomeAlunoLabel.setText("Aluno: " + aluno.getNome());
        carregarHistorico();
    }

    @FXML
    public void initialize() {
        // Configura como cada célula da lista será exibida
        historicoListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<AvaliacaoComportamental> call(ListView<AvaliacaoComportamental> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(AvaliacaoComportamental avaliacao, boolean empty) {
                        super.updateItem(avaliacao, empty);
                        if (empty || avaliacao == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Cria a aparência customizada para cada item
                            VBox vbox = new VBox(5); // Espaçamento de 5px
                            vbox.setStyle("-fx-padding: 10px;");

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            Label dataLabel = new Label("Data: " + avaliacao.getData().format(formatter));
                            dataLabel.setStyle("-fx-font-weight: bold;");

                            Label mediaLabel = new Label("Média Comportamental: " + avaliacao.getMediaComportamental());
                            Label obsLabel = new Label("Observações: " + (avaliacao.getObservacoes().isEmpty() ? "Nenhuma" : avaliacao.getObservacoes()));
                            obsLabel.setWrapText(true); // Quebra a linha se a observação for longa

                            vbox.getChildren().addAll(dataLabel, mediaLabel, obsLabel);
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });
    }

    private void carregarHistorico() {
        List<AvaliacaoComportamental> historico = avaliacaoDAO.findAllByAlunoAndTurma(aluno.getId(), turma.getId());
        historicoListView.getItems().setAll(historico);
    }

    @FXML
    private void handleFechar() {
        Stage stage = (Stage) nomeAlunoLabel.getScene().getWindow();
        stage.close();
    }
}