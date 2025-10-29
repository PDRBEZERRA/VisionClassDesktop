package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class HelloApplication extends Application {

    private static ScreenManager screenManager;

    @Override
    public void start(Stage stage) throws IOException {
        ConnectionFactory.initializeDatabase();

        Pane root = new Pane();
        Scene scene = new Scene(root, 1000, 700); // 1000x700 é um bom tamanho inicial

        screenManager = new ScreenManager(scene);

        stage.setScene(scene);
        stage.setTitle("VisionClass Desktop");
        stage.show();

        screenManager.switchTo("login-view.fxml");
    }

    public static ScreenManager getScreenManager() {
        return screenManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}