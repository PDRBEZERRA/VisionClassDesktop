package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static ScreenManager screenManager;

    @Override
    public void start(Stage stage) throws IOException {
        // A linha do Ikonli foi REMOVIDA daqui

        ConnectionFactory.initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        screenManager = new ScreenManager(scene);

        scene.getRoot().setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6, #14b8a6);");

        stage.setTitle("VisionClass");
        stage.setScene(scene);
        stage.show();
    }

    public static ScreenManager getScreenManager() {
        return screenManager;
    }
}