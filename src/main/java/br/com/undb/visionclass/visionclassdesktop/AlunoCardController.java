package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

public class AlunoCardController {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label nomeLabel;
    @FXML
    private Label matriculaLabel;
    @FXML
    private VBox cardRoot;

    private User aluno;
    private Turma turma;

    public void setData(User aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
        nomeLabel.setText(aluno.getNome());
        matriculaLabel.setText(aluno.getMatricula());
        loadUserAvatar(aluno.getFoto());
    }

    @FXML
    private void initialize() {
        cardRoot.setOnMouseClicked(this::handleCardClick);
    }

    private void handleCardClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("avaliacao-comportamental-view.fxml"));
            Parent root = loader.load();

            AvaliacaoComportamentalController controller = loader.getController();
            controller.setData(this.aluno, this.turma);
            Stage modalStage = new Stage();
            modalStage.initStyle(StageStyle.DECORATED);
            modalStage.setTitle("Avaliação Comportamental de " + this.aluno.getNome());
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserAvatar(String photoFileName) {
        try {
            Image image;
            if (photoFileName != null && !photoFileName.isEmpty()) {
                File file = new File("user_photos/" + photoFileName);
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            avatarImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do avatar para o aluno.");
            e.printStackTrace();
        }
    }
}