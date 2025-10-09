package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class TurmasController {

    @FXML
    private TilePane turmasTilePane;

    private TurmaDAO turmaDAO = new TurmaDAO();

    @FXML
    public void initialize() {
        loadTurmasData();
    }

    private void loadTurmasData() {
        // Limpa quaisquer cards que já existam para evitar duplicação
        turmasTilePane.getChildren().clear();

        // Busca todas as turmas do banco de dados
        List<Turma> turmas = turmaDAO.findAll();

        // Para cada turma na lista, cria e adiciona um card
        for (Turma turma : turmas) {
            try {
                // Carrega o FXML do "molde" do card
                FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-card-view.fxml"));
                VBox cardNode = loader.load();

                // Pega o controlador do card que acabamos de carregar
                TurmaCardController cardController = loader.getController();
                // Envia os dados da turma atual para o controlador do card preenchê-lo
                cardController.setData(turma);

                // Adiciona o card preenchido ao TilePane
                turmasTilePane.getChildren().add(cardNode);

            } catch (IOException e) {
                System.err.println("Erro ao carregar o card da turma: " + turma.getNome());
                e.printStackTrace();
            }
        }
    }

    /**
     * Chamado quando o botão "Adicionar Nova Turma" é clicado.
     * Abre uma nova janela modal com o formulário de turma.
     */
    @FXML
    private void onAdicionarButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-form-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Adicionar Nova Turma");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a janela principal
            stage.setResizable(false);
            stage.showAndWait(); // Mostra a janela e espera que ela seja fechada

            // Após o formulário ser fechado, atualiza a lista de cards
            loadTurmasData();

        } catch (IOException e) {
            System.err.println("Erro ao abrir o formulário de nova turma.");
            e.printStackTrace();
        }
    }
}