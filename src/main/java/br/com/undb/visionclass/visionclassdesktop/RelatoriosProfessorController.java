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

    @FXML private Button comportamentalButton;
    @FXML private Button simuladosButton;
    @FXML private Button consolidadoButton;

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

    private TurmaDAO turmaDAO = new TurmaDAO();
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private UserDAO userDAO = new UserDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    private User professorLogado;

    private final String COMPORTAMENTAL = "comportamental";
    private final String SIMULADOS = "simulados";
    private final String CONSOLIDADO = "consolidado";
    private String activeTab = COMPORTAMENTAL;

    @FXML
    public void initialize() {
        this.professorLogado = UserSession.getInstance().getLoggedInUser();
        setupComboBoxes();
        loadTurmasData();

        turmaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            handleTrocaDeAbas();
        });

        handleTrocaDeAbas();

        dataInicialPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
        dataFinalPicker.valueProperty().addListener((obs, oldVal, newVal) -> handleFiltroDataChange());
    }

    private void handleFiltroDataChange() {
        System.out.println("Filtro de data alterado.");
    }

    private void setupComboBoxes() {
        periodoComboBox.getItems().addAll("Último Mês", "Últimos 3 Meses", "Últimos 6 Meses", "Personalizado");
        periodoComboBox.setValue("Último Mês");

        turmaComboBox.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "Todas as Turmas" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });
    }

    private void loadTurmasData() {
        if (professorLogado == null) return;

        List<Turma> turmas = turmaDAO.findByProfessorId(professorLogado.getId());

        turmaComboBox.getItems().add(null);

        turmaComboBox.getItems().addAll(turmas);

        turmaComboBox.getSelectionModel().selectFirst();
    }


    private void handleTrocaDeAbas() {
        Turma turmaSelecionada = turmaComboBox.getSelectionModel().getSelectedItem();

        List<Turma> turmasDoEscopo;
        if (turmaSelecionada == null) {
            turmasDoEscopo = turmaDAO.findByProfessorId(professorLogado.getId());
        } else {
            turmasDoEscopo = List.of(turmaSelecionada);
        }

        List<String> alunosIds = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());

        updateTabStyles();

        if (alunosIds.isEmpty()) {
            resetStatsComportamental("0.0/5.0", "N/A");
            resetStatsSimulados("0.0/10", "N/A");
            resetStatsConsolidado("N/A", "N/A");
            updateChartPlaceholder("Gráfico de Evolução", "Nenhum aluno encontrado no escopo selecionado.");
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


    private void updateChartPlaceholder(String title, String placeholderText) {
        if (!evolucaoGraficoContainer.getChildren().isEmpty() && evolucaoGraficoContainer.getChildren().get(0) instanceof Label) {
            ((Label) evolucaoGraficoContainer.getChildren().get(0)).setText(title);
        }

        if (evolucaoGraficoContainer.getChildren().size() > 1 && evolucaoGraficoContainer.getChildren().get(1) instanceof VBox) {
            VBox placeholderVBox = (VBox) evolucaoGraficoContainer.getChildren().get(1);
            if (!placeholderVBox.getChildren().isEmpty() && placeholderVBox.getChildren().get(0) instanceof Label) {
                ((Label) placeholderVBox.getChildren().get(0)).setText(placeholderText);
            }
        } else if (!evolucaoGraficoContainer.getChildren().isEmpty() && evolucaoGraficoContainer.getChildren().get(0) instanceof VBox) {
            VBox placeholderVBox = (VBox) evolucaoGraficoContainer.getChildren().get(0);
            if (!placeholderVBox.getChildren().isEmpty() && placeholderVBox.getChildren().get(0) instanceof Label) {
                ((Label) placeholderVBox.getChildren().get(0)).setText(placeholderText);
            }
        }
    }


    private void updateTabStyles() {
        final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #334155; -fx-font-weight: normal;";

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

        Stream.of(comportamentalButton, simuladosButton, consolidadoButton)
                .filter(Objects::nonNull)
                .forEach(button -> {
                    button.getStyleClass().removeAll(ACTIVE_CLASS, INACTIVE_CLASS);
                    button.setStyle(INACTIVE_STYLE);
                    button.getStyleClass().add(INACTIVE_CLASS);
                });

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

        double mediaGeral = getAggregateMedia(alunosIds, "media_geral");
        double mediaAssiduidade = getAggregateMedia(alunosIds, "assiduidade");
        double mediaParticipacao = getAggregateMedia(alunosIds, "participacao");
        int totalAvaliacoes = avaliacaoDAO.countByAlunosIds(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");

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
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsSimulados("0.0/10", "N/A");
            return;
        }

        double mediaSimulados = getAggregateSimuladosMedia(alunosIds);

        int totalSimuladosRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIds(alunosIds);

        double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMC(alunosIds);

        DecimalFormat df = new DecimalFormat("0.0");
        DecimalFormat dfPercent = new DecimalFormat("0.0'%'");

        mediaGeralLabel.setText(mediaSimulados >= 0 ? String.format("%s/10", df.format(mediaSimulados)) : "-/10");
        mediaGeralStatusLabel.setText("Média Geral nos Simulados");

        assiduidadeLabel.setText(String.valueOf(totalSimuladosRealizados));
        assiduidadeStatusLabel.setText("Simulados Realizados");

        participacaoLabel.setText(mediaAcertosMC >= 0 ? dfPercent.format(mediaAcertosMC) : "--");
        participacaoStatusLabel.setText("Acerto Médio (MC)");

        avaliacoesCountLabel.setText(String.valueOf(alunosIds.size()));
        avaliacoesCountStatusLabel.setText("Alunos no Escopo");
    }


    private void loadConsolidadoData(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            resetStatsConsolidado("N/A", "N/A");
            return;
        }

        double mediaComportamental = getAggregateMedia(alunosIds, "media_geral"); // (0-5)
        double mediaSimulados = getAggregateSimuladosMedia(alunosIds); // (0-10)
        int totalAlunos = alunosIds.size();

        DecimalFormat dfComportamental = new DecimalFormat("0.0");
        DecimalFormat dfSimulados = new DecimalFormat("0.0");
        DecimalFormat dfScore = new DecimalFormat("0.0");

        mediaGeralLabel.setText(mediaSimulados >= 0 ? String.format("%s/10", dfSimulados.format(mediaSimulados)) : "N/A");
        mediaGeralStatusLabel.setText("Média Acadêmica (Simulados)");

        assiduidadeLabel.setText(mediaComportamental >= 0 ? String.format("%s/5.0", dfComportamental.format(mediaComportamental)) : "N/A");
        assiduidadeStatusLabel.setText("Média Comportamental");

        participacaoLabel.setText(String.valueOf(totalAlunos));
        participacaoStatusLabel.setText("Total de Alunos no Escopo");

        double scoreCombinado = -1;
        if (mediaComportamental >= 0 && mediaSimulados >= 0) {
            double mediaComportamentalNormalizada = mediaComportamental * 2.0;
            if (mediaComportamentalNormalizada > 0 || mediaSimulados > 0) {
                scoreCombinado = (mediaComportamentalNormalizada + mediaSimulados) / 2.0;
            } else {
                scoreCombinado = 0.0;
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

    private double getAggregateMedia(List<String> alunosIds, String dimensao) {
        if (alunosIds == null || alunosIds.isEmpty()) return 0.0;

        double somaMedias = 0;
        int count = 0;

        for (String alunoId : alunosIds) {
            String mediaStr;
            if ("media_geral".equals(dimensao)) {
                mediaStr = avaliacaoDAO.getMediaGeralByAlunoId(alunoId);
            } else {
                mediaStr = avaliacaoDAO.getMediaPorDimensao(alunoId, dimensao);
            }

            if (mediaStr != null && !"-".equals(mediaStr)) {
                try {
                    somaMedias += Double.parseDouble(mediaStr.replace(",", "."));
                    count++;
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter média para double: " + mediaStr + " para aluno ID: " + alunoId);
                }
            }
        }
        return count > 0 ? somaMedias / count : 0.0;
    }

    private int getAggregateCount(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) return 0;
        return avaliacaoDAO.countByAlunosIds(alunosIds);
    }


    private double getAggregateSimuladosMedia(List<String> alunosIds) {
        if (alunosIds == null || alunosIds.isEmpty()) return -1.0;

        double somaMedias = 0;
        int countValidos = 0;
        for (String alunoId : alunosIds) {
            double media = alunoRespostaDAO.getMediaGeralSimulados(alunoId);
            if (media >= 0) {
                somaMedias += media;
                countValidos++;
            }
        }
        return countValidos > 0 ? somaMedias / countValidos : -1.0;
    }



    @FXML
    private void handleExportarRelatorio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exportar-relatorio-view.fxml"));
            Parent root = loader.load();

            ExportarRelatorioController exportController = loader.getController();

            Turma turmaSelecionada = turmaComboBox.getValue();
            LocalDate dataInicial = dataInicialPicker.getValue();
            LocalDate dataFinal = dataFinalPicker.getValue();
            User professorSelecionado = this.professorLogado;


            exportController.setFiltros(turmaSelecionada, professorSelecionado, dataInicial, dataFinal);


            Stage stage = new Stage();
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

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