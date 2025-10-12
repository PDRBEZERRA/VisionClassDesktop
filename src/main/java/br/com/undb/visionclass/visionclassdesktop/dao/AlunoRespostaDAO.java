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

    // MÉTODO EXISTENTE: Salva a nota da correção manual
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

    // NOVO MÉTODO: Para encontrar a nota atribuída em questões discursivas
    public Double findNotaAtribuida(String alunoId, int simuladoId, int questaoId) {
        String sql = "SELECT nota_atribuida FROM aluno_respostas WHERE aluno_id = ? AND simulado_id = ? AND questao_id = ?";
        Double nota = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alunoId);
            stmt.setInt(2, simuladoId);
            stmt.setInt(3, questaoId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double result = rs.getDouble("nota_atribuida");
                if (!rs.wasNull()) {
                    nota = result;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar nota atribuída.");
            e.printStackTrace();
        }
        return nota;
    }


    /**
     * Calcula a nota final de um aluno em um simulado, somando os pontos obtidos
     * em cada questão (baseado na notaPontuacao).
     *
     * @param alunoId O ID do aluno.
     * @param simuladoId O ID do simulado.
     * @return A nota absoluta (soma dos pontos), -1 se não houver respostas, ou -2 se houver correção manual pendente.
     */
    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId) {

        // Query para somar os pontos obtidos:
        String sqlSomaObtida = "SELECT " +
                // 1. Multipla Escolha (MC): Soma a nota_pontuacao da questao (q) se a alternativa selecionada (a) estiver correta
                "  CAST(SUM(CASE WHEN ar.alternativa_selecionada_id IS NOT NULL AND a.correta = 1 THEN q.nota_pontuacao ELSE 0 END) AS REAL) as obtido_mc, " +
                // 2. Discursiva: Soma a nota_atribuida (que é o ponto concedido pelo professor)
                "  CAST(SUM(CASE WHEN ar.resposta_discursiva IS NOT NULL THEN ar.nota_atribuida ELSE 0 END) AS REAL) as obtido_discursiva " +
                "FROM aluno_respostas ar " +
                "JOIN questoes q ON ar.questao_id = q.id " +
                "LEFT JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " +
                "WHERE ar.aluno_id = ? AND ar.simulado_id = ?";

        double notaObtidaTotal = -1;
        boolean hasResult = false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtSoma = conn.prepareStatement(sqlSomaObtida)) {

            stmtSoma.setString(1, alunoId);
            stmtSoma.setInt(2, simuladoId);
            ResultSet rs = stmtSoma.executeQuery();

            if (rs.next()) {
                double obtidoMc = rs.getDouble("obtido_mc");
                double obtidoDiscursiva = rs.getDouble("obtido_discursiva");

                if (!rs.wasNull()) { // Se pelo menos uma das somas tem valor (i.e., há respostas)
                    notaObtidaTotal = obtidoMc + obtidoDiscursiva;
                    hasResult = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular a nota do simulado para o aluno.");
            e.printStackTrace();
            return -1;
        }

        if (hasResult) {
            // VERIFICAÇÃO CRÍTICA: Contar questões discursivas que o aluno respondeu e que não foram corrigidas
            String sqlDiscursivasNaoCorrigidas = "SELECT COUNT(ar.id) FROM aluno_respostas ar " +
                    "JOIN questoes q ON ar.questao_id = q.id " +
                    "WHERE ar.aluno_id = ? AND ar.simulado_id = ? AND q.tipo = 'DISCURSIVA' AND ar.nota_atribuida IS NULL AND ar.resposta_discursiva IS NOT NULL";

            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmtCount = conn.prepareStatement(sqlDiscursivasNaoCorrigidas)) {
                stmtCount.setString(1, alunoId);
                stmtCount.setInt(2, simuladoId);
                ResultSet rs = stmtCount.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return -2; // Retorna código de erro: Aguardando correção manual
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar correção manual pendente.");
            }

            // O retorno é a nota absoluta (soma dos pontos), se não houver pendências
            return notaObtidaTotal;
        }

        return -1; // Sem respostas (notaObtidaTotal continua -1)
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

    /**
     * Calcula a média geral de um aluno em todos os simulados que ele realizou.
     * A nota do simulado (soma dos pontos) é normalizada para a escala de 0 a 10.
     * @param alunoId O ID do aluno.
     * @return A média geral (0 a 10), ou -1 se não houver simulados realizados.
     */
    public double getMediaGeralSimulados(String alunoId) {
        List<Integer> simuladosRealizadosIds = findSimuladosRealizadosIdsByAluno(alunoId);
        QuestaoDAO questaoDAO = new QuestaoDAO();

        if (simuladosRealizadosIds.isEmpty()) {
            return -1;
        }

        double somaDasNotasFinal = 0;
        int countValidos = 0;

        for (Integer simuladoId : simuladosRealizadosIds) {
            double notaAbsoluta = getNotaByAlunoAndSimulado(alunoId, simuladoId);

            // Ignora simulados sem respostas (-1) ou aguardando correção (-2)
            if (notaAbsoluta >= 0) {
                double maxPontos = questaoDAO.getTotalPontuacaoBySimuladoId(simuladoId);

                if (maxPontos > 0) {
                    // Normaliza a nota para a escala de 0 a 10 para o cálculo da média
                    double notaFinal = (notaAbsoluta / maxPontos) * 10.0;
                    somaDasNotasFinal += notaFinal;
                    countValidos++;
                }
            }
        }

        if (countValidos > 0) {
            return somaDasNotasFinal / countValidos;
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