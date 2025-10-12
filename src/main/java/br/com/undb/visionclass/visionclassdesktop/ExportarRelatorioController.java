package br.com.undb.visionclass.visionclassdesktop;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ExportarRelatorioController {

    // --- 1. Escopo da Exportação ---
    @FXML
    private ComboBox<String> periodoComboBox;
    @FXML
    private ComboBox<String> turmaComboBox;
    @FXML
    private TextField alunoEspecificoTextField;
    @FXML
    private DatePicker dataInicialPicker;
    @FXML
    private DatePicker dataFinalPicker;
    @FXML
    private ComboBox<String> conteudoRelatorioComboBox;
    @FXML
    private Button prepararDadosButton;

    // --- 2. Formato de Saída (Botões de Exportação) ---
    @FXML
    private Button exportarPDFButton;
    @FXML
    private Button exportarCSVButton;
    @FXML
    private Button exportarExcelButton;

    @FXML
    private Button voltarButton;

    @FXML
    public void initialize() {
        // Inicializa ComboBoxes com opções visíveis na imagem
        periodoComboBox.getItems().addAll("Último Mês", "Últimos 3 Meses", "Personalizado");
        turmaComboBox.getItems().addAll("Todas as Turmas", "Turma A - Manhã", "Turma B - Noite"); // Mock
        conteudoRelatorioComboBox.getItems().addAll("Consolidado (Comportamental + Simulados)", "Apenas Comportamental", "Apenas Simulados");

        // Seta valores padrão
        periodoComboBox.setValue("Último Mês");
        turmaComboBox.setValue("Todas as Turmas");
        conteudoRelatorioComboBox.setValue("Consolidado (Comportamental + Simulados)");

        // Desabilitar botões de exportação até que os dados sejam carregados
        exportarPDFButton.setDisable(true);
        exportarCSVButton.setDisable(true);
        exportarExcelButton.setDisable(true);
    }

    @FXML
    private void handlePrepararDados() {
        // Lógica de simulação: Aqui ocorreria a chamada ao DAO para carregar e processar os dados
        System.out.println("Preparando dados para exportação com os filtros selecionados...");

        // Simulação de sucesso: Habilita os botões de exportação
        exportarPDFButton.setDisable(false);
        exportarCSVButton.setDisable(false);
        exportarExcelButton.setDisable(false);
    }

    @FXML
    private void handleExportarPDF() {
        // Lógica real de geração de relatório PDF
        System.out.println("Exportando para PDF...");
        closeWindow();
    }

    @FXML
    private void handleExportarCSV() {
        // Lógica real de geração de arquivo CSV
        System.out.println("Exportando para CSV...");
        closeWindow();
    }

    @FXML
    private void handleExportarExcel() {
        // Lógica real de geração de planilha Excel
        System.out.println("Exportando para Excel...");
        closeWindow();
    }

    @FXML
    private void handleVoltar() {
        closeWindow();
    }

    private void closeWindow() {
        // Método utilitário para fechar o modal
        Stage stage = (Stage) prepararDadosButton.getScene().getWindow();
        stage.close();
    }
}