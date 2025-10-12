package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.stream.Stream;

public class RelatoriosProfessorController {

    // --- Componentes FXML para Filtros ---
    @FXML
    private ComboBox<String> periodoComboBox;
    @FXML
    private ComboBox<Turma> turmaComboBox;
    @FXML
    private DatePicker dataInicialPicker;
    @FXML
    private DatePicker dataFinalPicker;
    @FXML
    private Button exportarRelatorioButton;

    // FXML Handlers (Adicionados no FXML)
    @FXML private Button comportamentalButton;
    @FXML private Button simuladosButton;
    @FXML private Button consolidadoButton;

    // --- Componentes FXML para Estatísticas Comportamentais ---
    @FXML
    private Label mediaGeralLabel;
    @FXML
    private Label mediaGeralStatusLabel;
    @FXML
    private Label assiduidadeLabel;
    @FXML
    private Label assiduidadeStatusLabel;
    @FXML
    private Label participacaoLabel;
    @FXML
    private Label participacaoStatusLabel;
    @FXML
    private Label avaliacoesCountLabel;
    @FXML
    private Label avaliacoesCountStatusLabel;

    @FXML
    private VBox evolucaoGraficoContainer;

    // --- DAOs e Dados ---
    private TurmaDAO turmaDAO = new TurmaDAO();
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private UserDAO userDAO = new UserDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    private User professorLogado;

    private final String COMPORTAMENTAL = "comportamental";
    private final String SIMULADOS = "simulados";
    private final String CONSOLIDADO = "consolidado";
    private String activeTab = COMPORTAMENTAL; // Aba ativa por padrão

