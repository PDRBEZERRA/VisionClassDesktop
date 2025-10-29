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

import java.time.LocalDate;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.control.Alert;

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
    private Label assiduidadeLabel; // Reutilizado para "Simulados Realizados" na aba Simulados
    @FXML
    private Label assiduidadeStatusLabel; // Reutilizado
    @FXML
    private Label participacaoLabel; // Reutilizado para "Acerto Médio (MC)" na aba Simulados
    @FXML
    private Label participacaoStatusLabel; // Reutilizado
    @FXML
    private Label avaliacoesCountLabel; // Reutilizado para "Alunos no Escopo" na aba Simulados
    @FXML
    private Label avaliacoesCountStatusLabel; // Reutilizado

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

        dataInicialPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
        dataFinalPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
    }

    private void handleFiltroDataChange() {
        System.out.println("Filtro de data alterado.");
        // Opcional: Adicionar lógica para limpar/recarregar dados visuais
        // handleTrocaDeAbas();
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
            // Se "Todas as Turmas" selecionado, pega todas as turmas do professor logado
            turmasDoEscopo = turmaDAO.findByProfessorId(professorLogado.getId());
        } else {
            // Se uma turma específica foi selecionada
            turmasDoEscopo = List.of(turmaSelecionada);
        }

        // 2. Define o escopo dos alunos (IDs) a partir das turmas selecionadas
        List<String> alunosIds = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream()) // Busca alunos de cada turma no escopo
                .map(User::getId) // Pega apenas os IDs
                .distinct() // Garante que não haja IDs duplicados se um aluno estiver em mais de uma turma (caso raro)
                .collect(Collectors.toList());

        // 3. Atualiza estilos visuais das abas
        updateTabStyles();

        // 4. Carrega os dados da aba ativa com base nos alunos filtrados
        if (alunosIds.isEmpty()) {
            // Se não houver alunos no escopo (turma vazia ou nenhuma turma), reseta os stats
            resetStatsComportamental("0.0/5.0", "N/A"); // Ou um reset geral, dependendo da aba
            resetStatsSimulados("0.0/10", "N/A");
            resetStatsConsolidado("N/A", "N/A");
            updateChartPlaceholder("Gráfico de Evolução", "Nenhum aluno encontrado no escopo selecionado.");
            return; // Interrompe a execução se não houver alunos
        }

        // Carrega os dados específicos da aba ativa
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
        // Tenta encontrar o Label do título (assumindo ser o primeiro filho direto)
        if (!evolucaoGraficoContainer.getChildren().isEmpty() && evolucaoGraficoContainer.getChildren().get(0) instanceof Label) {
            ((Label) evolucaoGraficoContainer.getChildren().get(0)).setText(title);
        }

        // Tenta encontrar o Label do placeholder (assumindo estar dentro de uma VBox que é o segundo filho)
        if (evolucaoGraficoContainer.getChildren().size() > 1 && evolucaoGraficoContainer.getChildren().get(1) instanceof VBox) {
            VBox placeholderVBox = (VBox) evolucaoGraficoContainer.getChildren().get(1);
            if (!placeholderVBox.getChildren().isEmpty() && placeholderVBox.getChildren().get(0) instanceof Label) {
                ((Label) placeholderVBox.getChildren().get(0)).setText(placeholderText);
            }
        } else if (!evolucaoGraficoContainer.getChildren().isEmpty() && evolucaoGraficoContainer.getChildren().get(0) instanceof VBox) {
            // Caso alternativo: O placeholder está dentro da primeira VBox (se não houver título separado)
            VBox placeholderVBox = (VBox) evolucaoGraficoContainer.getChildren().get(0);
            if (!placeholderVBox.getChildren().isEmpty() && placeholderVBox.getChildren().get(0) instanceof Label) {
                // Potencialmente atualiza o título aqui também se for o caso
                ((Label) placeholderVBox.getChildren().get(0)).setText(placeholderText); // Ou o title dependendo da estrutura
            }
        }
    }


    /**
     * Atualiza o estilo dos botões das abas para garantir que o contorno azul se mova.
     */
    private void updateTabStyles() {
        // Estilo transparente e texto escuro para abas inativas
        final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #334155; -fx-font-weight: normal;";

        // Estilo inline para botões ATIVOS (cor de fundo azul e texto branco, forçando dominância)
        final String ACTIVE_INLINE_STYLE = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;";

        final String ACTIVE_CLASS = "primary-button";
        final String INACTIVE_CLASS = "secondary-tab"; // Supondo que você criou essa classe no CSS

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
                    // Remove a classe ativa e inativa (se existir)
                    button.getStyleClass().removeAll(ACTIVE_CLASS, INACTIVE_CLASS);
                    // Aplica o estilo INACTIVE transparente
                    button.setStyle(INACTIVE_STYLE);
                    // Adiciona a classe inativa (se existir no CSS)
                    button.getStyleClass().add(INACTIVE_CLASS);
                });

        // 2. Ativa o botão selecionado
        if (activeButtonRef != null) {
            // Aplica o estilo ACTIVE INLINE para garantir fundo azul e texto branco
            activeButtonRef.setStyle(ACTIVE_INLINE_STYLE);
            // Remove a classe inativa e Adiciona a classe 'primary-button' para reativar o outline/border
            activeButtonRef.getStyleClass().remove(INACTIVE_CLASS);
            activeButtonRef.getStyleClass().add(ACTIVE_CLASS);
        }
    }


    private void loadComportamentalData(List<String> alunosIds) {
        // Se alunosIds for null ou vazia, reseta e retorna
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsComportamental("0.0/5.0", "N/A");
            return;
        }

        // --- Cálculo de Agregação de Dados Comportamentais ---
        double mediaGeral = getAggregateMedia(alunosIds, "media_geral");
        double mediaAssiduidade = getAggregateMedia(alunosIds, "assiduidade");
        double mediaParticipacao = getAggregateMedia(alunosIds, "participacao");
        // Contagem real usando o DAO
        int totalAvaliacoes = avaliacaoDAO.countByAlunosIds(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");

        // Atualiza os Labels
        mediaGeralLabel.setText(String.format("%s/5.0", df.format(mediaGeral)));
        mediaGeralStatusLabel.setText("Visão da Turma/Escopo");
        assiduidadeLabel.setText(String.format("%s/5.0", df.format(mediaAssiduidade)));
        assiduidadeStatusLabel.setText("Foco de Avaliação");
        participacaoLabel.setText(String.format("%s/5.0", df.format(mediaParticipacao)));
        participacaoStatusLabel.setText("Em Análise");
        avaliacoesCountLabel.setText(String.valueOf(totalAvaliacoes));
        avaliacoesCountStatusLabel.setText("Avaliações Encontradas");
    }

    private void loadSimuladosData(List<String> alunosIds) {
        // Se alunosIds for null ou vazia, reseta e retorna
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsSimulados("0.0/10", "N/A");
            return;
        }

        // --- Cálculo de Agregação de Dados de Simulados ---
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds);

        // *** CORREÇÃO 1: Usar o método real do DAO para contar simulados realizados ***
        int totalSimuladosRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIds(alunosIds);

        // *** CORREÇÃO 2: Calcular a média de acertos MC usando o novo método do DAO ***
        double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMC(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0"); // Formato para médias
        DecimalFormat dfPercent = new DecimalFormat("0.0'%'"); // Formato para percentual

        // 1. Média Geral Simulados (0 a 10)
        mediaGeralLabel.setText(mediaSimulados >= 0 ? String.format("%s/10", df.format(mediaSimulados)) : "-/10");
        mediaGeralStatusLabel.setText("Média Geral nos Simulados");

        // 2. Total de Simulados Realizados (usando o valor real)
        assiduidadeLabel.setText(String.valueOf(totalSimuladosRealizados));
        assiduidadeStatusLabel.setText("Simulados Realizados");

        // 3. Acerto Médio (MC) - Usando o valor calculado
        participacaoLabel.setText(mediaAcertosMC >= 0 ? dfPercent.format(mediaAcertosMC) : "--");
        participacaoStatusLabel.setText("Acerto Médio (MC)");

        // 4. Total de Alunos no Escopo
        avaliacoesCountLabel.setText(String.valueOf(alunosIds.size()));
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }


    // Carrega os dados para a aba Consolidado
    private void loadConsolidadoData(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsConsolidado("N/A", "N/A");
            return;
        }

        // Calcular médias
        double mediaComportamental = getAggregateMedia(alunosIds, "media_geral"); // (0-5)
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds); // (0-10)
        int totalAlunos = alunosIds.size();

        DecimalFormat dfComportamental = new DecimalFormat("0.0");
        DecimalFormat dfSimulados = new DecimalFormat("0.0");
        DecimalFormat dfScore = new DecimalFormat("0.0");

        // Atualiza Labels
        mediaGeralLabel.setText(mediaSimulados >= 0 ? String.format("%s/10", dfSimulados.format(mediaSimulados)) : "N/A");
        mediaGeralStatusLabel.setText("Média Acadêmica (Simulados)");

        assiduidadeLabel.setText(mediaComportamental >= 0 ? String.format("%s/5.0", dfComportamental.format(mediaComportamental)) : "N/A");
        assiduidadeStatusLabel.setText("Média Comportamental");

        participacaoLabel.setText(String.valueOf(totalAlunos));
        participacaoStatusLabel.setText("Total de Alunos no Escopo");

        // Calcula Score Combinado se ambas as médias forem válidas
        double scoreCombinado = -1;
        if (mediaComportamental >= 0 && mediaSimulados >= 0) {
            double mediaComportamentalNormalizada = mediaComportamental * 2.0; // Normaliza para 0-10
            // Evita divisão por zero se uma das médias for 0 e a outra não
            if (mediaComportamentalNormalizada > 0 || mediaSimulados > 0) {
                scoreCombinado = (mediaComportamentalNormalizada + mediaSimulados) / 2.0;
            } else {
                scoreCombinado = 0.0; // Caso ambas sejam 0
            }
        }

        avaliacoesCountLabel.setText(scoreCombinado >= 0 ? String.format("%s/10", dfScore.format(scoreCombinado)) : "N/A");
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
        participacaoStatusLabel.setText("Acerto Médio (MC)");
        avaliacoesCountLabel.setText("0");
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }


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
     * Calcula a média agregada de uma dimensão comportamental para um grupo de alunos.
     */
    private double getAggregateMedia(List<String> alunosIds, String dimensao) {
        if (alunosIds == null || alunosIds.isEmpty()) return 0.0; // Retorna 0 se não há alunos

        double somaMedias = 0;
        int count = 0;

        for (String alunoId : alunosIds) {
            String mediaStr;
            if ("media_geral".equals(dimensao)) {
                mediaStr = avaliacaoDAO.getMediaGeralByAlunoId(alunoId);
            } else {
                mediaStr = avaliacaoDAO.getMediaPorDimensao(alunoId, dimensao);
            }

            // Verifica se o valor retornado não é o placeholder "-"
            if (mediaStr != null && !"-".equals(mediaStr)) {
                try {
                    // Substitui vírgula por ponto para conversão correta
                    somaMedias += Double.parseDouble(mediaStr.replace(",", "."));
                    count++;
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter média para double: " + mediaStr + " para aluno ID: " + alunoId);
                    // Ignora este valor, mas registra o erro
                }
            }
        }
        // Retorna a média ou 0.0 se nenhum valor válido foi encontrado
        return count > 0 ? somaMedias / count : 0.0;
    }

    /**
     * MÉTODO CORRIGIDO: Usa o DAO para contagem real de avaliações.
     */
    private int getAggregateCount(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) return 0; // Retorna 0 se não há alunos
        return avaliacaoDAO.countByAlunosIds(alunosIds);
    }


    /**
     * Calcula a média de Simulado agregada para um grupo de alunos (escala 0-10).
     */
    private double getAggregateSimuladosMedia(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) return -1.0; // Retorna -1 se não há alunos

        double somaMedias = 0;
        int countValidos = 0; // Conta apenas alunos com médias válidas (>= 0)
        for (String alunoId : alunosIds) {
            double media = alunoRespostaDAO.getMediaGeralSimulados(alunoId);
            if (media >= 0) { // Considera apenas médias válidas
                somaMedias += media;
                countValidos++;
            }
        }
        // Retorna a média ou -1.0 se nenhum aluno tiver média válida
        return countValidos > 0 ? somaMedias / countValidos : -1.0;
    }

    /*
     * MÉTODO getAggregateSimuladosCount REMOVIDO pois agora usamos
     * alunoRespostaDAO.countSimuladosRealizadosByAlunosIds diretamente.
     */


    @FXML
    private void handleExportarRelatorio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exportar-relatorio-view.fxml"));
            Parent root = loader.load();

            ExportarRelatorioController exportController = loader.getController();

            // --- Coleta os filtros ---
            Turma turmaSelecionada = turmaComboBox.getValue();
            LocalDate dataInicial = dataInicialPicker.getValue();
            LocalDate dataFinal = dataFinalPicker.getValue();
            // O professor é o logado, não precisa pegar do ComboBox
            User professorSelecionado = this.professorLogado;

            // --- Passa os filtros ---
            // Passa null para o professor no caso do professor (ou passa o logado,
            // dependendo de como ExportarRelatorioController tratará isso)
            // Vamos passar o professor logado para consistência.
            exportController.setFiltros(turmaSelecionada, professorSelecionado, dataInicial, dataFinal);


            Stage stage = new Stage();
            // Título ligeiramente diferente para clareza
            stage.setTitle("Exportar Relatório - Professor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o modal de exportação de relatórios.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a janela de exportação.");
        }
    }

    // Método auxiliar para mostrar alertas (adicionar se não existir)
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Ações para alternar entre as abas
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