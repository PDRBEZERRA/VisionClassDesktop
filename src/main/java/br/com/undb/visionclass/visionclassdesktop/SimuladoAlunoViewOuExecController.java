package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.dao.QuestaoDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class SimuladoAlunoViewOuExecController {

    @FXML
    private VBox rootContainer;
    @FXML
    private Label tituloSimuladoLabel;

    private Simulado simuladoAtual;
    private User alunoLogado;
    private QuestaoDAO questaoDAO = new QuestaoDAO();
    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    public void setSimulado(Simulado simulado) {
        this.simuladoAtual = simulado;
        this.alunoLogado = UserSession.getInstance().getLoggedInUser();
        tituloSimuladoLabel.setText(simulado.getTitulo());

        // Lógica de Decisão
        List<Integer> simuladosRealizados = alunoRespostaDAO.findSimuladosRealizadosIdsByAluno(alunoLogado.getId());
        boolean simuladoJaFeito = simuladosRealizados.contains(simulado.getId());

        if (simuladoJaFeito) {
            loadNotaView();
        } else {
            // Se for iniciar, carrega as questões e a view de execução
            List<Questao> questoes = questaoDAO.findQuestoesBySimuladoId(simulado.getId());
            if (questoes.isEmpty()) {
                showError("Simulado Vazio", "Este simulado não possui questões associadas e não pode ser iniciado.");
                // Fechar a janela se não houver questões
                rootContainer.getScene().getWindow().hide();
            } else {
                loadExecucaoView(questoes);
            }
        }
    }

    private void loadNotaView() {
        // Lógica de Nota: Apenas um placeholder finalizado por enquanto.
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(tituloSimuladoLabel);

        double nota = alunoRespostaDAO.getNotaByAlunoAndSimulado(alunoLogado.getId(), simuladoAtual.getId());

        String notaTexto;
        if (nota == -2) {
            // Caso em que a nota ainda não foi totalmente corrigida (código de retorno -2)
            notaTexto = "Simulado Aguardando Correção do Professor (Questões Discursivas).";
        } else if (nota >= 0) {
            // Nota final em pontos absolutos (formatação com vírgula)
            notaTexto = String.format("Sua Nota: %.2f pontos", nota).replace(".", ",");
        } else {
            // Caso a nota seja -1 (sem respostas)
            notaTexto = "Não há dados de nota disponíveis para este simulado.";
        }

        Label notaLabel = new Label(notaTexto);
        notaLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        rootContainer.getChildren().add(notaLabel);
        rootContainer.getChildren().add(new Label("Obrigado por participar."));
    }

    private void loadExecucaoView(List<Questao> questoes) {
        try {
            // Carrega o FXML da execução do simulado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simulado-execucao-view.fxml"));
            Parent execucaoView = loader.load();

            // Pega o controller e injeta os dados essenciais
            SimuladoExecucaoController execucaoController = loader.getController();
            execucaoController.setDadosSimulado(simuladoAtual, questoes);

            // Substitui o conteúdo do rootContainer pelo conteúdo da execução
            rootContainer.getChildren().clear();
            VBox.setVgrow(execucaoView, Priority.ALWAYS);
            rootContainer.getChildren().add(execucaoView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erro de Carregamento", "Não foi possível carregar a tela de execução do simulado.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        // Inicialização padrão
    }
}