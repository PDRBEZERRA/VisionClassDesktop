package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole; // Import necessário
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.stream.Stream;

public class RelatoriosController {

    // --- Componentes FXML para Filtros ---
    @FXML
    private ComboBox<String> periodoComboBox;
    @FXML
    private ComboBox<Turma> turmaComboBox;
    @FXML
    private ComboBox<User> professorComboBox; // Filtro por professor (Presente no ADM)
    @FXML
    private DatePicker dataInicialPicker;
    @FXML
    private DatePicker dataFinalPicker;
    @FXML
    private Button exportarRelatorioButton;

    // FXML Handlers (Assumindo que foram adicionados ao FXML)
    @FXML private Button comportamentalButton;
    @FXML private Button simuladosButton;
    @FXML private Button consolidadoButton;

    // --- Componentes FXML para Estatísticas ---
    @FXML
    private Label mediaGeralLabel;
    @FXML
    private Label mediaGeralStatusLabel;
    @FXML
    private Label assiduidadeLabel; // Reutilizado
    @FXML
    private Label assiduidadeStatusLabel; // Reutilizado
    @FXML
    private Label participacaoLabel; // Reutilizado
    @FXML
    private Label participacaoStatusLabel; // Reutilizado
    @FXML
    private Label avaliacoesCountLabel; // Reutilizado
    @FXML
    private Label avaliacoesCountStatusLabel; // Reutilizado

    @FXML
    private VBox evolucaoGraficoContainer;

    // --- DAOs ---
    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    private final String COMPORTAMENTAL = "comportamental";
    private final String SIMULADOS = "simulados";
    private final String CONSOLIDADO = "consolidado";
    private String activeTab = COMPORTAMENTAL; // Aba ativa por padrão

