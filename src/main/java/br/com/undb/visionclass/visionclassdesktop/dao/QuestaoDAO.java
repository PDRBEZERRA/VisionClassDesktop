package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestaoDAO {

    public void save(Questao questao) {
        String sqlQuestao = "INSERT INTO questoes (enunciado, tipo, nivel_dificuldade, disciplina_id, assunto_id, professor_criador_id) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAlternativa = "INSERT INTO alternativas (texto, correta, questao_id) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtQuestao = conn.prepareStatement(sqlQuestao, Statement.RETURN_GENERATED_KEYS)) {
                stmtQuestao.setString(1, questao.getEnunciado());
                stmtQuestao.setString(2, questao.getTipo().name());
                stmtQuestao.setString(3, questao.getNivelDificuldade().name());
                stmtQuestao.setInt(4, questao.getDisciplinaId());
                stmtQuestao.setInt(5, questao.getAssuntoId());
                stmtQuestao.setString(6, questao.getProfessorCriadorId());
                stmtQuestao.executeUpdate();

                ResultSet rs = stmtQuestao.getGeneratedKeys();
                if (rs.next()) {
                    questao.setId(rs.getInt(1));
                }
            }

            if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA && !questao.getAlternativas().isEmpty()) {
                try (PreparedStatement stmtAlternativa = conn.prepareStatement(sqlAlternativa)) {
                    for (Alternativa alt : questao.getAlternativas()) {
                        stmtAlternativa.setString(1, alt.getTexto());
                        stmtAlternativa.setBoolean(2, alt.isCorreta());
                        stmtAlternativa.setInt(3, questao.getId());
                        stmtAlternativa.addBatch();
                    }
                    stmtAlternativa.executeBatch();
                }
            }

            conn.commit();
            System.out.println("Questão e alternativas salvas com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a questão. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
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

    // --- MÉTODO FINDALL SUBSTITUÍDO POR FINDBYFILTERS ---
    /**
     * Busca questões com base nos filtros de disciplina e assunto.
     * @param disciplinaId O ID da disciplina (pode ser nulo).
     * @param assuntoId O ID do assunto (pode ser nulo).
     * @return Uma lista de questões que correspondem aos filtros.
     */
    public List<Questao> findByFilters(Integer disciplinaId, Integer assuntoId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM questoes");
        List<Object> params = new ArrayList<>();

        if (disciplinaId != null) {
            sql.append(" WHERE disciplina_id = ?");
            params.add(disciplinaId);
        }
        if (assuntoId != null) {
            if (params.isEmpty()) {
                sql.append(" WHERE assunto_id = ?");
            } else {
                sql.append(" AND assunto_id = ?");
            }
            params.add(assuntoId);
        }

        sql.append(" ORDER BY id DESC");

        List<Questao> questoes = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Define os parâmetros na consulta
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Questao q = new Questao();
                q.setId(rs.getInt("id"));
                q.setEnunciado(rs.getString("enunciado"));
                q.setTipo(TipoQuestao.valueOf(rs.getString("tipo")));
                q.setNivelDificuldade(NivelDificuldade.valueOf(rs.getString("nivel_dificuldade")));
                q.setDisciplinaId(rs.getInt("disciplina_id"));
                q.setAssuntoId(rs.getInt("assunto_id"));
                q.setProfessorCriadorId(rs.getString("professor_criador_id"));
                questoes.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar questões por filtros.");
            e.printStackTrace();
        }
        return questoes;
    }

    public void delete(int questaoId) {
        String sql = "DELETE FROM questoes WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questaoId);
            stmt.executeUpdate();
            System.out.println("Questão ID " + questaoId + " deletada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao deletar a questão.");
            e.printStackTrace();
        }
    }
}