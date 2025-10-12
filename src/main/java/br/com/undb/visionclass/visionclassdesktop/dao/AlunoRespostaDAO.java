package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlunoRespostaDAO {

    /**
     * Salva a resposta de uma única questão do simulado feita pelo aluno.
     */
    public void saveResposta(String alunoId, int simuladoId, int questaoId, Integer alternativaSelecionadaId, String respostaDiscursiva, LocalDateTime dataResposta) {
        String sql = "INSERT INTO aluno_respostas (aluno_id, simulado_id, questao_id, alternativa_selecionada_id, resposta_discursiva, data_resposta) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setInt(2, simuladoId);
            stmt.setInt(3, questaoId);

            // 4. Handle alternativa_selecionada_id (pode ser NULL para discursivas)
            if (alternativaSelecionadaId != null) {
                stmt.setInt(4, alternativaSelecionadaId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            // 5. Handle resposta_discursiva (pode ser NULL para múltipla escolha)
            if (respostaDiscursiva != null) {
                stmt.setString(5, respostaDiscursiva);
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setString(6, dataResposta.toString());

            stmt.executeUpdate();
            System.out.println("Resposta salva para Questão ID: " + questaoId);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a resposta do aluno.");
            e.printStackTrace();
        }
    }

    // NOVO MÉTODO: Adicionado para salvar a nota da correção manual
    public void updateCorrecaoDiscursiva(String alunoId, int simuladoId, int questaoId, double nota, String feedback) {
        String sql = "UPDATE aluno_respostas SET nota_atribuida = ?, feedback_professor = ? WHERE aluno_id = ? AND simulado_id = ? AND questao_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nota);
            stmt.setString(2, feedback);
            stmt.setString(3, alunoId);
            stmt.setInt(4, simuladoId);
            stmt.setInt(5, questaoId);

            stmt.executeUpdate();
            System.out.println("Correção manual salva para Questão ID: " + questaoId + ". Nota: " + nota);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a correção discursiva.");
            e.printStackTrace();
        }
    }


    /**
     * Calcula a nota final de um aluno em um simulado, combinando MC e discursivas corrigidas.
     */
    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId) {

        // 1. A query agora seleciona a soma total dos pontos (MC=1 ponto, Discursiva=nota/10)
        String sql = "SELECT " +
                // Se for MC (alternativa selecionada), soma 1 se correta, senão 0.
                "  CAST(SUM(CASE WHEN ar.alternativa_selecionada_id IS NOT NULL AND a.correta = 1 THEN 1 ELSE 0 END) AS REAL) as acertos_mc, " +
                // Se for discursiva, usa a nota_atribuida (normaliza para 0 a 1)
                "  CAST(SUM(CASE WHEN ar.resposta_discursiva IS NOT NULL THEN ar.nota_atribuida / 10.0 ELSE 0 END) AS REAL) as soma_notas_discursivas " +
                "FROM aluno_respostas ar " +
                // Usamos LEFT JOIN para incluir todas as respostas (MC e Discursivas)
                "LEFT JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " +
                "WHERE ar.aluno_id = ? AND ar.simulado_id = ?";

        // 2. Busca o número total de questões para normalizar a nota (o QuestaoDAO tem o método)
        int totalQuestoesSimulado = new QuestaoDAO().findQuestoesBySimuladoId(simuladoId).size();

        if (totalQuestoesSimulado == 0) return -1;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setInt(2, simuladoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double acertosMC = rs.getDouble("acertos_mc");
                double somaNotasDiscursivas = rs.getDouble("soma_notas_discursivas");

                double notaSomaTotal = acertosMC + somaNotasDiscursivas;

                // Normaliza para 100%
                return (notaSomaTotal / totalQuestoesSimulado) * 100.0;
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

    // Implementação de findAlunosIdsBySimuladoId e findDiscursiveAnswers...
    public List<String> findAlunosIdsBySimuladoId(int simuladoId) {
        String sql = "SELECT DISTINCT aluno_id FROM aluno_respostas WHERE simulado_id = ?";
        List<String> alunosIds = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, simuladoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                alunosIds.add(rs.getString("aluno_id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de alunos por Simulado.");
            e.printStackTrace();
        }
        return alunosIds;
    }

    public Map<Integer, String> findDiscursiveAnswers(String alunoId, int simuladoId) {
        String sql = "SELECT ar.questao_id, ar.resposta_discursiva FROM aluno_respostas ar " +
                "JOIN questoes q ON ar.questao_id = q.id " +
                "WHERE ar.aluno_id = ? AND ar.simulado_id = ? AND q.tipo = 'DISCURSIVA' AND ar.resposta_discursiva IS NOT NULL";

        Map<Integer, String> respostas = new HashMap<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, alunoId);
            stmt.setInt(2, simuladoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                respostas.put(rs.getInt("questao_id"), rs.getString("resposta_discursiva"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar respostas discursivas.");
            e.printStackTrace();
        }
        return respostas;
    }

    public int countSubmissoesBySimuladoId(int simuladoId) {
        String sql = "SELECT COUNT(DISTINCT aluno_id) FROM aluno_respostas WHERE simulado_id = ?";
        int count = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, simuladoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar submissões por simulado.");
            e.printStackTrace();
        }
        return count;
    }
}