    @FXML
    public void initialize() {
        this.professorLogado = UserSession.getInstance().getLoggedInUser();
        setupComboBoxes();
        loadTurmasData();

        // Listener principal para carregar dados quando a turma muda
        turmaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            handleTrocaDeAbas();
        });

        // Carga inicial dos dados
        handleTrocaDeAbas();

        // TODO: Implementar listener para os outros filtros (período, datas)
    }

    private void setupComboBoxes() {
        // Inicialização dos filtros de período
        periodoComboBox.getItems().addAll("Último Mês", "Últimos 3 Meses", "Últimos 6 Meses", "Personalizado");
        periodoComboBox.setValue("Último Mês");

        // Configura o ComboBox de Turma para exibir o nome
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                // Opção para selecionar todas as turmas do professor
                return turma == null ? "Todas as Turmas" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });
    }

    private void loadTurmasData() {
        if (professorLogado == null) return;

        List<Turma> turmas = turmaDAO.findByProfessorId(professorLogado.getId());

        // 1. Adiciona a opção "Todas as Turmas" (representada por null)
        turmaComboBox.getItems().add(null);

        // 2. Adiciona as turmas do professor
        turmaComboBox.getItems().addAll(turmas);

        // 3. Seleciona a primeira opção (Todas as Turmas)
        turmaComboBox.getSelectionModel().selectFirst();
    }

    // --- LÓGICA DE CARREGAMENTO DE DADOS ---

    private void handleTrocaDeAbas() {
        Turma turmaSelecionada = turmaComboBox.getSelectionModel().getSelectedItem();

        // 1. Define o escopo das turmas
        List<Turma> turmasDoEscopo;
        if (turmaSelecionada == null) {
            turmasDoEscopo = turmaDAO.findByProfessorId(professorLogado.getId());
        } else {
            turmasDoEscopo = List.of(turmaSelecionada);
        }

        // 2. Define o escopo dos alunos (IDs)
        List<String> alunosIds = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                .map(User::getId)
                .collect(Collectors.toList());

        // 3. Atualiza estilos visuais
        updateTabStyles();

        // 4. Carrega os dados da aba ativa
        if (alunosIds.isEmpty()) {
            resetStatsComportamental("0.0/5.0", "N/A");
            updateChartPlaceholder("Gráfico de Evolução", "Selecione alunos nos filtros para ver os dados.");
            return;
        }

        if (activeTab.equals(COMPORTAMENTAL)) {
            loadComportamentalData(alunosIds);
            updateChartPlaceholder("Evolução dos Critérios Comportamentais", "Gráfico de tendência de Assiduidade, Participação, Responsabilidade e Sociabilidade.");
        } else if (activeTab.equals(SIMULADOS)) {
            loadSimuladosData(alunosIds);
            updateChartPlaceholder("Desempenho em Simulados", "Gráfico de acertos por disciplina ou nível de dificuldade.");
        } else if (activeTab.equals(CONSOLIDADO)) {
            loadConsolidadoData(alunosIds);
            updateChartPlaceholder("Análise Consolidada", "Gráfico de correlação entre desempenho acadêmico e comportamental.");
        }
    }

    /**
     * Atualiza o título e o conteúdo da área de gráficos.
     */
    private void updateChartPlaceholder(String title, String placeholderText) {
        // Simulação de atualização de título (se o título for o primeiro elemento na VBox pai)
        if (evolucaoGraficoContainer.getChildren().get(0) instanceof Label) {
            ((Label)evolucaoGraficoContainer.getChildren().get(0)).setText(title);
        }
        // Simulação de atualização de placeholder (se o placeholder for um Label dentro do segundo elemento)
        if (evolucaoGraficoContainer.getChildren().size() > 1 && evolucaoGraficoContainer.getChildren().get(1) instanceof VBox) {
            VBox placeholderVBox = (VBox) evolucaoGraficoContainer.getChildren().get(1);
            if (!placeholderVBox.getChildren().isEmpty() && placeholderVBox.getChildren().get(0) instanceof Label) {
                ((Label)placeholderVBox.getChildren().get(0)).setText(placeholderText);
            }
        }
    }


    /**
     * CORREÇÃO FINAL: Atualiza o estilo dos botões das abas para garantir que o contorno azul se mova.
     */
    private void updateTabStyles() {
        // Estilo transparente e texto escuro para abas inativas
        final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #334155; -fx-font-weight: normal;";

        // Estilo inline para botões ATIVOS (cor de fundo azul e texto branco, forçando dominância)
        // Isso resolve o problema de precedência sobre o estilo transparente das outras abas
        final String ACTIVE_INLINE_STYLE = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;";

        final String ACTIVE_CLASS = "primary-button";
        final String INACTIVE_CLASS = "secondary-tab";

        Button activeButtonRef = null;
        if (activeTab.equals(COMPORTAMENTAL)) {
            activeButtonRef = comportamentalButton;
        } else if (activeTab.equals(SIMULADOS)) {
            activeButtonRef = simuladosButton;
        } else if (activeTab.equals(CONSOLIDADO)) {
            activeButtonRef = consolidadoButton;
        }

        // 1. Itera e Desativa todos os botões
        Stream.of(comportamentalButton, simuladosButton, consolidadoButton)
                .filter(Objects::nonNull)
                .forEach(button -> {
                    // Remove a classe ativa e inativa
                    button.getStyleClass().removeAll(ACTIVE_CLASS, INACTIVE_CLASS);
                    // Aplica o estilo INACTIVE transparente
                    button.setStyle(INACTIVE_STYLE);
                    // Adiciona a classe inativa (se o FXML tiver sido corrigido para usar classes)
                    button.getStyleClass().add(INACTIVE_CLASS);
                });

        // 2. Ativa o botão selecionado
        if (activeButtonRef != null) {
            // 2a. Aplica o estilo ACTIVE INLINE para garantir fundo azul e texto branco
            activeButtonRef.setStyle(ACTIVE_INLINE_STYLE);
            // 2b. Remove a classe inativa e Adiciona a classe 'primary-button' para reativar o outline/border
            activeButtonRef.getStyleClass().remove(INACTIVE_CLASS);
            activeButtonRef.getStyleClass().add(ACTIVE_CLASS);
        }
    }


    private void loadComportamentalData(List<String> alunosIds) {

        if (alunosIds.isEmpty()) {
            resetStatsComportamental("0.0/5.0", "N/A");
            return;
        }

        // --- Cálculo de Agregação de Dados Comportamentais ---

        double mediaGeral = getAggregateMedia(alunosIds, "media_geral");
        double mediaAssiduidade = getAggregateMedia(alunosIds, "assiduidade");
        double mediaParticipacao = getAggregateMedia(alunosIds, "participacao");
        // Contagem real
        int totalAvaliacoes = avaliacaoDAO.countByAlunosIds(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");

        // 1. Média Geral Comportamental
        mediaGeralLabel.setText(String.format("%s/5.0", df.format(mediaGeral)));
        mediaGeralStatusLabel.setText("Visão da Turma/Escopo");

        // 2. Assiduidade
        assiduidadeLabel.setText(String.format("%s/5.0", df.format(mediaAssiduidade)));
        assiduidadeStatusLabel.setText("Foco de Avaliação");

        // 3. Participação
        participacaoLabel.setText(String.format("%s/5.0", df.format(mediaParticipacao)));
        participacaoStatusLabel.setText("Em Análise");

        // 4. Total de Avaliações
        avaliacoesCountLabel.setText(String.valueOf(totalAvaliacoes));
        avaliacoesCountStatusLabel.setText("Avaliações Encontradas");

    }

    private void loadSimuladosData(List<String> alunosIds) {

        if (alunosIds.isEmpty()) {
            resetStatsSimulados("0.0/10", "N/A");
            return;
        }

        // --- Cálculo de Agregação de Dados de Simulados ---
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds);
        int totalSimuladosRealizados = getAggregateSimuladosCount(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");

        // 1. Média Geral Simulados (0 a 10)
        mediaGeralLabel.setText(String.format("%s/10", df.format(mediaSimulados)));
        mediaGeralStatusLabel.setText("Média Geral nos Simulados");

        // 2. Total de Simulados Feitos (Reutilizando os cards para os dados de Simulados)
        assiduidadeLabel.setText(String.valueOf(totalSimuladosRealizados));
        assiduidadeStatusLabel.setText("Simulados Realizados");

        // 3. (Placeholder para Posição ou Acerto Médio)
        participacaoLabel.setText("75%"); // Mockup
        participacaoStatusLabel.setText("Acerto Médio (MC)");

        // 4. Total de Submissões
        avaliacoesCountLabel.setText(String.valueOf(alunosIds.size())); // Total de alunos no escopo
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }

    // NOVO MÉTODO: Carrega os dados para a aba Consolidado
    private void loadConsolidadoData(List<String> alunosIds) {
        if (alunosIds.isEmpty()) {
            resetStatsConsolidado("N/A", "N/A");
            return;
        }

        // Calcular a média comportamental e de simulados (0-10)
        double mediaComportamental = getAggregateMedia(alunosIds, "media_geral"); // (0-5 scale)
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds); // (0-10 scale)
        int totalAlunos = alunosIds.size();

        DecimalFormat dfComportamental = new DecimalFormat("0.0");
        DecimalFormat dfSimulados = new DecimalFormat("0.0");

        // 1. Média Geral (Combinada)
        mediaGeralLabel.setText(String.format("%s/10", dfSimulados.format(mediaSimulados)));
        mediaGeralStatusLabel.setText("Média Acadêmica (Simulados)");

        // 2. Média Comportamental
        assiduidadeLabel.setText(String.format("%s/5.0", dfComportamental.format(mediaComportamental)));
        assiduidadeStatusLabel.setText("Média Comportamental");

        // 3. Total de Alunos
        participacaoLabel.setText(String.valueOf(totalAlunos));
        participacaoStatusLabel.setText("Total de Alunos no Escopo");

        // 4. (Placeholder para Score Combinado)
        // Score Combinado Simulado (Média Comportamental normalizada para 0-10 + Média Simulado) / 2
        double mediaComportamentalNormalizada = mediaComportamental * 2.0; // 0-5 para 0-10
        double scoreCombinado = (mediaComportamentalNormalizada + mediaSimulados) / 2.0;

        avaliacoesCountLabel.setText(String.format("%s/10", new DecimalFormat("0.0").format(scoreCombinado)));
        avaliacoesCountStatusLabel.setText("Score de Progresso");
    }


    private void resetStatsComportamental(String media, String status) {
        mediaGeralLabel.setText(media);
        mediaGeralStatusLabel.setText(status);
        assiduidadeLabel.setText(media);
        assiduidadeStatusLabel.setText(status);
        participacaoLabel.setText(media);
        participacaoStatusLabel.setText(status);
        avaliacoesCountLabel.setText("0");
        avaliacoesCountStatusLabel.setText(status);
    }

    private void resetStatsSimulados(String media, String status) {
        mediaGeralLabel.setText(media);
        mediaGeralStatusLabel.setText(status);
        assiduidadeLabel.setText("0");
        assiduidadeStatusLabel.setText("Simulados Realizados");
        participacaoLabel.setText("--");
        participacaoStatusLabel.setText("Acerto Médio");
        avaliacoesCountLabel.setText("0");
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }

    // NOVO: Reset para a aba Consolidado
    private void resetStatsConsolidado(String media, String status) {
        mediaGeralLabel.setText("N/A");
        mediaGeralStatusLabel.setText("Média Acadêmica (Simulados)");
        assiduidadeLabel.setText("N/A");
        assiduidadeStatusLabel.setText("Média Comportamental");
        participacaoLabel.setText("0");
        participacaoStatusLabel.setText("Total de Alunos no Escopo");
        avaliacoesCountLabel.setText("N/A");
        avaliacoesCountStatusLabel.setText("Score de Progresso");
    }

    /**
     * MÉTODO CONCEITUAL: Calcula a média agregada de uma dimensão para um grupo de alunos.
     */
    private double getAggregateMedia(List<String> alunosIds, String dimensao) {
        double somaMedias = 0;
        int count = 0;

        for (String alunoId : alunosIds) {
            // Se for "media_geral", usa o método específico, senão, usa o método por dimensão.
            String mediaStr;
            if ("media_geral".equals(dimensao)) {
                mediaStr = avaliacaoDAO.getMediaGeralByAlunoId(alunoId);
            } else {
                mediaStr = avaliacaoDAO.getMediaPorDimensao(alunoId, dimensao);
            }

            if (!"-".equals(mediaStr)) {
                try {
                    somaMedias += Double.parseDouble(mediaStr.replace(",", "."));
                    count++;
                } catch (NumberFormatException e) {
                    // Ignora, mas idealmente não deveria acontecer.
                }
            }
        }
        return count > 0 ? somaMedias / count : 0.0;
    }

    /**
     * MÉTODO CORRIGIDO: Contagem real de avaliações.
     */
    private int getAggregateCount(List<String> alunosIds) {
        // CORREÇÃO AQUI: Chama o método DAO para obter a contagem real
        return avaliacaoDAO.countByAlunosIds(alunosIds);
    }


    /**
     * NOVO MÉTODO: Calcula a média de Simulado agregada para um grupo de alunos (escala 0-10).
     */
    private double getAggregateSimuladosMedia(List<String> alunosIds) {
        double somaMedias = 0;
        int count = 0;
        for (String alunoId : alunosIds) {
            double media = alunoRespostaDAO.getMediaGeralSimulados(alunoId);
            if (media >= 0) {
                somaMedias += media;
                count++;
            }
        }
        return count > 0 ? somaMedias / count : 0.0;
    }

    /**
     * MÉTODO CONCEITUAL: Simulação de contagem de Simulados Realizados.
     */
    private int getAggregateSimuladosCount(List<String> alunosIds) {
        // Simula que cada aluno fez pelo menos 2 simulados.
        return alunosIds.size() * 2;
    }


    @FXML
    private void handleExportarRelatorio() {
        try {
            // Carrega o FXML do modal de exportação
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exportar-relatorio-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Exportar Relatório");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a tela pai
            stage.setResizable(false);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o modal de exportação de relatórios.");
            e.printStackTrace();
        }
    }

    // Ações para alternar entre as abas (agora com troca de dados e estilo real)
    @FXML
    private void handleMudarAbaComportamental() {
        activeTab = COMPORTAMENTAL;
        handleTrocaDeAbas();
    }

    @FXML
    private void handleMudarAbaSimulados() {
        activeTab = SIMULADOS;
        handleTrocaDeAbas();
    }

    @FXML
    private void handleMudarAbaConsolidado() {
        activeTab = CONSOLIDADO;
        handleTrocaDeAbas();
    }
}