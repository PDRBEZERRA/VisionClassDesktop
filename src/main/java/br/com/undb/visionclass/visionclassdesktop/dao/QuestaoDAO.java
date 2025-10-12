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
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    // --- NOVO MÉTODO DE UPDATE ---
    public void update(Questao questao) {
        String sqlUpdateQuestao = "UPDATE questoes SET enunciado = ?, tipo = ?, nivel_dificuldade = ?, disciplina_id = ?, assunto_id = ? WHERE id = ?";
        String sqlDeleteAlternativas = "DELETE FROM alternativas WHERE questao_id = ?";
        String sqlInsertAlternativa = "INSERT INTO alternativas (texto, correta, questao_id) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Atualiza a questão principal
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateQuestao)) {
                stmt.setString(1, questao.getEnunciado());
                stmt.setString(2, questao.getTipo().name());
                stmt.setString(3, questao.getNivelDificuldade().name());
                stmt.setInt(4, questao.getDisciplinaId());
                stmt.setInt(5, questao.getAssuntoId());
                stmt.setInt(6, questao.getId());
                stmt.executeUpdate();
            }

            // 2. Deleta as alternativas antigas
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAlternativas)) {
                stmt.setInt(1, questao.getId());
                stmt.executeUpdate();
            }

            // 3. Insere as novas alternativas (se houver)
            if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA && !questao.getAlternativas().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertAlternativa)) {
                    for (Alternativa alt : questao.getAlternativas()) {
                        stmt.setString(1, alt.getTexto());
                        stmt.setBoolean(2, alt.isCorreta());
                        stmt.setInt(3, questao.getId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit(); // Confirma a transação
            System.out.println("Questão ID " + questao.getId() + " atualizada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar a questão. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }

    // --- MÉTODO PARA BUSCAR QUESTÃO COMPLETA (existente) ---
    public Questao findById(int questaoId) {
        String sqlQuestao = "SELECT * FROM questoes WHERE id = ?";
        String sqlAlternativas = "SELECT * FROM alternativas WHERE questao_id = ?";
        Questao questao = null;

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Busca a questão
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuestao)) {
                stmt.setInt(1, questaoId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    questao = new Questao();
                    questao.setId(rs.getInt("id"));
                    questao.setEnunciado(rs.getString("enunciado"));
                    questao.setTipo(TipoQuestao.valueOf(rs.getString("tipo")));
                    questao.setNivelDificuldade(NivelDificuldade.valueOf(rs.getString("nivel_dificuldade")));
                    questao.setDisciplinaId(rs.getInt("disciplina_id"));
                    questao.setAssuntoId(rs.getInt("assunto_id"));
                    questao.setProfessorCriadorId(rs.getString("professor_criador_id"));
                }
            }

            // Se a questão foi encontrada e é de múltipla escolha, busca as alternativas
            if (questao != null && questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlAlternativas)) {
                    stmt.setInt(1, questaoId);
                    ResultSet rs = stmt.executeQuery();
                    List<Alternativa> alternativas = new ArrayList<>();
                    while (rs.next()) {
                        Alternativa alt = new Alternativa();
                        alt.setId(rs.getInt("id"));
                        alt.setTexto(rs.getString("texto"));
                        alt.setCorreta(rs.getBoolean("correta"));
                        alt.setQuestaoId(rs.getInt("questao_id"));
                        alternativas.add(alt);
                    }
                    questao.setAlternativas(alternativas);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar questão por ID.");
            e.printStackTrace();
        }
        return questao;
    }

    // --- NOVO MÉTODO: BUSCA QUESTÕES COMPLETAS DE UM SIMULADO ---
    /**
     * Busca todas as questões completas (incluindo alternativas) associadas a um Simulado.
     * @param simuladoId O ID do simulado.
     * @return Uma lista de objetos Questao completos.
     */
    public List<Questao> findQuestoesBySimuladoId(int simuladoId) {
        // 1. Primeiro, encontra os IDs das questões associadas ao simulado
        String sqlQuestaoIds = "SELECT questao_id FROM simulado_questoes WHERE simulado_id = ?";
        List<Integer> questaoIds = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuestaoIds)) {

            stmt.setInt(1, simuladoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                questaoIds.add(rs.getInt("questao_id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de questões do simulado.");
            e.printStackTrace();
            return new ArrayList<>(); // Retorna lista vazia em caso de erro
        }

        // 2. Agora, busca os detalhes completos de cada questão
        List<Questao> questoesCompletas = new ArrayList<>();
        // Reutilizamos a lógica existente do findById
        for (int id : questaoIds) {
            Questao questao = findById(id); // findById já cuida da conexão e alternativas
            if (questao != null) {
                questoesCompletas.add(questao);
            }
        }

        return questoesCompletas;
    }


    public List<Questao> findByFilters(Integer disciplinaId, Integer assuntoId) {
        // ... (código existente sem alterações)
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