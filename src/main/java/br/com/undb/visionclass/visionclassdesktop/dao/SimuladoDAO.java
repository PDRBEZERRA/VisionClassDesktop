package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.Questao;
import br.com.undb.visionclass.visionclassdesktop.model.Simulado;
import br.com.undb.visionclass.visionclassdesktop.model.StatusSimulado;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SimuladoDAO {

    public void save(Simulado simulado) {
        String sqlSimulado = "INSERT INTO simulados (titulo, data_criacao, status, professor_criador_id) VALUES (?, ?, ?, ?)";
        String sqlSimuladoQuestoes = "INSERT INTO simulado_questoes (simulado_id, questao_id) VALUES (?, ?)";
        String sqlSimuladoTurmas = "INSERT INTO simulado_turmas (simulado_id, turma_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlSimulado, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, simulado.getTitulo());
                stmt.setString(2, simulado.getDataCriacao().toString());
                stmt.setString(3, simulado.getStatus().name());
                stmt.setString(4, simulado.getProfessorCriadorId());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    simulado.setId(rs.getInt(1));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlSimuladoQuestoes)) {
                for (Questao q : simulado.getQuestoes()) {
                    stmt.setInt(1, simulado.getId());
                    stmt.setInt(2, q.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlSimuladoTurmas)) {
                for (Turma t : simulado.getTurmas()) {
                    stmt.setInt(1, simulado.getId());
                    stmt.setString(2, t.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            System.out.println("Simulado salvo com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar o simulado. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }


    public void update(Simulado simulado) {
        String sqlUpdateSimulado = "UPDATE simulados SET titulo = ?, status = ? WHERE id = ?";
        String sqlDeleteQuestoes = "DELETE FROM simulado_questoes WHERE simulado_id = ?";
        String sqlInsertQuestoes = "INSERT INTO simulado_questoes (simulado_id, questao_id) VALUES (?, ?)";
        String sqlDeleteTurmas = "DELETE FROM simulado_turmas WHERE simulado_id = ?";
        String sqlInsertTurmas = "INSERT INTO simulado_turmas (simulado_id, turma_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateSimulado)) {
                stmt.setString(1, simulado.getTitulo());
                stmt.setString(2, simulado.getStatus().name());
                stmt.setInt(3, simulado.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteQuestoes)) {
                stmt.setInt(1, simulado.getId());
                stmt.executeUpdate();
            }
            if (!simulado.getQuestoes().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertQuestoes)) {
                    for (Questao q : simulado.getQuestoes()) {
                        stmt.setInt(1, simulado.getId());
                        stmt.setInt(2, q.getId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteTurmas)) {
                stmt.setInt(1, simulado.getId());
                stmt.executeUpdate();
            }
            if (!simulado.getTurmas().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertTurmas)) {
                    for (Turma t : simulado.getTurmas()) {
                        stmt.setInt(1, simulado.getId());
                        stmt.setString(2, t.getId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit();
            System.out.println("Simulado ID " + simulado.getId() + " atualizado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar o simulado. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public List<Simulado> findByProfessorId(String professorId) {
        String sql = "SELECT * FROM simulados WHERE professor_criador_id = ? ORDER BY data_criacao DESC";
        List<Simulado> simulados = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, professorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Simulado s = new Simulado();
                s.setId(rs.getInt("id"));
                s.setTitulo(rs.getString("titulo"));
                s.setDataCriacao(LocalDate.parse(rs.getString("data_criacao")));
                s.setStatus(StatusSimulado.valueOf(rs.getString("status")));
                s.setProfessorCriadorId(rs.getString("professor_criador_id"));
                simulados.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar simulados por professor.");
            e.printStackTrace();
        }
        return simulados;
    }

    public List<Simulado> findSimuladosByTurmaId(String turmaId) {
        String sql = "SELECT s.* FROM simulados s " +
                "INNER JOIN simulado_turmas st ON s.id = st.simulado_id " +
                "WHERE st.turma_id = ? " +
                "ORDER BY s.data_criacao DESC";
        List<Simulado> simulados = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, turmaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Simulado s = new Simulado();
                s.setId(rs.getInt("id"));
                s.setTitulo(rs.getString("titulo"));
                s.setDataCriacao(LocalDate.parse(rs.getString("data_criacao")));
                s.setStatus(StatusSimulado.valueOf(rs.getString("status")));
                s.setProfessorCriadorId(rs.getString("professor_criador_id"));
                simulados.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar simulados por turma.");
            e.printStackTrace();
        }
        return simulados;
    }

    public void delete(int simuladoId) {
        String sql = "DELETE FROM simulados WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, simuladoId);
            stmt.executeUpdate();
            System.out.println("Simulado ID " + simuladoId + " deletado com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao deletar o simulado.");
            e.printStackTrace();
        }
    }

    public int countQuestoesBySimuladoId(int simuladoId) {
        String sql = "SELECT COUNT(*) FROM simulado_questoes WHERE simulado_id = ?";
        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, simuladoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar questões do simulado.");
            e.printStackTrace();
        }
        return count;
    }

    public int countByTurmaId(String turmaId) {
        String sql = "SELECT COUNT(*) FROM simulado_turmas WHERE turma_id = ?";
        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turmaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar simulados por turma.");
            e.printStackTrace();
        }
        return count;
    }

    public int countRealizadosByAlunoId(String alunoId) {
        String sql = "SELECT COUNT(DISTINCT simulado_id) FROM aluno_respostas WHERE aluno_id = ?";
        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, alunoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar simulados realizados pelo aluno.");
            e.printStackTrace();
        }
        return count;
    }

    public List<Integer> findQuestaoIdsBySimuladoId(int simuladoId) {
        String sql = "SELECT questao_id FROM simulado_questoes WHERE simulado_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, simuladoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("questao_id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs das questões por Simulado.");
            e.printStackTrace();
        }
        return ids;
    }
}