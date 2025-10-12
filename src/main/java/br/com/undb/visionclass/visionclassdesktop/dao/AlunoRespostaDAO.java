package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlunoRespostaDAO {

    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId) {
        String sql = "SELECT " +
                "  CAST(SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) AS REAL) as acertos, " +
                "  COUNT(ar.id) as total_respostas " +
                "FROM aluno_respostas ar " +
                "JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " +
                "WHERE ar.aluno_id = ? AND ar.simulado_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setInt(2, simuladoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int acertos = rs.getInt("acertos");
                int totalRespostas = rs.getInt("total_respostas");

                if (totalRespostas > 0) {
                    return ((double) acertos / totalRespostas) * 100.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular a nota do simulado para o aluno.");
            e.printStackTrace();
        }
        return -1;
    }

    public List<Integer> findSimuladosRealizadosIdsByAluno(String alunoId) {
        String sql = "SELECT DISTINCT simulado_id FROM aluno_respostas WHERE aluno_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, alunoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("simulado_id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de simulados realizados.");
            e.printStackTrace();
        }
        return ids;
    }

    // --- NOVO MÉTODO REUTILIZÁVEL ---
    /**
     * Calcula a média geral de um aluno em todos os simulados que ele realizou.
     * @param alunoId O ID do aluno.
     * @return A média geral (0 a 100), ou -1 se não houver simulados realizados.
     */
    public double getMediaGeralSimulados(String alunoId) {
        List<Integer> simuladosRealizadosIds = findSimuladosRealizadosIdsByAluno(alunoId);

        if (simuladosRealizadosIds.isEmpty()) {
            return -1; // Retorna -1 para indicar que o aluno não fez simulados
        }

        double somaDasNotas = 0;
        int countValidos = 0;

        for (Integer simuladoId : simuladosRealizadosIds) {
            double nota = getNotaByAlunoAndSimulado(alunoId, simuladoId);
            if (nota >= 0) {
                somaDasNotas += nota;
                countValidos++;
            }
        }

        if (countValidos > 0) {
            return somaDasNotas / countValidos;
        }

        return -1;
    }
}