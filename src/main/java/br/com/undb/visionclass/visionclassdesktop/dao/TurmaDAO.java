package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.Turma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    /**
     * Busca todas as turmas registadas no banco de dados.
     */
    public List<Turma> findAll() {
        String sql = "SELECT * FROM turmas ORDER BY nome";
        List<Turma> turmas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Turma turma = new Turma();
                turma.setId(rs.getString("id"));
                turma.setNome(rs.getString("nome"));
                turma.setAno(rs.getString("ano"));
                turma.setPeriodo(rs.getString("periodo"));
                turma.setProfessorId(rs.getString("professorId"));
                turma.setDesempenho(rs.getDouble("desempenho"));
                turmas.add(turma);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as turmas.");
            e.printStackTrace();
        }
        return turmas;
    }

    /**
     * Guarda uma nova turma no banco de dados.
     */
    public void save(Turma turma) {
        String sql = "INSERT INTO turmas (id, nome, ano, periodo, professorId, desempenho) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, turma.getId());
            stmt.setString(2, turma.getNome());
            stmt.setString(3, turma.getAno());
            stmt.setString(4, turma.getPeriodo());
            stmt.setString(5, turma.getProfessorId());
            stmt.setDouble(6, turma.getDesempenho());

            stmt.executeUpdate();
            System.out.println("Turma guardada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao guardar a turma.");
            e.printStackTrace();
        }
    }

    /**
     * Conta o número total de turmas no banco de dados.
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM turmas";
        int count = 0;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar as turmas.");
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Busca todas as turmas de um professor específico.
     */
    public List<Turma> findByProfessorId(String professorId) {
        String sql = "SELECT * FROM turmas WHERE professorId = ? ORDER BY nome";
        List<Turma> turmas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, professorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Turma turma = new Turma();
                turma.setId(rs.getString("id"));
                turma.setNome(rs.getString("nome"));
                turma.setAno(rs.getString("ano"));
                turma.setPeriodo(rs.getString("periodo"));
                turma.setProfessorId(rs.getString("professorId"));
                turma.setDesempenho(rs.getDouble("desempenho"));
                turmas.add(turma);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar turmas por professorId.");
            e.printStackTrace();
        }
        return turmas;
    }

    /**
     * Busca uma turma pelo seu ID.
     */
    public Turma findById(String turmaId) {
        String sql = "SELECT * FROM turmas WHERE id = ?";
        Turma turma = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turmaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                turma = new Turma();
                turma.setId(rs.getString("id"));
                turma.setNome(rs.getString("nome"));
                turma.setAno(rs.getString("ano"));
                turma.setPeriodo(rs.getString("periodo"));
                turma.setProfessorId(rs.getString("professorId"));
                turma.setDesempenho(rs.getDouble("desempenho"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return turma;
    }

    /**
     * Adiciona um aluno a uma turma na tabela de associação.
     */
    public void addAlunoToTurma(String turmaId, String alunoId) {
        String sql = "INSERT INTO turma_alunos (turma_id, aluno_id) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turmaId);
            stmt.setString(2, alunoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { // UNIQUE constraint failed
                System.out.println("Aluno já está matriculado nesta turma.");
            } else {
                System.err.println("Erro ao adicionar aluno à turma.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Conta o número de alunos em uma turma específica.
     */
    public int countAlunosByTurmaId(String turmaId) {
        String sql = "SELECT COUNT(*) FROM turma_alunos WHERE turma_id = ?";
        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turmaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar alunos na turma.");
            e.printStackTrace();
        }
        return count;
    }

    // --- NOVO MÉTODO PARA REMOVER ALUNO ---
    /**
     * Remove um aluno de uma turma na tabela de associação.
     */
    public void removeAlunoFromTurma(String turmaId, String alunoId) {
        String sql = "DELETE FROM turma_alunos WHERE turma_id = ? AND aluno_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, turmaId);
            stmt.setString(2, alunoId);
            stmt.executeUpdate();
            System.out.println("Aluno removido da turma com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao remover aluno da turma.");
            e.printStackTrace();
        }
    }
}