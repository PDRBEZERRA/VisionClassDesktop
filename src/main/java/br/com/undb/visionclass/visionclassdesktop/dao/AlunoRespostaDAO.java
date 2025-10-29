package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate; // Import necessário
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import necessário
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

            if (alternativaSelecionadaId != null) {
                stmt.setInt(4, alternativaSelecionadaId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (respostaDiscursiva != null && !respostaDiscursiva.trim().isEmpty()) {
                stmt.setString(5, respostaDiscursiva);
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            // Formata LocalDateTime para String no formato ISO (compatível com SQLite)
            stmt.setString(6, dataResposta.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));


            stmt.executeUpdate();
            System.out.println("Resposta salva para Questão ID: " + questaoId);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a resposta do aluno.");
            e.printStackTrace();
        }
    }

    // Salva a nota da correção manual
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

    // Encontra a nota atribuída em questões discursivas
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
     * Calcula a nota final de um aluno em um simulado, somando os pontos obtidos.
     * Considera filtro de data se fornecido.
     * @param alunoId O ID do aluno.
     * @param simuladoId O ID do simulado.
     * @param dataInicio Data inicial (pode ser null).
     * @param dataFim Data final (pode ser null).
     * @return A nota absoluta (soma dos pontos), -1 se não houver respostas, ou -2 se houver correção pendente.
     */
    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId, LocalDate dataInicio, LocalDate dataFim) {
        // Query base para somar pontos obtidos
        StringBuilder sqlSomaObtida = new StringBuilder(
                "SELECT " +
                        "  CAST(SUM(CASE WHEN ar.alternativa_selecionada_id IS NOT NULL AND a.correta = 1 THEN q.nota_pontuacao ELSE 0 END) AS REAL) as obtido_mc, " +
                        "  CAST(SUM(CASE WHEN ar.resposta_discursiva IS NOT NULL THEN ar.nota_atribuida ELSE 0 END) AS REAL) as obtido_discursiva " +
                        "FROM aluno_respostas ar " +
                        "JOIN questoes q ON ar.questao_id = q.id " +
                        "LEFT JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " +
                        "WHERE ar.aluno_id = ? AND ar.simulado_id = ?"
        );
        List<Object> paramsSoma = new ArrayList<>();
        paramsSoma.add(alunoId);
        paramsSoma.add(simuladoId);

        // Adiciona filtros de data se presentes (comparando data_resposta)
        if (dataInicio != null) {
            sqlSomaObtida.append(" AND date(ar.data_resposta) >= date(?)");
            paramsSoma.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sqlSomaObtida.append(" AND date(ar.data_resposta) <= date(?)");
            paramsSoma.add(dataFim.toString());
        }

        double notaObtidaTotal = -1;
        boolean hasResult = false;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtSoma = conn.prepareStatement(sqlSomaObtida.toString())) {

            // Seta os parâmetros (alunoId, simuladoId, datas)
            for (int i = 0; i < paramsSoma.size(); i++) {
                stmtSoma.setObject(i + 1, paramsSoma.get(i));
            }

            ResultSet rs = stmtSoma.executeQuery();
            if (rs.next()) {
                double obtidoMc = rs.getDouble("obtido_mc");
                double obtidoDiscursiva = rs.getDouble("obtido_discursiva");
                if (!rs.wasNull()) {
                    notaObtidaTotal = obtidoMc + obtidoDiscursiva;
                    hasResult = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular a nota do simulado para o aluno com filtro de data.");
            e.printStackTrace();
            return -1;
        }

        if (hasResult) {
            // Verifica pendências de correção DENTRO DO PERÍODO
            StringBuilder sqlDiscursivasNaoCorrigidas = new StringBuilder(
                    "SELECT COUNT(ar.id) FROM aluno_respostas ar " +
                            "JOIN questoes q ON ar.questao_id = q.id " +
                            "WHERE ar.aluno_id = ? AND ar.simulado_id = ? AND q.tipo = 'DISCURSIVA' AND ar.nota_atribuida IS NULL AND ar.resposta_discursiva IS NOT NULL"
            );
            List<Object> paramsPendente = new ArrayList<>();
            paramsPendente.add(alunoId);
            paramsPendente.add(simuladoId);

            if (dataInicio != null) {
                sqlDiscursivasNaoCorrigidas.append(" AND date(ar.data_resposta) >= date(?)");
                paramsPendente.add(dataInicio.toString());
            }
            if (dataFim != null) {
                sqlDiscursivasNaoCorrigidas.append(" AND date(ar.data_resposta) <= date(?)");
                paramsPendente.add(dataFim.toString());
            }


            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmtCount = conn.prepareStatement(sqlDiscursivasNaoCorrigidas.toString())) {

                for (int i = 0; i < paramsPendente.size(); i++) {
                    stmtCount.setObject(i + 1, paramsPendente.get(i));
                }

                ResultSet rs = stmtCount.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return -2; // Aguardando correção manual
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar correção manual pendente com filtro de data.");
                e.printStackTrace(); // Continua, mas pode levar a nota incorreta se houver erro
            }
            return notaObtidaTotal; // Nota calculada
        }

        return -1; // Sem respostas no período
    }

    // Sobrecarga para manter compatibilidade onde não se usa filtro de data
    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId) {
        return getNotaByAlunoAndSimulado(alunoId, simuladoId, null, null);
    }


    /**
     * Conta o número total de simulados distintos realizados pelo grupo de alunos (sem filtro de data).
     * @param alunosIds Lista de IDs dos alunos no escopo.
     * @return O número total de simulados realizados pelo grupo.
     */
    public int countSimuladosRealizadosByAlunosIds(List<String> alunosIds) {
        return countSimuladosRealizadosByAlunosIdsAndDateRange(alunosIds, null, null);
    }

    /**
     * NOVO MÉTODO COM FILTRO DE DATA: Conta o número total de simulados distintos
     * realizados por um grupo de alunos dentro de um intervalo de datas.
     * @param alunosIds Lista de IDs dos alunos no escopo.
     * @param dataInicio Data inicial (inclusiva, baseada na data_resposta). Pode ser null.
     * @param dataFim Data final (inclusiva, baseada na data_resposta). Pode ser null.
     * @return O número total de simulados distintos realizados no período.
     */
    public int countSimuladosRealizadosByAlunosIdsAndDateRange(List<String> alunosIds, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return 0;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(alunosIds.size(), "?"));
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT simulado_id) FROM aluno_respostas WHERE aluno_id IN (").append(placeholders).append(")");
        List<Object> params = new ArrayList<>(alunosIds);

        if (dataInicio != null) {
            sql.append(" AND date(data_resposta) >= date(?)");
            params.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sql.append(" AND date(data_resposta) <= date(?)");
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
            System.err.println("Erro ao contar simulados realizados por grupo e data.");
            e.printStackTrace();
        }
        return count;
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
     * Calcula a média geral (0-10) de um aluno em todos os simulados realizados (sem filtro de data).
     * @param alunoId O ID do aluno.
     * @return A média geral (0 a 10), ou -1 se não houver simulados realizados/válidos.
     */
    public double getMediaGeralSimulados(String alunoId) {
        return getAggregateSimuladosMediaByDateRange(List.of(alunoId), null, null); // Chama a versão agregada sem data
    }


    /**
     * NOVO MÉTODO COM FILTRO DE DATA: Calcula a média geral agregada (0-10)
     * dos simulados realizados por um grupo de alunos dentro de um intervalo de datas.
     * @param alunosIds Lista de IDs dos alunos.
     * @param dataInicio Data inicial. Pode ser null.
     * @param dataFim Data final. Pode ser null.
     * @return A média geral agregada (0 a 10), ou -1.0 se não houver simulados válidos no período.
     */
    public double getAggregateSimuladosMediaByDateRange(List<String> alunosIds, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return -1.0;
        }

        QuestaoDAO questaoDAO = new QuestaoDAO(); // Necessário para obter pontuação máxima
        double somaDasMediasIndividuaisNormalizadas = 0;
        int countAlunosComMediaValida = 0;

        // Itera por cada aluno no escopo
        for (String alunoId : alunosIds) {
            // Busca IDs dos simulados feitos PELO ALUNO ESPECÍFICO (respeitando datas se houver)
            List<Integer> simuladosDoAlunoNoPeriodo = findSimuladosRealizadosIdsByAlunoAndDateRange(alunoId, dataInicio, dataFim);

            if (simuladosDoAlunoNoPeriodo.isEmpty()) {
                continue; // Próximo aluno se este não fez simulados no período
            }

            double somaNotasFinaisAluno = 0;
            int countSimuladosValidosAluno = 0;

            // Calcula a média 0-10 para este aluno nos simulados do período
            for (Integer simuladoId : simuladosDoAlunoNoPeriodo) {
                // Usa a versão do getNota que já considera as datas
                double notaAbsoluta = getNotaByAlunoAndSimulado(alunoId, simuladoId, dataInicio, dataFim);

                if (notaAbsoluta >= 0) { // Ignora -1 (sem resposta) e -2 (pendente)
                    double maxPontos = questaoDAO.getTotalPontuacaoBySimuladoId(simuladoId); // Pontuação máxima do simulado
                    if (maxPontos > 0) {
                        double notaFinalNormalizada = (notaAbsoluta / maxPontos) * 10.0; // Normaliza para 0-10
                        somaNotasFinaisAluno += notaFinalNormalizada;
                        countSimuladosValidosAluno++;
                    }
                }
            }

            // Se o aluno teve pelo menos um simulado válido no período, calcula sua média e adiciona à soma geral
            if (countSimuladosValidosAluno > 0) {
                double mediaAluno = somaNotasFinaisAluno / countSimuladosValidosAluno;
                somaDasMediasIndividuaisNormalizadas += mediaAluno;
                countAlunosComMediaValida++;
            }
        }

        // Calcula a média final agregada
        if (countAlunosComMediaValida > 0) {
            return somaDasMediasIndividuaisNormalizadas / countAlunosComMediaValida;
        }

        return -1.0; // Nenhum aluno teve média válida no período
    }

    /**
     * NOVO MÉTODO AUXILIAR: Encontra IDs de simulados realizados por um aluno específico
     * dentro de um intervalo de datas.
     */
    private List<Integer> findSimuladosRealizadosIdsByAlunoAndDateRange(String alunoId, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT simulado_id FROM aluno_respostas WHERE aluno_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(alunoId);

        if (dataInicio != null) {
            sql.append(" AND date(data_resposta) >= date(?)");
            params.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sql.append(" AND date(data_resposta) <= date(?)");
            params.add(dataFim.toString());
        }

        List<Integer> ids = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("simulado_id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de simulados realizados por aluno e data.");
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Calcula a média geral (0-10) agregada para um grupo de alunos (sem filtro de data).
     * @param alunosIds Lista de IDs dos alunos.
     * @return A média geral agregada (0 a 10), ou -1.0 se não houver dados válidos.
     */
    public double getMediaGeralSimulados(List<String> alunosIds) {
        return getAggregateSimuladosMediaByDateRange(alunosIds, null, null);
    }


    // Encontra IDs dos alunos que responderam a um simulado
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

    // Encontra as respostas discursivas de um aluno para um simulado
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

    // Conta quantas submissões (alunos distintos) um simulado teve
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

    /**
     * Calcula a média percentual de acertos em questões de múltipla escolha
     * para um grupo de alunos (sem filtro de data).
     * @param alunosIds Lista de IDs dos alunos no escopo.
     * @return A média percentual de acertos (0.0 a 100.0), ou -1.0 se não houver respostas válidas.
     */
    public double calculateMediaAcertosMC(List<String> alunosIds) {
        return calculateMediaAcertosMCByDateRange(alunosIds, null, null);
    }


    /**
     * NOVO MÉTODO COM FILTRO DE DATA: Calcula a média percentual de acertos em questões
     * de múltipla escolha para um grupo de alunos dentro de um intervalo de datas.
     * @param alunosIds Lista de IDs dos alunos no escopo.
     * @param dataInicio Data inicial (inclusiva, baseada na data_resposta). Pode ser null.
     * @param dataFim Data final (inclusiva, baseada na data_resposta). Pode ser null.
     * @return A média percentual de acertos (0.0 a 100.0), ou -1.0 se não houver respostas válidas no período.
     */
    public double calculateMediaAcertosMCByDateRange(List<String> alunosIds, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return -1.0;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(alunosIds.size(), "?"));
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  COUNT(ar.id) as total_mc_respostas, " +
                        "  SUM(CASE WHEN a.correta = 1 THEN 1 ELSE 0 END) as total_acertos_mc " +
                        "FROM aluno_respostas ar " +
                        "JOIN questoes q ON ar.questao_id = q.id " +
                        "JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " + // INNER JOIN aqui garante que só pegamos respostas com alternativa selecionada
                        "WHERE ar.aluno_id IN (" + placeholders + ") AND q.tipo = 'MULTIPLA_ESCOLHA'"
        );
        List<Object> params = new ArrayList<>(alunosIds);

        // Adiciona filtros de data se presentes
        if (dataInicio != null) {
            sql.append(" AND date(ar.data_resposta) >= date(?)");
            params.add(dataInicio.toString());
        }
        if (dataFim != null) {
            sql.append(" AND date(ar.data_resposta) <= date(?)");
            params.add(dataFim.toString());
        }

        int totalRespostasMC = 0;
        int totalAcertosMC = 0;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalRespostasMC = rs.getInt("total_mc_respostas");
                totalAcertosMC = rs.getInt("total_acertos_mc");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular média de acertos MC por data.");
            e.printStackTrace();
            return -1.0;
        }

        if (totalRespostasMC > 0) {
            return ((double) totalAcertosMC / totalRespostasMC) * 100.0;
        } else {
            return -1.0; // Sem respostas MC válidas no período
        }
    }

}
