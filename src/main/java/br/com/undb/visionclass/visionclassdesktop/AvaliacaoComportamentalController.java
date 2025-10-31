package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.AvaliacaoComportamentalDAO;
import br.com.undb.visionclass.visionclassdesktop.model.AvaliacaoComportamental;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class AvaliacaoComportamentalController {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label nomeAlunoLabel;
    @FXML
    private Label matriculaLabel;
    @FXML
    private Label turmaLabel;
    @FXML
    private ToggleGroup assiduidadeGroup;
    @FXML
    private ToggleGroup participacaoGroup;
    @FXML
    private ToggleGroup responsabilidadeGroup;
    @FXML
    private ToggleGroup sociabilidadeGroup;
    @FXML
    private TextArea observacoesTextArea;

    private User aluno;
    private Turma turma;
    private AvaliacaoComportamentalDAO avaliacaoDAO = new AvaliacaoComportamentalDAO();
    private AvaliacaoComportamental avaliacaoExistente;

    public void setData(User aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;

        nomeAlunoLabel.setText(aluno.getNome());
        matriculaLabel.setText("Matrícula: " + aluno.getMatricula());
        turmaLabel.setText("Turma: " + turma.getNome());
        loadUserAvatar(aluno.getFoto());

        carregarAvaliacaoExistente();
    }

    private void carregarAvaliacaoExistente() {
        this.avaliacaoExistente = avaliacaoDAO.findLatestByAlunoAndTurma(aluno.getId(), turma.getId());
        if (this.avaliacaoExistente != null) {
            preencherFormulario(this.avaliacaoExistente);
        }
    }

    private void preencherFormulario(AvaliacaoComportamental avaliacao) {
        observacoesTextArea.setText(avaliacao.getObservacoes());
        setToggleGroupValue(assiduidadeGroup, avaliacao.getAssiduidade());
        setToggleGroupValue(participacaoGroup, avaliacao.getParticipacao());
        setToggleGroupValue(responsabilidadeGroup, avaliacao.getResponsabilidade());
        setToggleGroupValue(sociabilidadeGroup, avaliacao.getSociabilidade());
    }

    @FXML
    private void handleSalvar(ActionEvent event) {
        if (avaliacaoExistente != null) {
            avaliacaoExistente.setAssiduidade(getToggleGroupValue(assiduidadeGroup));
            avaliacaoExistente.setParticipacao(getToggleGroupValue(participacaoGroup));
            avaliacaoExistente.setResponsabilidade(getToggleGroupValue(responsabilidadeGroup));
            avaliacaoExistente.setSociabilidade(getToggleGroupValue(sociabilidadeGroup));
            avaliacaoExistente.setObservacoes(observacoesTextArea.getText());
            avaliacaoExistente.setData(LocalDate.now());
            avaliacaoExistente.setProfessorId(UserSession.getInstance().getLoggedInUser().getId());

            avaliacaoDAO.update(avaliacaoExistente);
            mostrarAlerta("Avaliação Atualizada", "A avaliação de " + aluno.getNome() + " foi atualizada.");
        } else {
            AvaliacaoComportamental novaAvaliacao = new AvaliacaoComportamental();
            novaAvaliacao.setId(UUID.randomUUID().toString());
            novaAvaliacao.setAlunoId(aluno.getId());
            novaAvaliacao.setProfessorId(UserSession.getInstance().getLoggedInUser().getId());
            novaAvaliacao.setTurmaId(turma.getId());
            novaAvaliacao.setData(LocalDate.now());
            novaAvaliacao.setObservacoes(observacoesTextArea.getText());
            novaAvaliacao.setAssiduidade(getToggleGroupValue(assiduidadeGroup));
            novaAvaliacao.setParticipacao(getToggleGroupValue(participacaoGroup));
            novaAvaliacao.setResponsabilidade(getToggleGroupValue(responsabilidadeGroup));
            novaAvaliacao.setSociabilidade(getToggleGroupValue(sociabilidadeGroup));

            avaliacaoDAO.save(novaAvaliacao);
            mostrarAlerta("Avaliação Salva", "A avaliação de " + aluno.getNome() + " foi salva com sucesso.");
        }
        closeWindow();
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void handleVerHistorico(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("historico-avaliacao-view.fxml"));
            Parent root = loader.load();

            HistoricoAvaliacaoController controller = loader.getController();
            controller.setData(aluno, turma);

            Stage modalStage = new Stage();
            modalStage.initStyle(StageStyle.DECORATED);
            modalStage.setTitle("Histórico de " + aluno.getNome());
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o histórico de avaliações.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private int getToggleGroupValue(ToggleGroup group) {
        ToggleButton selected = (ToggleButton) group.getSelectedToggle();
        if (selected == null) return 0;
        return group.getToggles().indexOf(selected) + 1;
    }

    private void setToggleGroupValue(ToggleGroup group, int value) {
        if (value > 0 && value <= group.getToggles().size()) {
            group.selectToggle(group.getToggles().get(value - 1));
        }
    }

    private void loadUserAvatar(String photoFileName) {
        try {
            Image image;
            if (photoFileName != null && !photoFileName.isEmpty()) {
                File file = new File("user_photos/" + photoFileName);
                if (file.exists() && !file.isDirectory()) {
                    image = new Image(file.toURI().toString());
                } else {
                    image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
            }

            if (image == null || image.isError()) {
                image = new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg"));
            }

            avatarImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar: " + e.getMessage());
            try {
                avatarImageView.setImage(new Image(getClass().getResourceAsStream("/br/com/undb/visionclass/visionclassdesktop/images/avatar.jpg")));
            } catch (Exception ex) {
                System.err.println("Falha total ao carregar qualquer imagem.");
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nomeAlunoLabel.getScene().getWindow();
        stage.close();
    }
}