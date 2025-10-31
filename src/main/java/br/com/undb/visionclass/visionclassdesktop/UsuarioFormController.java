package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class UsuarioFormController {

    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField senhaPasswordField;
    @FXML
    private TextField matriculaTextField;
    @FXML
    private TextField cpfTextField;
    @FXML
    private ComboBox<UserRole> roleComboBox;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Button salvarButton;

    private UserDAO userDAO = new UserDAO();
    private User userToEdit;
    private File selectedPhotoFile;
    private DashboardController mainDashboardController;

    public void setMainDashboardController(DashboardController controller) {
        this.mainDashboardController = controller;
    }

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(UserRole.values());
        loadAvatarImage(null);
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        nomeTextField.setText(user.getNome());
        emailTextField.setText(user.getEmail());
        matriculaTextField.setText(user.getMatricula());
        cpfTextField.setText(user.getCpf());
        roleComboBox.setValue(user.getRole());
        senhaPasswordField.setDisable(true);
        senhaPasswordField.setPromptText("Senha não pode ser alterada aqui");
        loadAvatarImage(user.getFoto());
    }

    @FXML
    private void onAnexarFotoClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(salvarButton.getScene().getWindow());

        if (file != null) {
            selectedPhotoFile = file;
            try (FileInputStream fis = new FileInputStream(file)) {
                Image image = new Image(fis);
                avatarImageView.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onSalvarButtonClick() {
        String photoPath = (userToEdit != null) ? userToEdit.getFoto() : null;
        if (selectedPhotoFile != null) {
            photoPath = savePhotoToFileSystem(selectedPhotoFile);
        }

        if (userToEdit == null) {
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setNome(nomeTextField.getText());
            newUser.setEmail(emailTextField.getText());
            newUser.setSenha(senhaPasswordField.getText());
            newUser.setMatricula(matriculaTextField.getText());
            newUser.setCpf(cpfTextField.getText());
            newUser.setRole(roleComboBox.getValue());
            newUser.setFoto(photoPath);
            userDAO.save(newUser);
        } else {
            userToEdit.setNome(nomeTextField.getText());
            userToEdit.setEmail(emailTextField.getText());
            userToEdit.setMatricula(matriculaTextField.getText());
            userToEdit.setCpf(cpfTextField.getText());
            userToEdit.setRole(roleComboBox.getValue());
            userToEdit.setFoto(photoPath);
            userDAO.update(userToEdit);

            if (userToEdit.getId().equals(UserSession.getInstance().getLoggedInUser().getId())) {
                UserSession.getInstance().setLoggedInUser(userToEdit);
                if (mainDashboardController != null) {
                    mainDashboardController.refreshUserProfile();
                }
            }
        }
        closeWindow();
    }

    private String savePhotoToFileSystem(File photoFile) {
        try {
            Path targetDir = Paths.get("user_photos");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getName();
            Path targetPath = targetDir.resolve(fileName);

            Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void loadAvatarImage(String photoFileName) {
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

    @FXML
    private void onCancelarButtonClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }
}