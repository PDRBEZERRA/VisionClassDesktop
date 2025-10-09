package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.UserDAO;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    // --- Componentes FXML existentes ---
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
    private Button salvarButton;
    @FXML
    private Button cancelarButton;

    // --- Novos componentes FXML para a foto ---
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Button anexarFotoButton;

    private UserDAO userDAO = new UserDAO();
    private User userToEdit;
    private File selectedPhotoFile; // Guarda o ficheiro da foto selecionada

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(UserRole.values());
        // Define uma imagem padrão
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

        // Carrega a foto de perfil existente do utilizador
        loadAvatarImage(user.getFoto());
    }

    /**
     * Chamado quando o botão "Anexar Foto" é clicado.
     */
    @FXML
    private void onAnexarFotoClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        // Filtra para mostrar apenas ficheiros de imagem
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        // Abre a janela de seleção de ficheiro
        File file = fileChooser.showOpenDialog(anexarFotoButton.getScene().getWindow());

        if (file != null) {
            selectedPhotoFile = file;
            // Mostra a imagem selecionada no ImageView
            Image image = new Image(file.toURI().toString());
            avatarImageView.setImage(image);
        }
    }

    @FXML
    private void onSalvarButtonClick() {
        // ... (validações)

        String photoPath = null;
        if (selectedPhotoFile != null) {
            photoPath = savePhotoToFileSystem(selectedPhotoFile);
        } else if (userToEdit != null) {
            photoPath = userToEdit.getFoto(); // Mantém a foto antiga se nenhuma nova for selecionada
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
            newUser.setFoto(photoPath); // Guarda o caminho da foto
            userDAO.save(newUser);
        } else { // Modo Edição
            userToEdit.setNome(nomeTextField.getText());
            userToEdit.setEmail(emailTextField.getText());
            userToEdit.setMatricula(matriculaTextField.getText());
            userToEdit.setCpf(cpfTextField.getText());
            userToEdit.setRole(roleComboBox.getValue());
            userToEdit.setFoto(photoPath); // Guarda o novo caminho da foto
            userDAO.update(userToEdit);
        }

        closeWindow();
    }

    /**
     * Carrega a imagem do avatar no ImageView.
     * @param photoPath O caminho para o ficheiro da imagem.
     */
    private void loadAvatarImage(String photoPath) {
        try {
            if (photoPath != null && !photoPath.isEmpty()) {
                File file = new File(photoPath);
                if (file.exists()) {
                    avatarImageView.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }
            // Se não houver foto ou o ficheiro não for encontrado, carrega a imagem padrão
            Image defaultImage = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            avatarImageView.setImage(defaultImage);
        } catch (Exception e) {
            // Em caso de erro, carrega a imagem padrão
            Image defaultImage = new Image(getClass().getResourceAsStream("images/avatar.jpg"));
            avatarImageView.setImage(defaultImage);
        }
    }

    /**
     * Guarda a foto selecionada numa pasta 'user_photos' e retorna o caminho.
     * @param photoFile O ficheiro da foto a ser guardado.
     * @return O caminho absoluto para a foto guardada.
     */
    private String savePhotoToFileSystem(File photoFile) {
        try {
            // Cria uma pasta 'user_photos' no diretório home do utilizador, se não existir
            Path targetDir = Paths.get(System.getProperty("user.home"), "visionclass_data", "user_photos");
            Files.createDirectories(targetDir);

            // Cria um nome de ficheiro único para evitar conflitos
            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getName();
            Path targetPath = targetDir.resolve(fileName);

            // Copia o ficheiro selecionado para o novo local
            Files.copy(photoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retorna o caminho absoluto do ficheiro guardado
            return targetPath.toAbsolutePath().toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Retorna nulo em caso de erro
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

    // (Pode adicionar o método showAlert aqui se precisar)
}