package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types; // Importação necessária para setNull
import java.time.LocalDateTime; // Importação necessária
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

    // --- NOVOS MÉTODOS PARA CORREÇÃO DO PROFESSOR ---

    /**
     * Busca os IDs dos alunos que responderam a um determinado simulado.
     * @param simuladoId O ID do simulado.
     * @return Uma lista de IDs de alunos (String).
     */
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

    /**
     * Busca as respostas discursivas de um aluno para um simulado.
     * Retorna apenas as questões discursivas que foram respondidas com texto.
     * @param alunoId O ID do aluno.
     * @param simuladoId O ID do simulado.
     * @return Um mapa onde a chave é o ID da Questão (Integer) e o valor é a resposta discursiva (String).
     */
    public Map<Integer, String> findDiscursiveAnswers(String alunoId, int simuladoId) {
        // A query assume que a tabela 'questoes' tem a coluna 'tipo' para filtrar por 'DISCURSIVA'
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

    // Dentro da classe AlunoRespostaDAO.java

    /**
     * Atualiza a nota de uma resposta discursiva e salva o feedback.
     * Requer que as colunas 'nota_atribuida' e 'feedback_professor' existam na tabela 'aluno_respostas'.
     */
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
            System.err.println("Erro ao salvar a correção discursiva. Verifique se as colunas 'nota_atribuida' e 'feedback_professor' existem no banco de dados.");
            e.printStackTrace();
        }
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