    @FXML
    public void initialize() {
        // Não precisamos pegar o professor logado aqui, pois o ADM vê tudo
        setupComboBoxes();
        loadTurmasEProfessores(); // Carrega TODAS as turmas e professores

        // Listeners para os filtros principais
        turmaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> handleTrocaDeAbas());
        professorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> handleTrocaDeAbas());
        dataInicialPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
        dataFinalPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
        // TODO: Adicionar listeners para periodoComboBox, dataInicialPicker, dataFinalPicker se necessário

        // Carga inicial dos dados
        handleTrocaDeAbas();
    }

    private void setupComboBoxes() {
        periodoComboBox.getItems().addAll("Último Mês", "Últimos 3 Meses", "Últimos 6 Meses", "Personalizado");
        periodoComboBox.setValue("Último Mês");

        // Configura o ComboBox de Turma
        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "Todas as Turmas" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });

        // Configura o ComboBox de Professor
        professorComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "Todos os Professores" : user.getNome();
            }
            @Override
            public User fromString(String string) { return null; }
        });
    }

    private void loadTurmasEProfessores() {
        // Carrega TODAS as turmas para o ADM
        List<Turma> turmas = turmaDAO.findAll();
        turmaComboBox.getItems().add(null); // Opção "Todas as Turmas"
        turmaComboBox.getItems().addAll(turmas);
        turmaComboBox.getSelectionModel().selectFirst(); // Seleciona "Todas as Turmas"

        // Carrega TODOS os professores para o ADM
        List<User> professores = userDAO.findByRole(UserRole.PROFESSOR);
        professorComboBox.getItems().add(null); // Opção "Todos os Professores"
        professorComboBox.getItems().addAll(professores);
        professorComboBox.getSelectionModel().selectFirst(); // Seleciona "Todos os Professores"
    }


    private void handleFiltroDataChange() {
        System.out.println("Filtro de data alterado.");
        // Opcional: Adicionar lógica para limpar/recarregar dados visuais se necessário
        // handleTrocaDeAbas(); // Poderia chamar aqui se quisesse recarregar tudo
    }

    // --- LÓGICA DE CARREGAMENTO DE DADOS (Adaptada para ADM) ---

    private void handleTrocaDeAbas() {
        Turma turmaFiltro = turmaComboBox.getSelectionModel().getSelectedItem();
        User professorFiltro = professorComboBox.getSelectionModel().getSelectedItem();

        // 1. Determinar o escopo das turmas com base nos filtros do ADM
        List<Turma> turmasDoEscopo;
        if (turmaFiltro != null) {
            // Filtro por turma específica
            turmasDoEscopo = List.of(turmaFiltro);
        } else if (professorFiltro != null) {
            // Filtro por professor específico
            turmasDoEscopo = turmaDAO.findByProfessorId(professorFiltro.getId());
        } else {
            // Sem filtro de turma ou professor -> Todas as turmas
            turmasDoEscopo = turmaDAO.findAll();
        }

        // 2. Determinar o escopo dos alunos (IDs) a partir das turmas filtradas
        List<String> alunosIds = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Atualiza estilos visuais das abas
        updateTabStyles();

        // 4. Carrega os dados da aba ativa com base nos alunos filtrados
        if (alunosIds.isEmpty()) {
            // Se não houver alunos no escopo, reseta os stats
            resetStatsComportamental("0.0/5.0", "N/A");
            resetStatsSimulados("0.0/10", "N/A");
            resetStatsConsolidado("N/A", "N/A");
            updateChartPlaceholder("Gráfico de Evolução", "Nenhum aluno encontrado no escopo selecionado.");
            return;
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
     * Atualiza o estilo dos botões das abas.
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
                    button.getStyleClass().removeAll(ACTIVE_CLASS, INACTIVE_CLASS);
                    button.setStyle(INACTIVE_STYLE);
                    button.getStyleClass().add(INACTIVE_CLASS);
                });

        // 2. Ativa o botão selecionado
        if (activeButtonRef != null) {
            activeButtonRef.setStyle(ACTIVE_INLINE_STYLE);
            activeButtonRef.getStyleClass().remove(INACTIVE_CLASS);
            activeButtonRef.getStyleClass().add(ACTIVE_CLASS);
        }
    }


    private void loadComportamentalData(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsComportamental("0.0/5.0", "N/A");
            return;
        }

        // Cálculos agregados
        double mediaGeral = getAggregateMedia(alunosIds, "media_geral");
        double mediaAssiduidade = getAggregateMedia(alunosIds, "assiduidade");
        double mediaParticipacao = getAggregateMedia(alunosIds, "participacao");
        int totalAvaliacoes = avaliacaoDAO.countByAlunosIds(alunosIds); // Contagem real

        DecimalFormat df = new DecimalFormat("0.0");

        // Atualiza Labels
        mediaGeralLabel.setText(String.format("%s/5.0", df.format(mediaGeral)));
        mediaGeralStatusLabel.setText("Visão Institucional"); // Adaptado para ADM
        assiduidadeLabel.setText(String.format("%s/5.0", df.format(mediaAssiduidade)));
        assiduidadeStatusLabel.setText("Média da Escola"); // Adaptado para ADM
        participacaoLabel.setText(String.format("%s/5.0", df.format(mediaParticipacao)));
        participacaoStatusLabel.setText("Foco Estratégico"); // Adaptado para ADM
        avaliacoesCountLabel.setText(String.valueOf(totalAvaliacoes));
        avaliacoesCountStatusLabel.setText("Total no Escopo"); // Adaptado para ADM
    }

    private void loadSimuladosData(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsSimulados("0.0/10", "N/A");
            return;
        }

        // Cálculos agregados
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds);

        // *** CORREÇÃO 1 (ADM): Usar o método real do DAO para contar simulados realizados ***
        int totalSimuladosRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIds(alunosIds);

        // *** CORREÇÃO 2 (ADM): Calcular a média de acertos MC usando o método do DAO ***
        double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMC(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");
        DecimalFormat dfPercent = new DecimalFormat("0.0'%'");

        // Atualiza Labels
        mediaGeralLabel.setText(mediaSimulados >= 0 ? String.format("%s/10", df.format(mediaSimulados)) : "-/10");
        mediaGeralStatusLabel.setText("Média Geral nos Simulados");

        assiduidadeLabel.setText(String.valueOf(totalSimuladosRealizados)); // Usa a contagem real
        assiduidadeStatusLabel.setText("Simulados Realizados");

        participacaoLabel.setText(mediaAcertosMC >= 0 ? dfPercent.format(mediaAcertosMC) : "--"); // Usa o cálculo real
        participacaoStatusLabel.setText("Acerto Médio (MC)");

        avaliacoesCountLabel.setText(String.valueOf(alunosIds.size())); // Total de alunos no escopo
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }

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

        // Calcula Score Combinado
        double scoreCombinado = -1;
        if (mediaComportamental >= 0 && mediaSimulados >= 0) {
            double mediaComportamentalNormalizada = mediaComportamental * 2.0; // Normaliza para 0-10
            if (mediaComportamentalNormalizada > 0 || mediaSimulados > 0) {
                scoreCombinado = (mediaComportamentalNormalizada + mediaSimulados) / 2.0;
            } else {
                scoreCombinado = 0.0;
            }
        }

        avaliacoesCountLabel.setText(scoreCombinado >= 0 ? String.format("%s/10", dfScore.format(scoreCombinado)) : "N/A");
        avaliacoesCountStatusLabel.setText("Score de Progresso");
    }


    // Métodos de Reset (permanecem os mesmos)
    private void resetStatsComportamental(String media, String status) {
        mediaGeralLabel.setText(media); mediaGeralStatusLabel.setText(status);
        assiduidadeLabel.setText(media); assiduidadeStatusLabel.setText(status);
        participacaoLabel.setText(media); participacaoStatusLabel.setText(status);
        avaliacoesCountLabel.setText("0"); avaliacoesCountStatusLabel.setText(status);
    }

    private void resetStatsSimulados(String media, String status) {
        mediaGeralLabel.setText(media); mediaGeralStatusLabel.setText(status);
        assiduidadeLabel.setText("0"); assiduidadeStatusLabel.setText("Simulados Realizados");
        participacaoLabel.setText("--"); participacaoStatusLabel.setText("Acerto Médio (MC)");
        avaliacoesCountLabel.setText("0"); avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }

    private void resetStatsConsolidado(String media, String status) {
        mediaGeralLabel.setText("N/A"); mediaGeralStatusLabel.setText("Média Acadêmica (Simulados)");
        assiduidadeLabel.setText("N/A"); assiduidadeStatusLabel.setText("Média Comportamental");
        participacaoLabel.setText("0"); participacaoStatusLabel.setText("Total de Alunos no Escopo");
        avaliacoesCountLabel.setText("N/A"); avaliacoesCountStatusLabel.setText("Score de Progresso");
    }

    // Métodos de Agregação (permanecem os mesmos, já usam os DAOs corretos)
    private double getAggregateMedia(List<String> alunosIds, String dimensao) {
        if (alunosIds == null || alunosIds.isEmpty()) return 0.0;
        double somaMedias = 0; int count = 0;
        for (String alunoId : alunosIds) {
            String mediaStr = ("media_geral".equals(dimensao)) ? avaliacaoDAO.getMediaGeralByAlunoId(alunoId) : avaliacaoDAO.getMediaPorDimensao(alunoId, dimensao);
            if (mediaStr != null && !"-".equals(mediaStr)) { try { somaMedias += Double.parseDouble(mediaStr.replace(",", ".")); count++; } catch (NumberFormatException e) { System.err.println("Erro conversão média: "+mediaStr); } }
        } return count > 0 ? somaMedias / count : 0.0;
    }

    private double getAggregateSimuladosMedia(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) return -1.0;
        double somaMedias = 0; int countValidos = 0;
        for (String alunoId : alunosIds) { double media = alunoRespostaDAO.getMediaGeralSimulados(alunoId); if (media >= 0) { somaMedias += media; countValidos++; } }
        return countValidos > 0 ? somaMedias / countValidos : -1.0;
    }

    // --- Ação de Exportar e Troca de Abas (permanecem as mesmas) ---
    @FXML
    private void handleExportarRelatorio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exportar-relatorio-view.fxml"));
            Parent root = loader.load();

            // Pega o controller da janela de exportação ANTES de mostrá-la
            ExportarRelatorioController exportController = loader.getController();

            // --- Coleta os filtros selecionados ---
            Turma turmaSelecionada = turmaComboBox.getValue();
            User professorSelecionado = professorComboBox.getValue(); // Específico do ADM
            LocalDate dataInicial = dataInicialPicker.getValue();
            LocalDate dataFinal = dataFinalPicker.getValue();
            // String periodoSelecionado = periodoComboBox.getValue(); // Poderia ser usado se necessário

            // --- Passa os filtros para o ExportarRelatorioController ---
            exportController.setFiltros(turmaSelecionada, professorSelecionado, dataInicial, dataFinal);


            Stage stage = new Stage();
            stage.setTitle("Exportar Relatório - Visão ADM");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait(); // Mostra a janela e espera ela fechar

            // Nenhuma ação extra necessária após fechar, a exportação acontece dentro do modal

        } catch (IOException e) {
            System.err.println("Erro ao abrir o modal de exportação de relatórios.");
            e.printStackTrace();
            // Mostrar alerta de erro para o usuário
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a janela de exportação.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleMudarAbaComportamental() { activeTab = COMPORTAMENTAL; handleTrocaDeAbas(); }
    @FXML private void handleMudarAbaSimulados() { activeTab = SIMULADOS; handleTrocaDeAbas(); }
    @FXML private void handleMudarAbaConsolidado() { activeTab = CONSOLIDADO; handleTrocaDeAbas(); }
}