package br.com.undb.visionclass.visionclassdesktop;

import br.com.undb.visionclass.visionclassdesktop.dao.TurmaDAO;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.session.UserSession;
import javafx.event.ActionEvent; // Importar ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class DashboardProfessorHomeController {

    @FXML
    private VBox minhasTurmasContainer;

    private TurmaDAO turmaDAO = new TurmaDAO();

    // Referência ao controlador principal do dashboard
    private DashboardProfessorController mainController;

    @FXML
    public void initialize() {
        loadMinhasTurmas();
    }

    // Método para injetar a referência do controlador principal
    public void setMainController(DashboardProfessorController mainController) {
        this.mainController = mainController;
    }

    private void loadMinhasTurmas() {
        minhasTurmasContainer.getChildren().clear();

        User loggedInProfessor = UserSession.getInstance().getLoggedInUser();
        if (loggedInProfessor == null) return;

        List<Turma> turmasDoProfessor = turmaDAO.findByProfessorId(loggedInProfessor.getId());

        if (turmasDoProfessor.isEmpty()) {
            minhasTurmasContainer.getChildren().add(new Label("Você ainda não está responsável por nenhuma turma."));
        } else {
            for (Turma turma : turmasDoProfessor) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("turma-card-view.fxml"));
                    VBox cardNode = loader.load();

                    TurmaCardController cardController = loader.getController();
                    cardController.setData(turma);

                    minhasTurmasContainer.getChildren().add(cardNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- Métodos para os botões de Ação Rápida ---

    @FXML
    private void handleAvaliarTurma(ActionEvent event) {
        if (mainController != null) {
            mainController.onCarometroClick(event); // Chama o método de navegação no controlador principal
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardProfessorHomeController.");
        }
    }

    @FXML
    private void handleCriarQuestao(ActionEvent event) {
        if (mainController != null) {
            mainController.onBancoQuestoesClick(event); // Chama o método de navegação no controlador principal
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardProfessorHomeController.");
        }
    }

    @FXML
    private void handleGerarSimulado(ActionEvent event) {
        if (mainController != null) {
            mainController.onSimuladosClick(event); // Chama o método de navegação no controlador principal
        } else {
            System.err.println("Erro: mainController não foi injetado em DashboardProfessorHomeController.");
        }
    }
}