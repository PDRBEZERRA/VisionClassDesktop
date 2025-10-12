package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.Assunto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AssuntoDAO {

    /**
     * Salva um novo assunto no banco de dados.
     * @param assunto O assunto a ser salvo.
     * @return O objeto Assunto salvo, com o ID gerado pelo banco.
     */
    public Assunto save(Assunto assunto) {
        String sql = "INSERT INTO assuntos (nome, disciplina_id) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, assunto.getNome());
            stmt.setInt(2, assunto.getDisciplinaId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                assunto.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar o assunto.");
            e.printStackTrace();
        }
        return assunto;
    }

    /**
     * Busca todos os assuntos de uma disciplina específica.
     * @param disciplinaId O ID da disciplina.
     * @return Uma lista de assuntos.
     */
    public List<Assunto> findByDisciplinaId(int disciplinaId) {
        String sql = "SELECT * FROM assuntos WHERE disciplina_id = ? ORDER BY nome";
        List<Assunto> assuntos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, disciplinaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assunto assunto = new Assunto();
                assunto.setId(rs.getInt("id"));
                assunto.setNome(rs.getString("nome"));
                assunto.setDisciplinaId(rs.getInt("disciplina_id"));
                assuntos.add(assunto);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar assuntos por disciplina.");
            e.printStackTrace();
        }
        return assuntos;
    }

    // --- NOVO MÉTODO PARA DELETAR ---
    /**
     * Deleta um assunto do banco de dados.
     * @param assuntoId O ID do assunto a ser deletado.
     */
    public void delete(int assuntoId) {
        String sql = "DELETE FROM assuntos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assuntoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao deletar o assunto.");
            e.printStackTrace();
        }
    }
}