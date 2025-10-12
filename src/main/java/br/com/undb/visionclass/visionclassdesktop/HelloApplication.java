package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static ScreenManager screenManager;

    @Override
    public void start(Stage stage) throws IOException {
        ConnectionFactory.initializeDatabase();
        screenManager = new ScreenManager(stage);
        screenManager.switchTo("login-view.fxml");
    }

    public static ScreenManager getScreenManager() {
        return screenManager;
    }

    /**
     * O método main agora é muito mais simples e apenas chama launch(),
     * que é a maneira padrão de iniciar uma aplicação JavaFX.
     */
    public static void main(String[] args) {
        launch(args);
    }
}