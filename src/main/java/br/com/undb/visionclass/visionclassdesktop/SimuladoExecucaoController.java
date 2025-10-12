package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Alternativa;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.TipoQuestao;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimuladoExecucaoController {

    @FXML
    private Label tituloLabel;
    @FXML
    private Label progressoLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label enunciadoLabel;
    @FXML
    private VBox respostaContainer;
    @FXML
    private Button btnAnterior;
    @FXML
    private Button btnProximo;
    @FXML
    private Button btnFinalizar;

    private List<Questao> questoes;
    private int indiceQuestaoAtual = 0;
    private Simulado simulado;
    private User alunoLogado;

    // Armazena as respostas do aluno: <QuestaoId, RespostaCapturada>
    // RespostaCapturada pode ser: Integer (AlternativaID) ou String (Texto Discursivo)
    private Map<Integer, Object> respostas = new HashMap<>();

    private LocalDateTime dataInicio;

    // Grupo para as alternativas (garante que apenas uma seja selecionada)
    private ToggleGroup alternativaToggleGroup = new ToggleGroup();

    // --- MÉTODOS DE INICIALIZAÇÃO ---

    public void setDadosSimulado(Simulado simulado, List<Questao> questoes) {
        this.simulado = simulado;
        this.questoes = questoes;
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        this.dataInicio = LocalDateTime.now(); // Marca o início do simulado

        tituloLabel.setText("Simulado: " + simulado.getTitulo());

        // Inicializa o mapa de respostas com valores nulos para todas as questões
        for (Questao q : questoes) {
            respostas.put(q.getId(), null);
        }

        mostrarQuestao(0);
    }

    @FXML
    public void initialize() {
        // Inicialização padrão
    }

    // --- LÓGICA DE NAVEGAÇÃO E RENDERIZAÇÃO ---

    private void mostrarQuestao(int indice) {
        if (indice < 0 || indice >= questoes.size()) return;

        // 1. Salva a resposta da questão anterior antes de mudar
        salvarRespostaAtual();

        this.indiceQuestaoAtual = indice;
        Questao q = questoes.get(indice);

        // 2. Atualiza UI de Progresso
        progressoLabel.setText(String.format("Questão %d de %d", indice + 1, questoes.size()));
        progressBar.setProgress((double) (indice + 1) / questoes.size());
        enunciadoLabel.setText(q.getEnunciado());

        // 3. Renderiza o container de respostas
        renderizarRespostaContainer(q);

        // 4. Carrega a resposta previamente salva (se houver)
        carregarRespostaSalva(q);

        // 5. Atualiza os botões de navegação
        btnAnterior.setDisable(indice == 0);
        btnProximo.setManaged(indice < questoes.size() - 1);
        btnProximo.setVisible(indice < questoes.size() - 1);
        btnFinalizar.setManaged(indice == questoes.size() - 1);
        btnFinalizar.setVisible(indice == questoes.size() - 1);
    }

    private void renderizarRespostaContainer(Questao questao) {
        respostaContainer.getChildren().clear();

        if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            alternativaToggleGroup = new ToggleGroup();

            for (Alternativa alt : questao.getAlternativas()) {
                RadioButton rb = new RadioButton(alt.getTexto());
                rb.setUserData(alt.getId()); // Usa o ID da alternativa como UserData
                rb.setToggleGroup(alternativaToggleGroup);
                respostaContainer.getChildren().add(rb);
            }
        } else if (questao.getTipo() == TipoQuestao.DISCURSIVA) {
            TextArea textArea = new TextArea();
            textArea.setPromptText("Digite sua resposta aqui...");
            textArea.setPrefHeight(150);
            respostaContainer.getChildren().add(textArea);
        }
    }

    // --- LÓGICA DE RESPOSTAS ---

    private void salvarRespostaAtual() {
        Questao q = questoes.get(indiceQuestaoAtual);
        Object resposta = null;

        if (q.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            // Se for múltipla escolha, salva o ID da alternativa selecionada
            Toggle selected = alternativaToggleGroup.getSelectedToggle();
            if (selected != null) {
                // UserData aqui é o ID da Alternativa (Integer)
                resposta = selected.getUserData();
            }
        } else if (q.getTipo() == TipoQuestao.DISCURSIVA) {
            // Se for discursiva, salva o texto
            if (!respostaContainer.getChildren().isEmpty() && respostaContainer.getChildren().get(0) instanceof TextArea) {
                TextArea textArea = (TextArea) respostaContainer.getChildren().get(0);
                String texto = textArea.getText().trim();
                if (!texto.isEmpty()) {
                    resposta = texto;
                }
            }
        }

        respostas.put(q.getId(), resposta);
    }

    private void carregarRespostaSalva(Questao questao) {
        Object respostaSalva = respostas.get(questao.getId());
        if (respostaSalva == null) return;

        if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            Integer alternativaId = (Integer) respostaSalva;
            for (Toggle toggle : alternativaToggleGroup.getToggles()) {
                if (toggle.getUserData() instanceof Integer && (Integer) toggle.getUserData() == alternativaId) {
                    toggle.setSelected(true);
                    break;
                }
            }
        } else if (questao.getTipo() == TipoQuestao.DISCURSIVA) {
            if (!respostaContainer.getChildren().isEmpty() && respostaContainer.getChildren().get(0) instanceof TextArea) {
                TextArea textArea = (TextArea) respostaContainer.getChildren().get(0);
                textArea.setText((String) respostaSalva);
            }
        }
    }

    // --- HANDLERS DE EVENTO ---

    @FXML
    private void handleAnterior() {
        if (indiceQuestaoAtual > 0) {
            mostrarQuestao(indiceQuestaoAtual - 1);
        }
    }

    @FXML
    private void handleProximo() {
        if (indiceQuestaoAtual < questoes.size() - 1) {
            mostrarQuestao(indiceQuestaoAtual + 1);
        }
    }

    @FXML
    private void handleFinalizar() {
        // 1. Salva a resposta da última questão
        salvarRespostaAtual();

        // 2. Confirmação
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmação de Finalização");
        confirmation.setHeaderText("Deseja realmente finalizar o simulado?");
        confirmation.setContentText("Você não poderá alterar suas respostas depois de finalizar.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 3. Processa e salva as respostas
                processarEFinalizarSimulado();

                // 4. Fecha a janela modal
                Stage stage = (Stage) btnFinalizar.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void processarEFinalizarSimulado() {
        AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();
        LocalDateTime dataFinalizacao = LocalDateTime.now();
        int respostasSalvas = 0;

        // Exclui respostas antigas antes de salvar as novas para garantir integridade
        // Nota: Esta exclusão não foi implementada no AlunoRespostaDAO, mas é recomendada.
        // Por enquanto, vamos apenas salvar. Se o aluno já tiver respondido (o que não deve acontecer
        // se a lógica de decisão estiver correta), a DB pode falhar (violação de chave).

        for (Map.Entry<Integer, Object> entry : respostas.entrySet()) {
            Integer questaoId = entry.getKey();
            Object resposta = entry.getValue();

            if (resposta != null) {
                Integer alternativaId = null;
                String respostaDiscursiva = null;

                if (resposta instanceof Integer) {
                    alternativaId = (Integer) resposta;
                } else if (resposta instanceof String) {
                    // Garante que a string não é apenas espaço em branco
                    if (!((String)resposta).trim().isEmpty()) {
                        respostaDiscursiva = (String) resposta;
                    } else {
                        continue; // Não salva se a discursiva for vazia
                    }
                }

                alunoRespostaDAO.saveResposta(
                        alunoLogado.getId(),
                        simulado.getId(),
                        questaoId,
                        alternativaId,
                        respostaDiscursiva,
                        dataFinalizacao
                );
                respostasSalvas++;
            }
        }

        // Alerta de sucesso
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText("Simulado Finalizado!");
        alert.setContentText(String.format("Você respondeu %d de %d questões. Suas respostas foram submetidas.", respostasSalvas, questoes.size()));
        alert.showAndWait();
    }
}