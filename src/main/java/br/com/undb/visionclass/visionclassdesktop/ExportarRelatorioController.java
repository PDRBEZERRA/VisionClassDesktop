package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// --- NOVOS IMPORTS PARA PDFBox ---
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
// ---------------------------------

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat; // Embora não usemos mais para formatar dados, pode ser útil
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import para formatar data no PDF
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import para Objects.toString
import java.util.stream.Collectors;


public class ExportarRelatorioController {

    // --- Componentes FXML ---
    @FXML private ComboBox<String> periodoComboBox;
    @FXML private ComboBox<Turma> turmaComboBoxFiltro; // Renomeado no FXML (ou mantenha o nome original)
    @FXML private TextField alunoEspecificoTextField;
    @FXML private DatePicker dataInicialPickerFiltro; // Renomeado no FXML
    @FXML private DatePicker dataFinalPickerFiltro;   // Renomeado no FXML
    @FXML private ComboBox<String> conteudoRelatorioComboBox;
    @FXML private Button prepararDadosButton; // Botão que removemos

    @FXML private Button exportarPDFButton;
    @FXML private Button exportarCSVButton;
    @FXML private Button exportarExcelButton;
    @FXML private Button voltarButton;

    // --- Filtros Recebidos ---
    private Turma turmaSelecionada;
    private User professorSelecionado; // Pode ser o professor logado ou o selecionado pelo ADM
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private List<String> alunosIdsDoEscopo; // Lista de IDs dos alunos filtrados

    // --- DAOs ---
    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    @FXML
    public void initialize() {
        // Inicializa ComboBox de conteúdo
        conteudoRelatorioComboBox.getItems().addAll("Consolidado", "Apenas Comportamental", "Apenas Simulados");
        conteudoRelatorioComboBox.setValue("Consolidado");

        // *** ALTERAÇÃO: HABILITA o botão PDF ***
        exportarPDFButton.setDisable(false);

        // Botões CSV e Excel ficam habilitados
        exportarCSVButton.setDisable(false);
        exportarExcelButton.setDisable(false);

        // Desabilitar campos de filtro (pois são apenas para exibir os filtros recebidos)
        periodoComboBox.setDisable(true);
        turmaComboBoxFiltro.setDisable(true);
        alunoEspecificoTextField.setDisable(true);
        dataInicialPickerFiltro.setDisable(true);
        dataFinalPickerFiltro.setDisable(true);
    }

    /**
     * Método chamado pelos controllers de relatório para injetar os filtros selecionados.
     */
    public void setFiltros(Turma turma, User professor, LocalDate dataIni, LocalDate dataFim) {
        this.turmaSelecionada = turma;
        this.professorSelecionado = professor; // Pode ser null (ADM sem filtro) ou um professor específico
        this.dataInicial = dataIni;
        this.dataFinal = dataFim;

        // Pré-calcula a lista de alunosIds com base nos filtros recebidos
        calcularAlunosDoEscopo();

        // Atualiza a UI (para mostrar os filtros recebidos)
        if (turma != null) {
            turmaComboBoxFiltro.setPromptText(turma.getNome());
        } else if (professor != null) {
            turmaComboBoxFiltro.setPromptText("Turmas de " + professor.getNome());
        } else {
            turmaComboBoxFiltro.setPromptText("Todas as Turmas (ADM)");
        }

        if (dataIni != null) dataInicialPickerFiltro.setValue(dataIni);
        if (dataFim != null) dataFinalPickerFiltro.setValue(dataFim);

        // Define o período (lógica simples)
        if (dataIni == null && dataFim == null) {
            periodoComboBox.setPromptText("Todo o período");
        } else {
            periodoComboBox.setPromptText("Personalizado");
        }

        System.out.println("Filtros recebidos. Data inicial: " + dataIni + ", Data final: " + dataFim);
        System.out.println("Alunos no escopo para exportação: " + (alunosIdsDoEscopo != null ? alunosIdsDoEscopo.size() : 0));
    }

