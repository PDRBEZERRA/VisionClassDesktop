package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.util.StringConverter;


public class ExportarRelatorioController {

    @FXML private ComboBox<String> periodoComboBox;
    @FXML private ComboBox<Turma> turmaComboBoxFiltro;
    @FXML private ComboBox<User> professorComboBoxFiltro;

    @FXML private ComboBox<User> alunoComboBoxFiltro;

    @FXML private DatePicker dataInicialPickerFiltro;
    @FXML private DatePicker dataFinalPickerFiltro;
    @FXML private ComboBox<String> conteudoRelatorioComboBox;

    @FXML private Button exportarPDFButton;
    @FXML private Button exportarCSVButton;
    @FXML private Button exportarExcelButton;
    @FXML private Button voltarButton;

    private List<String> alunosIdsDoEscopo;

    private Task<List<User>> searchTask;

    private TurmaDAO turmaDAO = new TurmaDAO();
    private UserDAO userDAO = new UserDAO();
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    @FXML
    public void initialize() {
        conteudoRelatorioComboBox.getItems().addAll("Consolidado", "Apenas Comportamental", "Apenas Simulados");
        conteudoRelatorioComboBox.setValue("Consolidado");

        loadTurmasEProfessores();

        setupAlunoSearchComboBox();

        exportarPDFButton.setDisable(false);
        exportarCSVButton.setDisable(false);
        exportarExcelButton.setDisable(false);

        periodoComboBox.setDisable(true);
        periodoComboBox.setValue("Personalizado");
    }

