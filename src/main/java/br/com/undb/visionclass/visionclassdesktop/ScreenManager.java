package br.com.undb.visionclass.visionclassdesktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class ScreenManager {

    private Scene scene;

    public ScreenManager(Scene scene) {
        this.scene = scene;
    }

    public void switchTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            scene.setRoot(root);

            if (fxmlFile.equals("login-view.fxml")) {
                scene.getRoot().setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6, #14b8a6);");
            } else {
                scene.getRoot().setStyle(null);
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar o ficheiro FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}