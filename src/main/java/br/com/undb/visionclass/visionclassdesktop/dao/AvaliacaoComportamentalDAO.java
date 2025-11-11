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
                avaliacao = mapRowToAvaliacao(rs);
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
                historico.add(mapRowToAvaliacao(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar o histórico de avaliações.");
            e.printStackTrace();
        }
        return historico;
    }

    public int countByAlunosIds(List<String> alunosIds) {
        return countByAlunosIdsAndDateRange(alunosIds, null, null);
    }


    public int countByAlunosIdsAndDateRange(List<String> alunosIds, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return 0;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(alunosIds.size(), "?"));
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM avaliacoes_comportamentais WHERE aluno_id IN (").append(placeholders).append(")");
        List<Object> params = new ArrayList<>(alunosIds);

        if (dataInicio != null) {
            sql.append(" AND date(data) >= date(?)");
            params.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sql.append(" AND date(data) <= date(?)");
            params.add(dataFim.toString());
        }

        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar avaliações comportamentais por grupo e data.");
            e.printStackTrace();
        }
        return count;
    }



    public String getMediaPorDimensao(String alunoId, String dimensao) {
        double media = getAggregateMediaByDateRange(List.of(alunoId), dimensao, null, null);
        if (media >= 0) {
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(media);
        }
        return "-";
    }


    public String getMediaGeralByAlunoId(String alunoId) {
        double media = getAggregateMediaByDateRange(List.of(alunoId), "media_geral", null, null);
        if (media >= 0) {
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(media);
        }
        return "-";
    }
    public double getAggregateMediaByDateRange(List<String> alunosIds, String dimensao, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return -1.0;
        }

        String avgExpression;
        if ("media_geral".equals(dimensao)) {
            avgExpression = "(assiduidade + participacao + responsabilidade + sociabilidade) / 4.0";
        } else if (List.of("assiduidade", "participacao", "responsabilidade", "sociabilidade").contains(dimensao)) {
            avgExpression = dimensao;
        } else {
            System.err.println("Dimensão inválida para agregação: " + dimensao);
            return -1.0;
        }


        String placeholders = String.join(",", java.util.Collections.nCopies(alunosIds.size(), "?"));
        StringBuilder sql = new StringBuilder("SELECT AVG(").append(avgExpression).append(") as media FROM avaliacoes_comportamentais WHERE aluno_id IN (").append(placeholders).append(")");
        List<Object> params = new ArrayList<>(alunosIds);

        if (dataInicio != null) {
            sql.append(" AND date(data) >= date(?)");
            params.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sql.append(" AND date(data) <= date(?)");
            params.add(dataFim.toString());
        }

        double media = -1.0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                media = rs.getDouble("media");
                if (rs.wasNull()) {
                    media = -1.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular média agregada (" + dimensao + ") por data.");
            e.printStackTrace();
            media = -1.0;
        }
        return media;
    }


    private AvaliacaoComportamental mapRowToAvaliacao(ResultSet rs) throws SQLException {
        AvaliacaoComportamental avaliacao = new AvaliacaoComportamental();
        avaliacao.setId(rs.getString("id"));
        avaliacao.setAlunoId(rs.getString("aluno_id"));
        avaliacao.setProfessorId(rs.getString("professor_id"));
        avaliacao.setTurmaId(rs.getString("turma_id"));
        // Trata data como String e depois converte para LocalDate
        String dataStr = rs.getString("data");
        if (dataStr != null && !dataStr.isEmpty()) {
            try {
                avaliacao.setData(LocalDate.parse(dataStr));
            } catch (Exception e) {
                System.err.println("Erro ao parsear data: " + dataStr);
            }
        }
        avaliacao.setAssiduidade(rs.getInt("assiduidade"));
        avaliacao.setParticipacao(rs.getInt("participacao"));
        avaliacao.setResponsabilidade(rs.getInt("responsabilidade"));
        avaliacao.setSociabilidade(rs.getInt("sociabilidade"));
        avaliacao.setObservacoes(rs.getString("observacoes"));
        return avaliacao;
    }
}
