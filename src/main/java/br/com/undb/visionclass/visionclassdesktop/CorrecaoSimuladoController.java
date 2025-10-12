package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO; // Novo DAO necessário
import br.com.undb.visionclass.visionclassdesktop.dao.SimuladoDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Questao; // Novo Model necessário
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

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
    private QuestaoDAO questaoDAO = new QuestaoDAO(); // DAO para buscar o enunciado
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    // Classe interna auxiliar para exibir a questão e a resposta
    private static class QuestaoResposta {
        Questao questao;
        String respostaDiscursiva;
        // String notaAtribuida; // Futuramente, para salvar a nota.

        public QuestaoResposta(Questao questao, String respostaDiscursiva) {
            this.questao = questao;
            this.respostaDiscursiva = respostaDiscursiva;
        }

        @Override
        public String toString() {
            // Exibição na lista: Enunciado (primeiras 50 letras)
            String enunciadoCurto = questao.getEnunciado().substring(0, Math.min(questao.getEnunciado().length(), 50)) + "...";
            return enunciadoCurto + " | Resposta: " + (respostaDiscursiva.length() > 20 ? respostaDiscursiva.substring(0, 20) + "..." : respostaDiscursiva);
        }
    }

    @FXML
    public void initialize() {
        // Formato customizado para a lista de alunos (Nome e matrícula)
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

        // Listener para carregar os detalhes do aluno selecionado
        alunosListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarDetalhesCorrecao(newVal);
            }
        });

        // Desabilita o painel de detalhes por padrão
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
        // 1. Busca os IDs dos alunos que responderam a este simulado
        List<String> alunosIds = alunoRespostaDAO.findAlunosIdsBySimuladoId(simuladoAtual.getId());

        // 2. Converte os IDs para objetos User (para exibir o nome)
        List<User> alunos = alunosIds.stream()
                .map(id -> userDAO.findById(id))
                .filter(user -> user != null)
                .collect(Collectors.toList());

        alunosListView.getItems().setAll(alunos);

        if (alunos.isEmpty()) {
            // Exibe mensagem de feedback se nenhum aluno tiver respondido
            mensagemFeedback.setText("Nenhum aluno submeteu respostas para este simulado.");
        } else {
            mensagemFeedback.setText("Selecione um aluno para iniciar a correção.");
        }
    }

    private void carregarDetalhesCorrecao(User aluno) {
        setDetalhesPanelDisabled(false);
        nomeAlunoDetalheLabel.setText("Corrigindo: " + aluno.getNome());

        // 1. Nota Atual (só mult. escolha, discursiva precisa de nota)
        double notaParcial = alunoRespostaDAO.getNotaByAlunoAndSimulado(aluno.getId(), simuladoAtual.getId());

        if (notaParcial < 0) {
            notaAtualLabel.setText("Nota Parcial: 0.0%");
        } else {
            notaAtualLabel.setText(String.format("Nota Parcial: %.2f%%", notaParcial));
        }

        // 2. Carrega as questões discursivas respondidas
        Map<Integer, String> respostasDiscursivas = alunoRespostaDAO.findDiscursiveAnswers(aluno.getId(), simuladoAtual.getId());

        ObservableList<QuestaoResposta> listaDeCorrecao = FXCollections.observableArrayList();

        // Converte o Map de ID/Resposta em objetos QuestaoResposta (precisa do enunciado)
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
}