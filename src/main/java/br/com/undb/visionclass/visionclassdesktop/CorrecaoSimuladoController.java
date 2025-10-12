package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CorrecaoSimuladoController {

    @FXML
    private Label tituloSimuladoLabel;
    @FXML
    private ListView<User> alunosListView;
    @FXML
    private VBox detalhesCorrecaoContainer;
    @FXML
    private Label nomeAlunoDetalheLabel;
    @FXML
    private Label statusCorrecaoLabel;
    @FXML
    private Label notaAtualLabel;
    @FXML
    private ListView<QuestaoResposta> questoesDiscursivasListView;
    @FXML
    private Label mensagemFeedback;

    private Simulado simuladoAtual;
    private UserDAO userDAO = new UserDAO();
    private QuestaoDAO questaoDAO = new QuestaoDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    // Classe interna auxiliar (Mantida como estava)
    private static class QuestaoResposta {
        Questao questao;
        String respostaDiscursiva;

        public QuestaoResposta(Questao questao, String respostaDiscursiva) {
            this.questao = questao;
            this.respostaDiscursiva = respostaDiscursiva;
        }

        @Override
        public String toString() {
            String enunciadoCurto = questao.getEnunciado().substring(0, Math.min(questao.getEnunciado().length(), 50)) + "...";
            return enunciadoCurto + " | Resposta: " + (respostaDiscursiva.length() > 20 ? respostaDiscursiva.substring(0, 20) + "..." : respostaDiscursiva);
        }
    }

    @FXML
    public void initialize() {
        // Formato customizado para a lista de alunos
        alunosListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<User> call(ListView<User> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(User aluno, boolean empty) {
                        super.updateItem(aluno, empty);
                        if (empty || aluno == null) {
                            setText(null);
                        } else {
                            setText(aluno.getNome() + " (" + aluno.getMatricula() + ")");
                        }
                    }
                };
            }
        });

        // Listener 1: Seleção de Aluno (Carrega o painel direito)
        alunosListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarDetalhesCorrecao(newVal);
            }
        });

        // Listener 2: Duplo Clique na Questão (Abre o modal de correção)
        questoesDiscursivasListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                QuestaoResposta questaoResposta = questoesDiscursivasListView.getSelectionModel().getSelectedItem();
                User alunoSelecionado = alunosListView.getSelectionModel().getSelectedItem();

                if (questaoResposta != null && alunoSelecionado != null) {
                    handleAbrirCorrecaoDetalhada(questaoResposta, alunoSelecionado);
                }
            }
        });

        setDetalhesPanelDisabled(true);
    }

    public void setSimulado(Simulado simulado) {
        this.simuladoAtual = simulado;
        tituloSimuladoLabel.setText("Correção: " + simulado.getTitulo());
        loadAlunosComRespostas();
    }

    private void setDetalhesPanelDisabled(boolean disabled) {
        detalhesCorrecaoContainer.setDisable(disabled);
        nomeAlunoDetalheLabel.setVisible(!disabled);
    }

    private void loadAlunosComRespostas() {
        List<String> alunosIds = alunoRespostaDAO.findAlunosIdsBySimuladoId(simuladoAtual.getId());
        List<User> alunos = alunosIds.stream()
                .map(id -> userDAO.findById(id))
                .filter(user -> user != null)
                .collect(Collectors.toList());

        alunosListView.getItems().setAll(alunos);

        if (alunos.isEmpty()) {
            mensagemFeedback.setText("Nenhum aluno submeteu respostas para este simulado.");
        } else {
            mensagemFeedback.setText("Selecione um aluno para iniciar a correção.");
        }
    }

    // --- LÓGICA DE RECARGA: Chama a busca no DAO novamente ---
    public void carregarDetalhesCorrecao(User aluno) {
        setDetalhesPanelDisabled(false);
        nomeAlunoDetalheLabel.setText("Corrigindo: " + aluno.getNome());

        // 1. NOTA ATUAL: Esta chamada refaz a consulta no AlunoRespostaDAO.getNotaByAlunoAndSimulado
        double notaParcial = alunoRespostaDAO.getNotaByAlunoAndSimulado(aluno.getId(), simuladoAtual.getId());

        if (notaParcial < 0) {
            notaAtualLabel.setText("Nota Parcial: 0.0%");
        } else {
            // A nota deve ser atualizada com o novo valor do banco
            notaAtualLabel.setText(String.format("Nota Parcial: %.2f%%", notaParcial));
        }

        // 2. Carrega as questões discursivas (para manter a lista atualizada)
        Map<Integer, String> respostasDiscursivas = alunoRespostaDAO.findDiscursiveAnswers(aluno.getId(), simuladoAtual.getId());
        ObservableList<QuestaoResposta> listaDeCorrecao = FXCollections.observableArrayList();

        for (Map.Entry<Integer, String> entry : respostasDiscursivas.entrySet()) {
            Questao questao = questaoDAO.findById(entry.getKey());
            if (questao != null) {
                listaDeCorrecao.add(new QuestaoResposta(questao, entry.getValue()));
            }
        }

        questoesDiscursivasListView.getItems().setAll(listaDeCorrecao);

        // 3. Status de Correção
        if (listaDeCorrecao.isEmpty()) {
            statusCorrecaoLabel.setText("Status: Sem questões discursivas para corrigir.");
        } else {
            statusCorrecaoLabel.setText("Status: Pronta para Correção Manual.");
        }
    }

    // --- LÓGICA DE ABERTURA DO MODAL ---
    private void handleAbrirCorrecaoDetalhada(QuestaoResposta qr, User aluno) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("correcao-detalhe-view.fxml"));
            Parent root = loader.load();

            CorrecaoDetalheController controller = loader.getController();

            // Passa TODOS os dados necessários para o modal
            controller.setDadosCorrecao(simuladoAtual, aluno, qr.questao, qr.respostaDiscursiva);

            Stage modalStage = new Stage();
            modalStage.setTitle("Corrigir Questão " + qr.questao.getId());
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setResizable(false);

            // AQUI OCORRE A ESPERA, e o código retoma quando o modal é fechado
            modalStage.showAndWait();

            // AÇÃO FINAL: Recarrega os detalhes do aluno
            // Isto obriga o método carregarDetalhesCorrecao() a consultar a nova nota do DB.
            carregarDetalhesCorrecao(aluno);

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de correção detalhada.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar o formulário de correção.");
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