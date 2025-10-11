package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class AlunoCardController {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label nomeLabel;
    @FXML
    private Label matriculaLabel;

    public void setData(User aluno) {
        nomeLabel.setText(aluno.getNome());
        matriculaLabel.setText(aluno.getMatricula());
        loadUserAvatar(aluno.getFoto());
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