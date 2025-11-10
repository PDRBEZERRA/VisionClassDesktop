package br.com.undb.visionclass.visionclassdesktop;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.image.BufferedImage;
import java.util.List;

public class WebcamCaptureController {

    @FXML
    private ComboBox<Webcam> webcamComboBox;
    @FXML
    private ImageView webcamImageView;
    @FXML
    private Button capturarButton;
    @FXML
    private Button cancelarButton;

    private Webcam webcamSelecionada;
    private Thread videoFeedThread;
    private boolean isCameraAtiva = false;
    private BufferedImage imagemCapturada;

    @FXML
    public void initialize() {
        List<Webcam> webcams = Webcam.getWebcams();

        if (webcams.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhuma webcam foi encontrada.");
            capturarButton.setDisable(true);
            webcamComboBox.setDisable(true);
            return;
        }

        webcamComboBox.getItems().addAll(webcams);
        webcamComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Webcam webcam) {
                return webcam == null ? "Nenhuma" : webcam.getName();
            }
            @Override
            public Webcam fromString(String string) {
                return null;
            }
        });

        webcamComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCam, newCam) -> {
            iniciarWebcam(newCam);
        });

        webcamComboBox.getSelectionModel().selectFirst();
    }

    private void iniciarWebcam(Webcam webcam) {
        pararWebcam();

        webcamSelecionada = webcam;
        webcamSelecionada.setViewSize(WebcamResolution.VGA.getSize());
        webcamSelecionada.open();
        isCameraAtiva = true;

        videoFeedThread = new Thread(() -> {
            while (isCameraAtiva) {
                if (webcamSelecionada.isImageNew()) {
                    BufferedImage awtImage = webcamSelecionada.getImage();
                    Image fxImage = SwingFXUtils.toFXImage(awtImage, null);

                    Platform.runLater(() -> {
                        webcamImageView.setImage(fxImage);
                    });
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        videoFeedThread.setDaemon(true);
        videoFeedThread.start();
    }

    private void pararWebcam() {
        isCameraAtiva = false;

        if (videoFeedThread != null) {
            videoFeedThread.interrupt();
        }

        if (webcamSelecionada != null && webcamSelecionada.isOpen()) {
            webcamSelecionada.close();
        }
    }

    @FXML
    private void handleCapturarFoto() {
        if (webcamSelecionada != null && webcamSelecionada.isOpen()) {
            this.imagemCapturada = webcamSelecionada.getImage();
            pararWebcam();
            fecharJanela();
        }
    }

    @FXML
    private void handleCancelar() {
        pararWebcam();
        fecharJanela();
    }


    public BufferedImage getImagemCapturada() {
        return imagemCapturada;
    }

    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}