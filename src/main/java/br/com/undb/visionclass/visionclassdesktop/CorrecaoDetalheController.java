package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AlunoRespostaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CorrecaoDetalheController {

    @FXML
    private Label alunoQuestaoLabel;
    @FXML
    private Label enunciadoLabel;
    @FXML
    private TextArea respostaAlunoTextArea;
    @FXML
    private TextField notaTextField;
    @FXML
    private TextArea feedbackTextArea;
    @FXML
    private Label notaAtribuidaLabel;

    private User aluno;
    private Simulado simulado;
    private Questao questao;
    private String respostaAluno;

    private AlunoRespostaDAO alunoRespostaDAO = new AlunoRespostaDAO();

    public void setDadosCorrecao(Simulado simulado, User aluno, Questao questao, String respostaAluno) {
        this.simulado = simulado;
        this.aluno = aluno;
        this.questao = questao;
        this.respostaAluno = respostaAluno;

        alunoQuestaoLabel.setText("Correção Detalhada - " + aluno.getNome());
        enunciadoLabel.setText(questao.getEnunciado());
        respostaAlunoTextArea.setText(respostaAluno);

        String maxPontos = String.format("%.2f", questao.getNotaPontuacao()).replace(".", ",");
        notaAtribuidaLabel.setText("Nota Atribuída (Max: " + maxPontos + " pontos):");

        Double notaSalva = alunoRespostaDAO.findNotaAtribuida(aluno.getId(), simulado.getId(), questao.getId());
        if (notaSalva != null) {
            notaTextField.setText(String.format("%.2f", notaSalva).replace(".", ","));
        } else {
            notaTextField.setText("0");
        }
    }

    @FXML
    private void handleSalvarNota() {
        try {
            double nota = Double.parseDouble(notaTextField.getText().replace(",", "."));
            String feedback = feedbackTextArea.getText();

            if (nota < 0 || nota > questao.getNotaPontuacao()) {
                String maxPontos = String.format("%.2f", questao.getNotaPontuacao()).replace(".", ",");
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "A nota deve ser entre 0.0 e " + maxPontos + ".");
                return;
            }

            alunoRespostaDAO.updateCorrecaoDiscursiva(aluno.getId(), simulado.getId(), questao.getId(), nota, feedback);

            Stage stage = (Stage) notaTextField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Por favor, insira um número válido para a nota (use ponto ou vírgula).");
        }
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) notaTextField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}