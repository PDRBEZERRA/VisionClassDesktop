package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.AvaliacaoComportamental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoComportamentalDAO {

    public void save(AvaliacaoComportamental avaliacao) {
        String sql = "INSERT INTO avaliacoes_comportamentais (id, aluno_id, professor_id, turma_id, data, assiduidade, participacao, responsabilidade, sociabilidade, observacoes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, avaliacao.getId());
            stmt.setString(2, avaliacao.getAlunoId());
            stmt.setString(3, avaliacao.getProfessorId());
            stmt.setString(4, avaliacao.getTurmaId());
            stmt.setString(5, avaliacao.getData().toString());
            stmt.setInt(6, avaliacao.getAssiduidade());
            stmt.setInt(7, avaliacao.getParticipacao());
            stmt.setInt(8, avaliacao.getResponsabilidade());
            stmt.setInt(9, avaliacao.getSociabilidade());
            stmt.setString(10, avaliacao.getObservacoes());

            stmt.executeUpdate();
            System.out.println("Avaliação comportamental guardada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao guardar a avaliação comportamental.");
            e.printStackTrace();
        }
    }

    public AvaliacaoComportamental findLatestByAlunoAndTurma(String alunoId, String turmaId) {
        String sql = "SELECT * FROM avaliacoes_comportamentais WHERE aluno_id = ? AND turma_id = ? ORDER BY data DESC LIMIT 1";
        AvaliacaoComportamental avaliacao = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setString(2, turmaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                avaliacao = new AvaliacaoComportamental();
                avaliacao.setId(rs.getString("id"));
                avaliacao.setAlunoId(rs.getString("aluno_id"));
                avaliacao.setProfessorId(rs.getString("professor_id"));
                avaliacao.setTurmaId(rs.getString("turma_id"));
                avaliacao.setData(LocalDate.parse(rs.getString("data")));
                avaliacao.setAssiduidade(rs.getInt("assiduidade"));
                avaliacao.setParticipacao(rs.getInt("participacao"));
                avaliacao.setResponsabilidade(rs.getInt("responsabilidade"));
                avaliacao.setSociabilidade(rs.getInt("sociabilidade"));
                avaliacao.setObservacoes(rs.getString("observacoes"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar a última avaliação comportamental.");
            e.printStackTrace();
        }
        return avaliacao;
    }

    public void update(AvaliacaoComportamental avaliacao) {
        String sql = "UPDATE avaliacoes_comportamentais SET " +
                "data = ?, assiduidade = ?, participacao = ?, responsabilidade = ?, sociabilidade = ?, observacoes = ?, professor_id = ? " +
                "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, avaliacao.getData().toString());
            stmt.setInt(2, avaliacao.getAssiduidade());
            stmt.setInt(3, avaliacao.getParticipacao());
            stmt.setInt(4, avaliacao.getResponsabilidade());
            stmt.setInt(5, avaliacao.getSociabilidade());
            stmt.setString(6, avaliacao.getObservacoes());
            stmt.setString(7, avaliacao.getProfessorId());
            stmt.setString(8, avaliacao.getId());

            stmt.executeUpdate();
            System.out.println("Avaliação comportamental atualizada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar a avaliação comportamental.");
            e.printStackTrace();
        }
    }

    public List<AvaliacaoComportamental> findAllByAlunoAndTurma(String alunoId, String turmaId) {
        String sql = "SELECT * FROM avaliacoes_comportamentais WHERE aluno_id = ? AND turma_id = ? ORDER BY data DESC";
        List<AvaliacaoComportamental> historico = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setString(2, turmaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AvaliacaoComportamental avaliacao = new AvaliacaoComportamental();
                avaliacao.setId(rs.getString("id"));
                avaliacao.setAlunoId(rs.getString("aluno_id"));
                avaliacao.setProfessorId(rs.getString("professor_id"));
                avaliacao.setTurmaId(rs.getString("turma_id"));
                avaliacao.setData(LocalDate.parse(rs.getString("data")));
                avaliacao.setAssiduidade(rs.getInt("assiduidade"));
                avaliacao.setParticipacao(rs.getInt("participacao"));
                avaliacao.setResponsabilidade(rs.getInt("responsabilidade"));
                avaliacao.setSociabilidade(rs.getInt("sociabilidade"));
                avaliacao.setObservacoes(rs.getString("observacoes"));
                historico.add(avaliacao);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar o histórico de avaliações.");
            e.printStackTrace();
        }
        return historico;
    }

    // --- NOVO MÉTODO PARA O DASHBOARD DO ALUNO ---
    /**
     * Calcula a média geral comportamental de um aluno.
     * @param alunoId O ID do aluno.
     * @return A média formatada como uma String, ou "-" se não houver avaliações.
     */
    public String getMediaGeralByAlunoId(String alunoId) {
        // Esta query calcula a média de todas as notas de uma vez
        String sql = "SELECT AVG((assiduidade + participacao + responsabilidade + sociabilidade) / 4.0) as media_geral FROM avaliacoes_comportamentais WHERE aluno_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double media = rs.getDouble("media_geral");
                if (rs.wasNull()) { // Se não houver avaliações, a média será NULL
                    return "-";
                }
                DecimalFormat df = new DecimalFormat("#.#");
                return df.format(media);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular a média geral comportamental.");
            e.printStackTrace();
        }
        return "-";
    }
}