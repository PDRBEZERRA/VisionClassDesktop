package br.com.undb.visionclass.visionclassdesktop;

/**
 * Esta classe serve como um ponto de entrada separado para a aplicação,
 * o que resolve problemas de empacotamento em um JAR executável.
 */
public class Launcher {
    public static void main(String[] args) {
        // A forma correta de iniciar a aplicação JavaFX a partir de uma classe que não estende Application.
        HelloApplication.launch(HelloApplication.class, args);
    }
}