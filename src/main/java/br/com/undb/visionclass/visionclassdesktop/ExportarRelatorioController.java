package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
// Removido import desnecessário de AvaliacaoComportamental
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

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
// Removido import desnecessário de DateTimeFormatter
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ExportarRelatorioController {

    // --- Componentes FXML ---
    // Removidos ComboBoxes e DatePickers de filtro pois os valores vêm de fora
    @FXML private TextField alunoEspecificoTextField; // Mantido, mas não usado na lógica atual
    @FXML private ComboBox<String> conteudoRelatorioComboBox;
    // Removido prepararDadosButton
    @FXML private Button exportarPDFButton;
    @FXML private Button exportarCSVButton;
    @FXML private Button exportarExcelButton;
    @FXML private Button voltarButton;

    // --- Filtros Recebidos ---
    private Turma turmaSelecionada;
    private User professorSelecionado;
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

        // Desabilitar botão PDF (não implementado)
        exportarPDFButton.setDisable(true);

        // Botões CSV e Excel ficam habilitados por padrão
        exportarCSVButton.setDisable(false);
        exportarExcelButton.setDisable(false);
    }

    /**
     * Método chamado pelos controllers de relatório para injetar os filtros selecionados.
     */
    public void setFiltros(Turma turma, User professor, LocalDate dataIni, LocalDate dataFim) {
        this.turmaSelecionada = turma;
        this.professorSelecionado = professor; // Pode ser null
        this.dataInicial = dataIni;
        this.dataFinal = dataFim;

        // Pré-calcula a lista de alunosIds com base nos filtros recebidos
        calcularAlunosDoEscopo();

        // Poderia atualizar a UI aqui se os componentes de filtro ainda existissem, mas foram removidos.
        System.out.println("Filtros recebidos. Data inicial: " + dataIni + ", Data final: " + dataFim);
        System.out.println("Alunos no escopo para exportação: " + (alunosIdsDoEscopo != null ? alunosIdsDoEscopo.size() : 0));
    }

    /**
     * Determina a lista de IDs de alunos com base nos filtros recebidos.
     */
    private void calcularAlunosDoEscopo() {
        List<Turma> turmasDoEscopo;
        if (turmaSelecionada != null) {
            turmasDoEscopo = List.of(turmaSelecionada);
        } else if (professorSelecionado != null) {
            // Se veio do controller do ADM com professor selecionado
            turmasDoEscopo = turmaDAO.findByProfessorId(professorSelecionado.getId());
        } else {
            // Se veio do controller do Professor (sem filtro de professor) ou ADM (sem filtros)
            // Precisamos saber quem chamou ou passar um contexto. Assumindo ADM sem filtros por enquanto.
            // Para ser mais robusto, o ideal seria o controller que chama passar a lista de turmas já filtrada.
            // Solução alternativa: Verificar se professorSelecionado foi setado. Se não, assumir contexto ADM.
            if (professorSelecionado == null && turmaSelecionada == null) { // Caso ADM sem filtro ou Professor com "Todas as Turmas"
                // Se quem chamou foi o ProfessorController, precisamos pegar só as turmas DELE
                // Se quem chamou foi o ADMController, pegamos TODAS
                // Adicionaremos uma checagem simples (poderia ser melhorada com passagem de contexto)
                // Se professorSelecionado é null E turmaSelecionada é null, assumimos ADM sem filtro
                turmasDoEscopo = turmaDAO.findAll(); // ADM sem filtro
                // Se precisasse diferenciar Professor vs ADM aqui, a lógica seria mais complexa
            } else {
                // Caso estranho, mas por segurança, pega todas
                turmasDoEscopo = turmaDAO.findAll();
            }

        }

        this.alunosIdsDoEscopo = turmasDoEscopo.stream()
                .flatMap(t -> userDAO.findAlunosByTurmaId(t.getId()).stream())
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());
    }


    @FXML
    private void handleExportarPDF() {
        System.out.println("Exportação para PDF ainda não implementada.");
        showAlert(Alert.AlertType.INFORMATION, "Funcionalidade Indisponível", "A exportação para PDF ainda não foi implementada.");
    }

    @FXML
    private void handleExportarCSV() {
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV (*.csv)", "*.csv"));
        String suggestedFileName = "relatorio_" + conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_") + ".csv";
        fileChooser.setInitialFileName(suggestedFileName);
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try (FileWriter out = new FileWriter(file);
                 CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(getHeader()))) {

                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    return;
                }

                csvPrinter.printRecords(data); // Imprime todos os registros de uma vez
                csvPrinter.flush();
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório CSV exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();

            } catch (IOException e) {
                System.err.println("Erro ao exportar CSV: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro de Exportação", "Ocorreu um erro ao gerar o arquivo CSV.");
            } catch (Exception e) { // Captura genérica para erros na busca de dados
                System.err.println("Erro ao buscar dados para CSV: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro de Dados", "Ocorreu um erro ao buscar os dados para o relatório.");
            }
        }
    }

    @FXML
    private void handleExportarExcel() {
        if (alunosIdsDoEscopo == null || alunosIdsDoEscopo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Aluno", "Não há alunos no escopo selecionado para exportar dados.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Planilha Excel (*.xlsx)", "*.xlsx"));
        String suggestedFileName = "relatorio_" + conteudoRelatorioComboBox.getValue().toLowerCase().replace(" ", "_") + ".xlsx";
        fileChooser.setInitialFileName(suggestedFileName);
        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(file)) {

                Sheet sheet = workbook.createSheet("Relatório");

                // Cria o cabeçalho
                Row headerRow = sheet.createRow(0);
                String[] headers = getHeader();
                CellStyle headerStyle = createHeaderStyle(workbook); // Estilo para o cabeçalho
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                List<Object[]> data = fetchDataForExport();
                if (data.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Nenhum Dado", "Nenhum dado encontrado para os filtros selecionados no período.");
                    workbook.close(); // Fecha o workbook antes de retornar
                    return;
                }

                // Estilos para células de dados
                CellStyle defaultStyle = workbook.createCellStyle();
                CellStyle doubleStyle = workbook.createCellStyle();
                DataFormat format = workbook.createDataFormat();
                doubleStyle.setDataFormat(format.getFormat("0.0")); // Formato com uma casa decimal


                // Preenche os dados
                int rowNum = 1;
                for (Object[] record : data) {
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;
                    for (Object field : record) {
                        Cell cell = row.createCell(colNum++);
                        if (field instanceof String) {
                            cell.setCellValue((String) field);
                            cell.setCellStyle(defaultStyle);
                        } else if (field instanceof Double) { // Verifica se é Double
                            cell.setCellValue((Double) field);
                            cell.setCellStyle(doubleStyle); // Aplica estilo numérico
                        } else if (field instanceof Integer) { // Trata Integers
                            cell.setCellValue((Integer) field);
                            cell.setCellStyle(defaultStyle); // Ou um estilo específico para inteiros
                        } else if (field == null || "N/A".equals(field)) {
                            cell.setCellValue(""); // Célula vazia para N/A ou null
                            cell.setCellStyle(defaultStyle);
                        } else {
                            cell.setCellValue(field.toString()); // Fallback para outros tipos
                            cell.setCellStyle(defaultStyle);
                        }
                    }
                }

                // Ajusta largura das colunas
                for(int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(fileOut);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Relatório Excel exportado com sucesso para:\n" + file.getAbsolutePath());
                closeWindow();

            } catch (IOException e) {
                System.err.println("Erro ao exportar Excel: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro de Exportação", "Ocorreu um erro ao gerar o arquivo Excel.");
            } catch (Exception e) { // Captura genérica para erros na busca de dados
                System.err.println("Erro ao buscar dados para Excel: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro de Dados", "Ocorreu um erro ao buscar os dados para o relatório.");
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
            headerList.addAll(List.of("Média Geral Comp.", "Média Assiduidade", "Média Participação", "Média Responsab.", "Média Sociab.", "Avaliações no Período"));
        }
        if ("Apenas Simulados".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
            headerList.addAll(List.of("Média Geral Simulados (0-10)", "Simulados Realizados", "Acerto Médio MC (%)"));
        }
        if ("Consolidado".equals(tipoRelatorio)) {
            headerList.add("Score de Progresso (0-10)");
        }

        return headerList.toArray(new String[0]);
    }

    /**
     * Busca os dados agregados dos alunos no escopo, aplicando filtros de data.
     * Retorna uma lista de arrays de objetos, onde cada array representa uma linha.
     */
    private List<Object[]> fetchDataForExport() {
        List<Object[]> results = new ArrayList<>();
        String tipoRelatorio = conteudoRelatorioComboBox.getValue();
        // Não precisamos mais dos formatadores aqui, POI/CSV lidam com tipos

        // Itera sobre cada aluno no escopo definido pelos filtros
        for (String alunoId : alunosIdsDoEscopo) {
            User aluno = userDAO.findById(alunoId);
            if (aluno == null) continue;

            List<Object> rowData = new ArrayList<>(List.of(aluno.getId(), aluno.getNome(), aluno.getMatricula()));
            List<String> alunoIdList = List.of(alunoId); // Lista com um único aluno para os métodos DAO

            // --- Dados Comportamentais (com filtro de data) ---
            if ("Apenas Comportamental".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                // *** SUBSTITUIÇÃO DOS PLACEHOLDERS ***
                double mediaGeralComp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "media_geral", dataInicial, dataFinal);
                double mediaAssid = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "assiduidade", dataInicial, dataFinal);
                double mediaPart = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "participacao", dataInicial, dataFinal);
                double mediaResp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "responsabilidade", dataInicial, dataFinal);
                double mediaSociab = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "sociabilidade", dataInicial, dataFinal);
                int countAval = avaliacaoDAO.countByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);

                // Adiciona Double ou "N/A"
                rowData.add(mediaGeralComp >= 0 ? mediaGeralComp : "N/A");
                rowData.add(mediaAssid >= 0 ? mediaAssid : "N/A");
                rowData.add(mediaPart >= 0 ? mediaPart : "N/A");
                rowData.add(mediaResp >= 0 ? mediaResp : "N/A");
                rowData.add(mediaSociab >= 0 ? mediaSociab : "N/A");
                rowData.add(countAval); // Integer
            }

            // --- Dados de Simulados (com filtro de data) ---
            if ("Apenas Simulados".equals(tipoRelatorio) || "Consolidado".equals(tipoRelatorio)) {
                // *** SUBSTITUIÇÃO DOS PLACEHOLDERS ***
                // Nota: getAggregateSimuladosMediaByDateRange já espera uma lista
                double mediaGeralSim = alunoRespostaDAO.getAggregateSimuladosMediaByDateRange(alunoIdList, dataInicial, dataFinal);
                int countSimRealizados = alunoRespostaDAO.countSimuladosRealizadosByAlunosIdsAndDateRange(alunoIdList, dataInicial, dataFinal);
                double mediaAcertosMC = alunoRespostaDAO.calculateMediaAcertosMCByDateRange(alunoIdList, dataInicial, dataFinal);


                rowData.add(mediaGeralSim >= 0 ? mediaGeralSim : "N/A");
                rowData.add(countSimRealizados); // Integer
                rowData.add(mediaAcertosMC >= 0 ? mediaAcertosMC : "N/A"); // Média percentual como Double
            }

            // --- Score Consolidado ---
            if ("Consolidado".equals(tipoRelatorio)) {
                // Busca novamente os dados filtrados para calcular o score
                double mediaGeralComp = avaliacaoDAO.getAggregateMediaByDateRange(alunoIdList, "media_geral", dataInicial, dataFinal);
                double mediaGeralSim = alunoRespostaDAO.getAggregateSimuladosMediaByDateRange(alunoIdList, dataInicial, dataFinal);

                Double score = null; // Usar Double para permitir null
                if (mediaGeralComp >= 0 && mediaGeralSim >= 0) {
                    double compNorm = mediaGeralComp * 2.0;
                    if (compNorm > 0 || mediaGeralSim > 0) {
                        score = (compNorm + mediaGeralSim) / 2.0;
                    } else {
                        score = 0.0;
                    }
                }
                rowData.add(score != null ? score : "N/A"); // Adiciona Double ou "N/A"
            }

            results.add(rowData.toArray());
        }

        return results;
    }

    // --- Placeholders removidos ---

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

