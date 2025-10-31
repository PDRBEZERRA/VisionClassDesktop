package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlunoRespostaDAO {

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

            stmt.setString(6, dataResposta.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));


            stmt.executeUpdate();
            System.out.println("Resposta salva para Questão ID: " + questaoId);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a resposta do aluno.");
            e.printStackTrace();
        }
    }

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


    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId, LocalDate dataInicio, LocalDate dataFim) {
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
                    return -2;
                }
            } catch (SQLException e) {
                System.err.println("Erro ao verificar correção manual pendente com filtro de data.");
                e.printStackTrace();
            }
            return notaObtidaTotal;
        }

        return -1;
    }

    public double getNotaByAlunoAndSimulado(String alunoId, int simuladoId) {
        return getNotaByAlunoAndSimulado(alunoId, simuladoId, null, null);
    }


    public int countSimuladosRealizadosByAlunosIds(List<String> alunosIds) {
        return countSimuladosRealizadosByAlunosIdsAndDateRange(alunosIds, null, null);
    }


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

    public double getMediaGeralSimulados(String alunoId) {
        return getAggregateSimuladosMediaByDateRange(List.of(alunoId), null, null); // Chama a versão agregada sem data
    }



    public double getAggregateSimuladosMediaByDateRange(List<String> alunosIds, LocalDate dataInicio, LocalDate dataFim) {
        if (alunosIds == null || alunosIds.isEmpty()) {
            return -1.0;
        }

        QuestaoDAO questaoDAO = new QuestaoDAO(); // Necessário para obter pontuação máxima
        double somaDasMediasIndividuaisNormalizadas = 0;
        int countAlunosComMediaValida = 0;

        for (String alunoId : alunosIds) {
            List<Integer> simuladosDoAlunoNoPeriodo = findSimuladosRealizadosIdsByAlunoAndDateRange(alunoId, dataInicio, dataFim);

            if (simuladosDoAlunoNoPeriodo.isEmpty()) {
                continue;
            }

            double somaNotasFinaisAluno = 0;
            int countSimuladosValidosAluno = 0;

            for (Integer simuladoId : simuladosDoAlunoNoPeriodo) {
                double notaAbsoluta = getNotaByAlunoAndSimulado(alunoId, simuladoId, dataInicio, dataFim);

                if (notaAbsoluta >= 0) {
                    double maxPontos = questaoDAO.getTotalPontuacaoBySimuladoId(simuladoId);
                    if (maxPontos > 0) {
                        double notaFinalNormalizada = (notaAbsoluta / maxPontos) * 10.0;
                        somaNotasFinaisAluno += notaFinalNormalizada;
                        countSimuladosValidosAluno++;
                    }
                }
            }

            if (countSimuladosValidosAluno > 0) {
                double mediaAluno = somaNotasFinaisAluno / countSimuladosValidosAluno;
                somaDasMediasIndividuaisNormalizadas += mediaAluno;
                countAlunosComMediaValida++;
            }
        }

        if (countAlunosComMediaValida > 0) {
            return somaDasMediasIndividuaisNormalizadas / countAlunosComMediaValida;
        }

        return -1.0;
    }

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


    public double getMediaGeralSimulados(List<String> alunosIds) {
        return getAggregateSimuladosMediaByDateRange(alunosIds, null, null);
    }



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

    public double calculateMediaAcertosMC(List<String> alunosIds) {
        return calculateMediaAcertosMCByDateRange(alunosIds, null, null);
    }



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
                        "JOIN alternativas a ON ar.alternativa_selecionada_id = a.id " +
                        "WHERE ar.aluno_id IN (" + placeholders + ") AND q.tipo = 'MULTIPLA_ESCOLHA'"
        );
        List<Object> params = new ArrayList<>(alunosIds);

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
            return -1.0;
        }
    }

    public double getMediaGeralSimuladosPorLista(String alunoId, List<Integer> simuladosIds) {
        if (simuladosIds == null || simuladosIds.isEmpty()) {
            return -1.0;
        }

        QuestaoDAO questaoDAO = new QuestaoDAO();
        double somaNotasFinaisAluno = 0;
        int countSimuladosValidosAluno = 0;

        for (Integer simuladoId : simuladosIds) {
            double notaAbsoluta = getNotaByAlunoAndSimulado(alunoId, simuladoId);

            if (notaAbsoluta >= 0) {
                double maxPontos = questaoDAO.getTotalPontuacaoBySimuladoId(simuladoId);
                if (maxPontos > 0) {
                    double notaFinalNormalizada = (notaAbsoluta / maxPontos) * 10.0;
                    somaNotasFinaisAluno += notaFinalNormalizada;
                    countSimuladosValidosAluno++;
                }
            }
        }

        if (countSimuladosValidosAluno > 0) {
            return somaNotasFinaisAluno / countSimuladosValidosAluno;
        }

        return -1.0;
    }

}