    private void loadTurmasEProfessores() {
        turmaComboBoxFiltro.setConverter(new StringConverter<Turma>() {
            @Override
            public String toString(Turma turma) {
                return turma == null ? "Todas as Turmas" : turma.getNome();
            }
            @Override
            public Turma fromString(String string) { return null; }
        });
        List<Turma> turmas = turmaDAO.findAll();
        turmaComboBoxFiltro.getItems().add(null);
        turmaComboBoxFiltro.getItems().addAll(turmas);
        turmaComboBoxFiltro.getSelectionModel().selectFirst();

        professorComboBoxFiltro.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "Todos os Professores" : user.getNome();
            }
            @Override
            public User fromString(String string) { return null; }
        });
        List<User> professores = userDAO.findByRole(UserRole.PROFESSOR);
        professorComboBoxFiltro.getItems().add(null);
        professorComboBoxFiltro.getItems().addAll(professores);
        professorComboBoxFiltro.getSelectionModel().selectFirst();
    }

    private void setupAlunoSearchComboBox() {
        alunoComboBoxFiltro.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? null : user.getNome() + " (" + user.getMatricula() + ")";
            }

            @Override
            public User fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                for (User user : alunoComboBoxFiltro.getItems()) {
                    if (toString(user).equals(string)) {
                        return user;
                    }
                }
                return null;
            }
        });

        alunoComboBoxFiltro.getEditor().textProperty().addListener((obs, oldText, newText) -> {

            User selectedUser = alunoComboBoxFiltro.getSelectionModel().getSelectedItem();
            String selectedUserText = null;
            if (selectedUser != null) {
                selectedUserText = alunoComboBoxFiltro.getConverter().toString(selectedUser);
            }

            if (newText != null && newText.equals(selectedUserText)) {
                return;
            }

            if (newText == null || newText.trim().length() < 3) {
                alunoComboBoxFiltro.getItems().clear();
                return;
            }

            if (searchTask != null && searchTask.isRunning()) {
                searchTask.cancel();
            }

            searchTask = new Task<>() {
                @Override
                protected List<User> call() throws Exception {
                    return userDAO.findAlunosByNameLike(newText);
                }
            };

            searchTask.setOnSucceeded(e -> {
                List<User> results = searchTask.getValue();
                User currentSelection = alunoComboBoxFiltro.getSelectionModel().getSelectedItem();

                alunoComboBoxFiltro.setItems(FXCollections.observableArrayList(results));
                alunoComboBoxFiltro.show();

                if (currentSelection != null && results.contains(currentSelection)) {
                    alunoComboBoxFiltro.getSelectionModel().select(currentSelection);
                }
            });

            searchTask.setOnFailed(e -> searchTask.getException().printStackTrace());

            new Thread(searchTask).start();
        });
    }


    private void calcularAlunosDoEscopo() {
        User alunoFiltro = alunoComboBoxFiltro.getValue();
        Turma turmaFiltro = turmaComboBoxFiltro.getValue();
        User professorFiltro = professorComboBoxFiltro.getValue();

        if (alunoFiltro != null) {
            this.alunosIdsDoEscopo = List.of(alunoFiltro.getId());

        } else if (turmaFiltro != null) {
            this.alunosIdsDoEscopo = userDAO.findAlunosByTurmaId(turmaFiltro.getId()).stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else if (professorFiltro != null) {
            List<Turma> turmasDoProf = turmaDAO.findByProfessorId(professorFiltro.getId());
            this.alunosIdsDoEscopo = turmasDoProf.stream()
                    .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                    .map(User::getId)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            this.alunosIdsDoEscopo = userDAO.findByRole(UserRole.ALUNO).stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        }

        System.out.println("Alunos no escopo para exportação: " + (alunosIdsDoEscopo != null ? alunosIdsDoEscopo.size() : 0));
    }


    @FXML
    private void handleExportarPDF() {
        calcularAlunosDoEscopo();

        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = createFileChooser("Salvar Relatório PDF", "*.pdf", "Arquivo PDF");

        fileChooser.setInitialFileName(suggestFileName(".pdf"));
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    return;
                }
                String[] headers = getHeader();
                saveAsPDF(data, headers, file, dataInicialPickerFiltro.getValue(), dataFinalPickerFiltro.getValue());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório PDF exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();
            } catch (IOException e) {
                handleExportException("PDF", e);
            } catch (Exception e) {
                handleDataFetchException("PDF", e);
            }
        }
    }

    private void saveAsPDF(List<Object[]> data, String[] headers, File file, LocalDate dataInicial, LocalDate dataFinal) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeA4);
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

            String[] pdfHeaders = getPdfHeaders(headers);
            float[] colWidths = new float[pdfHeaders.length];
            float[] colPositions = new float[pdfHeaders.length];

            colWidths[0] = 70;
            colWidths[1] = 150;
            colWidths[2] = 80;

            float fixedWidth = colWidths[0] + colWidths[1] + colWidths[2];
            float remainingWidth = tableWidth - fixedWidth;
            int remainingCols = pdfHeaders.length - 3;
            float dataColWidth = remainingCols > 0 ? remainingWidth / remainingCols : 0;

            for (int i = 3; i < pdfHeaders.length; i++) {
                colWidths[i] = dataColWidth;
            }

            colPositions[0] = margin;
            for (int i = 1; i < pdfHeaders.length; i++) {
                colPositions[i] = colPositions[i-1] + colWidths[i-1];
            }

            float padding = 3;
            for (int i = 1; i < colPositions.length; i++) {
                colPositions[i] += padding;
            }

            writeText(contentStream, fontBold, fontSizeTitle, margin, yPosition, "Relatório - " + conteudoRelatorioComboBox.getValue());
            yPosition -= (lineSpacing * 1.5f);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFiltro = "Período: " + (dataInicial != null ? dataInicial.format(dtf) : "N/A") +
                    " até " + (dataFinal != null ? dataFinal.format(dtf) : "N/A");
            writeText(contentStream, fontPlain, 10, margin, yPosition, dataFiltro);
            yPosition -= lineSpacing;

            String turmaFiltro = "Turma: " + (turmaComboBoxFiltro.getValue() != null ? turmaComboBoxFiltro.getValue().getNome() : "Todas");
            String profFiltro = "Professor: " + (professorComboBoxFiltro.getValue() != null ? professorComboBoxFiltro.getValue().getNome() : "Todos");
            writeText(contentStream, fontPlain, 10, margin, yPosition, turmaFiltro + " | " + profFiltro);
            yPosition -= (lineSpacing * 2.0f);

            contentStream.setLineWidth(0.5f);

            writeRow(contentStream, colPositions, colWidths, yPosition, pdfHeaders, fontBold, fontSizeHeader);
            yPosition -= (lineSpacing * 0.5f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
            yPosition -= lineSpacing;

            for (Object[] row : data) {
                if (yPosition < bottomMargin) {
                    contentStream.close();
                    page = new PDPage(landscapeA4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;
                    writeRow(contentStream, colPositions, colWidths, yPosition, pdfHeaders, fontBold, fontSizeHeader);
                    yPosition -= (lineSpacing * 0.5f);
                    contentStream.moveTo(margin, yPosition);
                    contentStream.lineTo(margin + tableWidth, yPosition);
                    contentStream.stroke();
                    yPosition -= lineSpacing;
                }
                String[] dataLine = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    dataLine[i] = formatPdfField(row[i]);
                }
                writeRow(contentStream, colPositions, colWidths, yPosition, dataLine, fontPlain, fontSizeBody);
                yPosition -= lineSpacing;
            }
            contentStream.close();
            document.save(file);
        }
    }

    private String[] getPdfHeaders(String[] originalHeaders) {
        String[] pdfHeaders = new String[originalHeaders.length];
        for (int i = 0; i < originalHeaders.length; i++) {
            switch (originalHeaders[i]) {
                case "Aluno ID": pdfHeaders[i] = "ID"; break;
                case "Nome Aluno": pdfHeaders[i] = "Nome"; break;
                case "Média Geral Comp.": pdfHeaders[i] = "M. Comp"; break;
                case "Média Assiduidade": pdfHeaders[i] = "Assid."; break;
                case "Média Participação": pdfHeaders[i] = "Partic."; break;
                case "Média Responsab.": pdfHeaders[i] = "Resp."; break;
                case "Média Sociab.": pdfHeaders[i] = "Sociab."; break;
                case "Nº Avaliações": pdfHeaders[i] = "N. Aval."; break;
                case "Média Geral Simulados (0-10)": pdfHeaders[i] = "M. Simu"; break;
                case "Nº Simulados Realizados": pdfHeaders[i] = "N. Simu"; break;
                case "Acerto Médio MC (%)": pdfHeaders[i] = "% MC"; break;
                case "Score de Progresso (0-10)": pdfHeaders[i] = "Score"; break;
                default: pdfHeaders[i] = originalHeaders[i]; break;
            }
        }
        return pdfHeaders;
    }

    private void writeRow(PDPageContentStream stream, float[] colPositions, float[] colWidths, float y, String[] data, PDFont font, int fontSize) throws IOException {
        stream.setFont(font, fontSize);
        float padding = 3;
        for (int i = 0; i < data.length; i++) {
            stream.beginText();
            stream.newLineAtOffset(colPositions[i], y);
            String text = data[i];
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            float availableWidth = colWidths[i] - padding;
            if (textWidth > availableWidth) {
                int charsToKeep = (int) (text.length() * (availableWidth / textWidth));
                if (charsToKeep > 3) {
                    text = text.substring(0, charsToKeep - 3) + "...";
                } else if (charsToKeep > 0) {
                    text = text.substring(0, charsToKeep -1 );
                } else if (text.length() > 0) {
                    text = text.substring(0, Math.min(text.length(), 10)) + "...";
                }
            }
            stream.showText(text);
            stream.endText();
        }
    }

    private String formatPdfField(Object field) {
        if (field instanceof Double) {
            return String.format("%.1f", (Double) field).replace(",", ".");
        }
        return Objects.toString(field, "N/A");
    }

    private void writeText(PDPageContentStream stream, PDFont font, int fontSize, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    @FXML
    private void handleExportarCSV() {
        calcularAlunosDoEscopo();
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }
        FileChooser fileChooser = createFileChooser("Salvar Relatório CSV", "*.csv", "Arquivo CSV");
        fileChooser.setInitialFileName(suggestFileName(".csv"));
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
                    for (Object[] row : data) {
                        for (int i = 0; i < row.length; i++) {
                            if (row[i] instanceof Double) {
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
        calcularAlunosDoEscopo();
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }
        FileChooser fileChooser = createFileChooser("Salvar Relatório Excel", "*.xlsx", "Planilha Excel");
        fileChooser.setInitialFileName(suggestFileName(".xlsx"));
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
                    populateExcelSheet(workbook, sheet, data);
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

    private String suggestFileName(String extension) {
        String tipo = conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_");
        String nomeFiltro = "";

        if (alunoComboBoxFiltro.getValue() != null) {
            nomeFiltro = alunoComboBoxFiltro.getValue().getNome().toLowerCase().replace(" ", "_");
        } else if (turmaComboBoxFiltro.getValue() != null) {
            nomeFiltro = turmaComboBoxFiltro.getValue().getNome().toLowerCase().replace(" ", "_");
        } else if (professorComboBoxFiltro.getValue() != null) {
            nomeFiltro = "prof_" + professorComboBoxFiltro.getValue().getNome().toLowerCase().replace(" ", "_");
        } else {
            nomeFiltro = "todos";
        }

        return String.format("relatorio_%s_%s%s", tipo, nomeFiltro, extension);
    }

    private String suggestFileName() {
        return suggestFileName("");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

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

    private List<Object[]> fetchDataForExport() throws Exception {
        List<Object[]> results = new ArrayList<>();
        String tipoRelatorio = conteudoRelatorioComboBox.getValue();
        LocalDate dataInicial = dataInicialPickerFiltro.getValue();
        LocalDate dataFinal = dataFinalPickerFiltro.getValue();

        for (String alunoId : alunosIdsDoEscopo) {
            User aluno = userDAO.findById(alunoId);
            if (aluno == null) continue;
            List<Object> rowData = new ArrayList<>(List.of(aluno.getId(), aluno.getNome(), aluno.getMatricula()));
            List<String> alunoIdList = List.of(alunoId);
            if ("Apenas Comportamental".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                double mediaGeralComp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "media_geral", dataInicial, dataFinal);
                double mediaAssid = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "assiduidade", dataInicial, dataFinal);
                double mediaPart = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "participacao", dataInicial, dataFinal);
                double mediaResp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "responsabilidade", dataInicial, dataFinal);
                double mediaSociab = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "sociabilidade", dataInicial, dataFinal);
                int countAval = avaliacaoDAO.countByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);
                rowData.add(mediaGeralComp >= 0 ? mediaGeralComp : null);
                rowData.add(mediaAssid >= 0 ? mediaAssid : null);
                rowData.add(mediaPart >= 0 ? mediaPart : null);
                rowData.add(mediaResp >= 0 ? mediaResp : null);
                rowData.add(mediaSociab >= 0 ? mediaSociab : null);
                rowData.add(countAval);
            }
            if ("Apenas Simulados".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                double mediaGeralSim = alunoRespostaDAO.getAggregateSimuladosMediaByDateRange(alunoIdList, dataInicial, dataFinal);
                int countSimRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);
                double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMCByDateRange(alunoIdList, dataInicial, dataFinal);
                rowData.add(mediaGeralSim >= 0 ? mediaGeralSim : null);
                rowData.add(countSimRealizados);
                rowData.add(mediaAcertosMC >= 0 ? mediaAcertosMC : null);
            }
            if ("Consolidado".equals(tipoRelatorio)) {
                double mediaGeralComp = (rowData.size() > 3 && rowData.get(3) instanceof Double) ? (Double) rowData.get(3) : -1.0;
                double mediaGeralSim = (rowData.size() > 9 && rowData.get(9) instanceof Double) ? (Double) rowData.get(9) : -1.0;
                Double score = null;
                if (mediaGeralComp >= 0 && mediaGeralSim >= 0) {
                    double compNorm = mediaGeralComp * 2.0;
                    score = (compNorm > 0 || mediaGeralSim > 0) ? (compNorm + mediaGeralSim) / 2.0 : 0.0;
                }
                rowData.add(score);
            }
            results.add(rowData.toArray());
        }
        return results;
    }

    private FileChooser createFileChooser(String title, String extension, String description) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description + " (*" + extension + ")", extension));
        return fileChooser;
    }

    private void createExcelHeader(Workbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void populateExcelSheet(Workbook workbook, Sheet sheet, List<Object[]> data) {
        CellStyle defaultStyle = workbook.createCellStyle();
        CellStyle doubleStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        doubleStyle.setDataFormat(format.getFormat("0.0"));
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
                    cell.setCellStyle(doubleStyle);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                    cell.setCellStyle(defaultStyle);
                } else if (field == null) {
                    cell.setCellValue("");
                    cell.setCellStyle(defaultStyle);
                } else {
                    cell.setCellValue(field.toString());
                    cell.setCellStyle(defaultStyle);
                }
            }
        }
    }

    private void handleExportException(String format, IOException e) {
        System.err.println("Erro ao exportar " + format + ": " + e.getMessage());
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erro de Exportação", "Ocorreu um erro ao gerar o arquivo " + format + ".\nVerifique se o arquivo não está aberto em outro programa.");
    }

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