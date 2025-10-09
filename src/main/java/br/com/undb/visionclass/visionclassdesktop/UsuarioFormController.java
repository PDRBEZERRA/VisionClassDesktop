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
    private DashboardController mainDashboardController; // Referência ao controlador principal

    // Método para receber a referência
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
            Image image = new Image(file.toURI().toString());
            avatarImageView.setImage(image);
        }
    }

    @FXML
    private void onSalvarButtonClick() {
        String photoPath = (userToEdit != null) ? userToEdit.getFoto() : null;
        if (selectedPhotoFile != null) {
            photoPath = savePhotoToFileSystem(selectedPhotoFile);
        }

        if (userToEdit == null) { // Modo Criação
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
        } else { // Modo Edição
            userToEdit.setNome(nomeTextField.getText());
            userToEdit.setEmail(emailTextField.getText());
            userToEdit.setMatricula(matriculaTextField.getText());
            userToEdit.setCpf(cpfTextField.getText());
            userToEdit.setRole(roleComboBox.getValue());
            userToEdit.setFoto(photoPath);
            userDAO.update(userToEdit);

            // Se o utilizador editado for o mesmo que está logado...
            if (userToEdit.getId().equals(UserSession.getInstance().getLoggedInUser().getId())) {
                // Atualiza a sessão
                UserSession.getInstance().setLoggedInUser(userToEdit);
                // E notifica o Dashboard para refrescar o perfil
                if (mainDashboardController != null) {
                    mainDashboardController.refreshUserProfile();
                }
            }
        }
        closeWindow();
    }

    /**
     * Guarda a foto selecionada numa pasta 'user_photos' na raiz do projeto e retorna apenas o nome do ficheiro.
     * @param photoFile O ficheiro da foto a ser guardado.
     * @return O nome do ficheiro da foto guardada.
     */
    private String savePhotoToFileSystem(File photoFile) {
        try {
            // Cria uma pasta 'user_photos' na raiz do projeto, se não existir
            Path targetDir = Paths.get("user_photos");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Cria um nome de ficheiro único para evitar conflitos
            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getName();
            Path targetPath = targetDir.resolve(fileName);

            // Copia o ficheiro selecionado para o novo local
            Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retorna APENAS o nome do ficheiro para ser guardado na base de dados
            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Retorna nulo em caso de erro
        }
    }

    /**
     * Carrega a imagem do avatar no ImageView a partir de um nome de ficheiro.
     * @param photoFileName O nome do ficheiro da imagem (e não o caminho completo).
     */
    private void loadAvatarImage(String photoFileName) {
        try {
            Image image;
            if (photoFileName != null && !photoFileName.isEmpty()) {
                // Constrói o caminho para o ficheiro dentro da pasta 'user_photos'
                File file = new File("user_photos/" + photoFileName);
                if (file.exists()) {
                    image = new Image(file.toURI().toString());
                } else {
                    // Se o ficheiro não for encontrado, usa a imagem padrão
                    image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
                }
            } else {
                // Se não houver nome de ficheiro, usa a imagem padrão
                image = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            }
            avatarImageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
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