    /**
     * Determina a lista de IDs de alunos com base nos filtros recebidos.
     * ESTA LÓGICA FOI CORRIGIDA.
     */
    private void calcularAlunosDoEscopo() {
        List<Turma> turmasDoEscopo = new ArrayList<>();

        if (turmaSelecionada != null) {
            // 1. Filtro por turma específica (prioridade máxima)
            turmasDoEscopo = List.of(turmaSelecionada);
        } else if (professorSelecionado != null) {
            // 2. Filtro por professor (Professor logado OU ADM selecionou um professor)
            turmasDoEscopo = turmaDAO.findByProfessorId(professorSelecionado.getId());
        } else {
            // 3. Sem filtro de turma E sem filtro de professor (ADM selecionou "Todos")
            turmasDoEscopo = turmaDAO.findAll();
        }

        this.alunosIdsDoEscopo = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());
    }


    @FXML
    private void handleExportarPDF() {
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = createFileChooser("Salvar Relatório PDF", "*.pdf", "Arquivo PDF");
        String suggestedFileName = "relatorio_" + conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_") + ".pdf";
        fileChooser.setInitialFileName(suggestedFileName);
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                // 1. Busca os dados (já filtrados por data)
                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    return;
                }

                // 2. Pega os cabeçalhos
                String[] headers = getHeader();

                // 3. Chama o método de geração de PDF
                saveAsPDF(data, headers, file);

                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório PDF exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();

            } catch (IOException e) {
                handleExportException("PDF", e);
            } catch (Exception e) {
                handleDataFetchException("PDF", e);
            }
        }
    }

    /**
     * NOVO MÉTODO: Gera e salva um arquivo PDF com os dados.
     * Esta é uma implementação simples focada em texto.
     */
    private void saveAsPDF(List<Object[]> data, String[] headers, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4); // Deixa a página em paisagem
            document.addPage(page);

            float margin = 40;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            float yPosition = yStart;
            float bottomMargin = margin;
            float lineSpacing = 15;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDFont fontBold = PDType1Font.HELVETICA_BOLD;
            PDFont fontPlain = PDType1Font.HELVETICA;
            int fontSizeTitle = 16;
            int fontSizeHeader = 8;
            int fontSizeBody = 8;

            // 1. Título do Relatório
            String title = "Relatório - " + conteudoRelatorioComboBox.getValue();
            writeText(contentStream, fontBold, fontSizeTitle, margin, yPosition, title);
            yPosition -= (lineSpacing * 1.5f);

            // 2. Informações de Filtro
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFiltro = "Período: " + (dataInicial != null ? dataInicial.format(dtf) : "N/A") +
                    " até " + (dataFinal != null ? dataFinal.format(dtf) : "N/A");
            writeText(contentStream, fontPlain, 10, margin, yPosition, dataFiltro);
            yPosition -= lineSpacing;
            String turmaFiltro = "Turma: " + (turmaSelecionada != null ? turmaSelecionada.getNome() : (professorSelecionado != null ? "Turmas de " + professorSelecionado.getNome() : "Todas as Turmas (ADM)"));
            writeText(contentStream, fontPlain, 10, margin, yPosition, turmaFiltro);
            yPosition -= (lineSpacing * 2.0f);

            // 3. Cabeçalho da Tabela
            contentStream.setLineWidth(0.5f);
            String headerLine = formatAsPdfRow(headers); // Formata cabeçalho
            writeText(contentStream, fontBold, fontSizeHeader, margin, yPosition, headerLine);
            yPosition -= (lineSpacing * 0.5f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
            yPosition -= lineSpacing;

            // 4. Dados da Tabela
            for (Object[] row : data) {
                // Verifica se precisa de nova página
                if (yPosition < bottomMargin) {
                    contentStream.close(); // Fecha stream da página atual
                    page = new PDPage(PDRectangle.A4); // Cria nova página
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page); // Novo stream
                    yPosition = yStart; // Reseta Y

                    // Repete o cabeçalho na nova página
                    writeText(contentStream, fontBold, fontSizeHeader, margin, yPosition, headerLine);
                    yPosition -= (lineSpacing * 0.5f);
                    contentStream.moveTo(margin, yPosition);
                    contentStream.lineTo(margin + tableWidth, yPosition);
                    contentStream.stroke();
                    yPosition -= lineSpacing;
                }

                // Converte colunas para String
                String dataLine = formatAsPdfRow(row);

                // Escreve a linha de dados
                writeText(contentStream, fontPlain, fontSizeBody, margin, yPosition, dataLine);
                yPosition -= lineSpacing;
            }

            contentStream.close(); // Fecha o último stream
            document.save(file); // Salva o documento
        }
    }

    /**
     * Formata um array de objetos em uma string de largura fixa (rudimentar) para PDF.
     */
    private String formatAsPdfRow(Object[] row) {
        // Define larguras fixas aproximadas para alinhamento
        // ID(50), Nome(150), Matrícula(80), 
        // Comp(50), Assid(50), Part(50), Resp(50), Soc(50), N.Aval(50)
        // Sim(50), N.Sim(50), Acerto(50)
        // Score(50)
        // Esta é uma solução simples. Uma solução robusta usaria bibliotecas de tabela PDF.
        StringBuilder sb = new StringBuilder();
        // Formato: "%-Xs" -> X = largura, - = alinhado à esquerda
        try {
            sb.append(String.format("%-10s", Objects.toString(row[0], "").substring(0, Math.min(Objects.toString(row[0], "").length(), 10)))); // ID (curto)
            sb.append(String.format("%-30s", Objects.toString(row[1], "").substring(0, Math.min(Objects.toString(row[1], "").length(), 30)))); // Nome
            sb.append(String.format("%-15s", Objects.toString(row[2], ""))); // Matrícula

            // Formata o restante (assumindo que são números ou N/A)
            for (int i = 3; i < row.length; i++) {
                sb.append(String.format("%-18s", formatPdfField(row[i]))); // Colunas de dados
            }
        } catch (Exception e) {
            // Ignora erros de formatação
        }
        return sb.toString();
    }

    /** Formata um campo de dados para o PDF (números com 1 casa decimal) */
    private String formatPdfField(Object field) {
        if (field instanceof Double) {
            return String.format("%.1f", (Double) field);
        }
        return Objects.toString(field, "N/A");
    }

    /**
     * Método auxiliar para escrever texto no PDF.
     */
    private void writeText(PDPageContentStream stream, PDFont font, int fontSize, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }


    @FXML
    private void handleExportarCSV() {
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = createFileChooser("Salvar Relatório CSV", "*.csv", "Arquivo CSV");
        String suggestedFileName = "relatorio_" + conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_") + ".csv";
        fileChooser.setInitialFileName(suggestedFileName);
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    return;
                }

                try (FileWriter out = new FileWriter(file);
                     CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(getHeader()))) {

                    // Formata Doubles para usar "." como separador decimal no CSV
                    for (Object[] row : data) {
                        for (int i = 0; i < row.length; i++) {
                            if (row[i] instanceof Double) {
                                // Formata explicitamente para garantir o "."
                                row[i] = String.format("%.1f", (Double) row[i]).replace(",", ".");
                            }
                        }
                    }
                    csvPrinter.printRecords(data);
                    csvPrinter.flush();
                }

                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório CSV exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();

            } catch (IOException e) {
                handleExportException("CSV", e);
            } catch (Exception e) {
                handleDataFetchException("CSV", e);
            }
        }
    }

    @FXML
    private void handleExportarExcel() {
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = createFileChooser("Salvar Relatório Excel", "*.xlsx", "Planilha Excel");
        String suggestedFileName = "relatorio_" + conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_") + ".xlsx";
        fileChooser.setInitialFileName(suggestedFileName);
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    return;
                }

                try (Workbook workbook = new XSSFWorkbook();
                     FileOutputStream fileOut = new FileOutputStream(file)) {

                    Sheet sheet = workbook.createSheet("Relatório");
                    String[] headers = getHeader();
                    createExcelHeader(workbook, sheet, headers);
                    populateExcelSheet(workbook, sheet, data); // Usa o método auxiliar

                    // Ajusta largura das colunas
                    for(int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    workbook.write(fileOut);
                }

                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório Excel exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();

            } catch (IOException e) {
                handleExportException("Excel", e);
            } catch (Exception e) {
                handleDataFetchException("Excel", e);
            }
        }
    }

    /**
     * Cria um estilo básico para o cabeçalho da planilha Excel.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Adicionando bordas
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Define o cabeçalho do arquivo com base no tipo de relatório selecionado.
     */
    private String[] getHeader() {
        String tipoRelatorio = conteudoRelatorioComboBox.getValue();
        List<String> headerList = new ArrayList<>(List.of("Aluno ID", "Nome Aluno", "Matrícula"));

        if ("Apenas Comportamental".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
            headerList.addAll(List.of("Média Geral Comp.", "Média Assiduidade", "Média Participação", "Média Responsab.", "Média Sociab.", "Nº Avaliações"));
        }
        if ("Apenas Simulados".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
            headerList.addAll(List.of("Média Geral Simulados (0-10)", "Nº Simulados Realizados", "Acerto Médio MC (%)"));
        }
        if ("Consolidado".equals(tipoRelatorio)) {
            headerList.add("Score de Progresso (0-10)");
        }

        return headerList.toArray(new String[0]);
    }

    /**
     * Busca os dados agregados dos alunos no escopo, aplicando filtros de data.
     * Chama os métodos DAO corretos (com filtro de data).
     */
    private List<Object[]> fetchDataForExport() throws Exception {
        List<Object[]> results = new ArrayList<>();
        String tipoRelatorio = conteudoRelatorioComboBox.getValue();

        // Itera sobre cada aluno no escopo definido pelos filtros
        for (String alunoId : alunosIdsDoEscopo) {
            User aluno = userDAO.findById(alunoId);
            if (aluno == null) continue;

            List<Object> rowData = new ArrayList<>(List.of(aluno.getId(), aluno.getNome(), aluno.getMatricula()));
            List<String> alunoIdList = List.of(alunoId); // Lista com um único aluno para os métodos DAO

            // --- Dados Comportamentais (com filtro de data) ---
            if ("Apenas Comportamental".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                double mediaGeralComp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "media_geral", dataInicial, dataFinal);
                double mediaAssid = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "assiduidade", dataInicial, dataFinal);
                double mediaPart = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "participacao", dataInicial, dataFinal);
                double mediaResp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "responsabilidade", dataInicial, dataFinal);
                double mediaSociab = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "sociabilidade", dataInicial, dataFinal);
                int countAval = avaliacaoDAO.countByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);

                rowData.add(mediaGeralComp >= 0 ? mediaGeralComp : null); // null para N/A
                rowData.add(mediaAssid >= 0 ? mediaAssid : null);
                rowData.add(mediaPart >= 0 ? mediaPart : null);
                rowData.add(mediaResp >= 0 ? mediaResp : null);
                rowData.add(mediaSociab >= 0 ? mediaSociab : null);
                rowData.add(countAval); // Integer
            }

            // --- Dados de Simulados (com filtro de data) ---
            if ("Apenas Simulados".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                double mediaGeralSim = alunoRespostaDAO.getAggregateSimuladosMediaByDateRange(alunoIdList, dataInicial, dataFinal);
                int countSimRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);
                double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMCByDateRange(alunoIdList, dataInicial, dataFinal);

                rowData.add(mediaGeralSim >= 0 ? mediaGeralSim : null);
                rowData.add(countSimRealizados); // Integer
                rowData.add(mediaAcertosMC >= 0 ? mediaAcertosMC : null); // Média percentual como Double
            }

            // --- Score Consolidado ---
            if ("Consolidado".equals(tipoRelatorio)) {
                // Reutiliza os valores já buscados
                double mediaGeralComp = (rowData.size() > 3 && rowData.get(3) instanceof Double) ? (Double) rowData.get(3) : -1.0;
                // Ajusta o índice se "Apenas Comportamental" não foi selecionado
                int indiceMediaSim = "Apenas Comportamental".equals(tipoRelatorio) ? 9 : 3;
                double mediaGeralSim = (rowData.size() > indiceMediaSim && rowData.get(indiceMediaSim) instanceof Double) ? (Double) rowData.get(indiceMediaSim) : -1.0;

                Double score = null; // Usar Double para permitir null
                if (mediaGeralComp >= 0 && mediaGeralSim >= 0) {
                    double compNorm = mediaGeralComp * 2.0; // Normaliza 0-5 para 0-10
                    score = (compNorm > 0 || mediaGeralSim > 0) ? (compNorm + mediaGeralSim) / 2.0 : 0.0;
                }
                rowData.add(score); // Adiciona Double ou null
            }

            results.add(rowData.toArray());
        }

        return results;
    }


    // --- Métodos Auxiliares de UI e Arquivo ---

    /** Cria um FileChooser pré-configurado */
    private FileChooser createFileChooser(String title, String extension, String description) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description + " (*" + extension + ")", extension));
        return fileChooser;
    }

    /** Cria o cabeçalho no arquivo Excel */
    private void createExcelHeader(Workbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /** Preenche a planilha Excel com os dados */
    private void populateExcelSheet(Workbook workbook, Sheet sheet, List<Object[]> data) {
        // Estilos para células de dados
        CellStyle defaultStyle = workbook.createCellStyle();
        CellStyle doubleStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        doubleStyle.setDataFormat(format.getFormat("0.0")); // Formato com uma casa decimal

        int rowNum = 1;
        for (Object[] record : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : record) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                    cell.setCellStyle(defaultStyle);
                } else if (field instanceof Double) {
                    cell.setCellValue((Double) field);
                    cell.setCellStyle(doubleStyle); // Aplica estilo numérico
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                    cell.setCellStyle(defaultStyle);
                } else if (field == null) {
                    cell.setCellValue(""); // Célula vazia para null (N/A)
                    cell.setCellStyle(defaultStyle);
                } else {
                    // Fallback para outros tipos (como "N/A" que é String)
                    cell.setCellValue(field.toString());
                    cell.setCellStyle(defaultStyle);
                }
            }
        }
    }

    /** Trata erros de IO (escrita de arquivo) */
    private void handleExportException(String format, IOException e) {
        System.err.println("Erro ao exportar " + format + ": " + e.getMessage());
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erro de Exportação", "Ocorreu um erro ao gerar o arquivo " + format + ".\nVerifique se o arquivo não está aberto em outro programa.");
    }

    /** Trata erros na busca de dados (Ex: SQL) */
    private void handleDataFetchException(String format, Exception e) {
        System.err.println("Erro ao buscar dados para " + format + ": " + e.getMessage());
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erro de Dados", "Ocorreu um erro ao buscar os dados para o relatório.");
    }


    @FXML
    private void handleVoltar() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getStage() {
        if (voltarButton != null && voltarButton.getScene() != null) {
            return (Stage) voltarButton.getScene().getWindow();
        }
        // Fallback se o voltarButton falhar
        if (exportarCSVButton != null && exportarCSVButton.getScene() != null) {
            return (Stage) exportarCSVButton.getScene().getWindow();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

