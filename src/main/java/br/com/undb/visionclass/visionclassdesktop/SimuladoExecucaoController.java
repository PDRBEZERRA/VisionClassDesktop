package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Alternativa;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.TipoQuestao;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import java.util.Collections;
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

    private Map<Integer, Object> respostas = new HashMap<>();

    private LocalDateTime dataInicio;

    private ToggleGroup alternativaToggleGroup = new ToggleGroup();


    public void setDadosSimulado(Simulado simulado, List<Questao> questoes) {
        this.simulado = simulado;
        this.questoes = questoes;

        Collections.shuffle(this.questoes);

        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        this.dataInicio = LocalDateTime.now();

        tituloLabel.setText("Simulado: " + simulado.getTitulo());

        for (Questao q : questoes) {
            respostas.put(q.getId(), null);
        }

        mostrarQuestao(0);
    }

    @FXML
    public void initialize() {
    }


    private void mostrarQuestao(int indice) {
        if (indice < 0 || indice >= questoes.size()) return;

        salvarRespostaAtual();

        this.indiceQuestaoAtual = indice;
        Questao q = questoes.get(indice);

        progressoLabel.setText(String.format("Questão %d de %d", indice + 1, questoes.size()));
        progressBar.setProgress((double) (indice + 1) / questoes.size());
        enunciadoLabel.setText(q.getEnunciado());

        renderizarRespostaContainer(q);

        carregarRespostaSalva(q);

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
                rb.setUserData(alt.getId());
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


    private void salvarRespostaAtual() {
        Questao q = questoes.get(indiceQuestaoAtual);
        Object resposta = null;

        if (q.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
            Toggle selected = alternativaToggleGroup.getSelectedToggle();
            if (selected != null) {
                resposta = selected.getUserData();
            }
        } else if (q.getTipo() == TipoQuestao.DISCURSIVA) {
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
        salvarRespostaAtual();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmação de Finalização");
        confirmation.setHeaderText("Deseja realmente finalizar o simulado?");
        confirmation.setContentText("Você não poderá alterar suas respostas depois de finalizar.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processarEFinalizarSimulado();

                Stage stage = (Stage) btnFinalizar.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void processarEFinalizarSimulado() {
        AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();
        LocalDateTime dataFinalizacao = LocalDateTime.now();
        int respostasSalvas = 0;


        for (Map.Entry<Integer, Object> entry : respostas.entrySet()) {
            Integer questaoId = entry.getKey();
            Object resposta = entry.getValue();

            if (resposta != null) {
                Integer alternativaId = null;
                String respostaDiscursiva = null;

                if (resposta instanceof Integer) {
                    alternativaId = (Integer) resposta;
                } else if (resposta instanceof String) {
                    if (!((String)resposta).trim().isEmpty()) {
                        respostaDiscursiva = (String) resposta;
                    } else {
                        continue;
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText("Simulado Finalizado!");
        alert.setContentText(String.format("Você respondeu %d de %d questões. Suas respostas foram submetidas.", respostasSalvas, questoes.size()));
        alert.showAndWait();
    }
}