package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene; // Novo Import
import javafx.scene.layout.Pane; // Import para o nó raiz temporário

import java.io.IOException;

public class HelloApplication extends Application {

    private static ScreenManager screenManager;

    @Override
    public void start(Stage stage) throws IOException {
        ConnectionFactory.initializeDatabase();

        // 1. Cria um Scene inicial (apenas um placeholder)
        Pane root = new Pane();
        Scene scene = new Scene(root, 1000, 700); // 1000x700 é um bom tamanho inicial

        // 2. Cria o ScreenManager com o Scene
        screenManager = new ScreenManager(scene);

        // 3. Configura e mostra o Stage
        stage.setScene(scene);
        stage.setTitle("VisionClass Desktop");
        stage.show();

        // 4. Carrega a tela de login (o ScreenManager agora define o 'root' do Scene)
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