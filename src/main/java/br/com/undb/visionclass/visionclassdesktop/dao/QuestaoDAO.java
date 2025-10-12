package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestaoDAO {

    /**
     * Salva uma nova questão e suas alternativas (se houver) no banco de dados.
     * Usa uma transação para garantir a integridade dos dados.
     */
    public void save(Questao questao) {
        String sqlQuestao = "INSERT INTO questoes (enunciado, tipo, nivel_dificuldade, disciplina_id, assunto_id, professor_criador_id) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAlternativa = "INSERT INTO alternativas (texto, correta, questao_id) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Inicia a transação

            // 1. Salva a questão principal
            try (PreparedStatement stmtQuestao = conn.prepareStatement(sqlQuestao, Statement.RETURN_GENERATED_KEYS)) {
                stmtQuestao.setString(1, questao.getEnunciado());
                stmtQuestao.setString(2, questao.getTipo().name());
                stmtQuestao.setString(3, questao.getNivelDificuldade().name());
                stmtQuestao.setInt(4, questao.getDisciplinaId());
                stmtQuestao.setInt(5, questao.getAssuntoId());
                stmtQuestao.setString(6, questao.getProfessorCriadorId());
                stmtQuestao.executeUpdate();

                // Recupera o ID gerado para a questão
                ResultSet rs = stmtQuestao.getGeneratedKeys();
                if (rs.next()) {
                    questao.setId(rs.getInt(1));
                }
            }

            // 2. Salva as alternativas, se for de múltipla escolha
            if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA && !questao.getAlternativas().isEmpty()) {
                try (PreparedStatement stmtAlternativa = conn.prepareStatement(sqlAlternativa)) {
                    for (Alternativa alt : questao.getAlternativas()) {
                        stmtAlternativa.setString(1, alt.getTexto());
                        stmtAlternativa.setBoolean(2, alt.isCorreta());
                        stmtAlternativa.setInt(3, questao.getId());
                        stmtAlternativa.addBatch(); // Adiciona a operação em lote
                    }
                    stmtAlternativa.executeBatch(); // Executa todas as inserções de uma vez
                }
            }

            conn.commit(); // Confirma a transação
            System.out.println("Questão e alternativas salvas com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a questão. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Desfaz a transação em caso de erro
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Busca todas as questões cadastradas.
     */
    public List<Questao> findAll() {
        String sql = "SELECT * FROM questoes ORDER BY id DESC";
        List<Questao> questoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Questao q = new Questao();
                q.setId(rs.getInt("id"));
                q.setEnunciado(rs.getString("enunciado"));
                q.setTipo(TipoQuestao.valueOf(rs.getString("tipo")));
                q.setNivelDificuldade(NivelDificuldade.valueOf(rs.getString("nivel_dificuldade")));
                q.setDisciplinaId(rs.getInt("disciplina_id"));
                q.setAssuntoId(rs.getInt("assunto_id"));
                q.setProfessorCriadorId(rs.getString("professor_criador_id"));

                // Futuramente, podemos carregar as alternativas aqui também se necessário
                questoes.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as questões.");
            e.printStackTrace();
        }
        return questoes;
    }

    // Métodos para update e delete serão adicionados futuramente.
}