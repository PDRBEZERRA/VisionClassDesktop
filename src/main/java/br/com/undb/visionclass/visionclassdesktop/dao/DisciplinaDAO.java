package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    /**
     * Salva uma nova disciplina no banco de dados.
     * @param disciplina A disciplina a ser salva.
     * @return O objeto Disciplina salvo, com o ID gerado pelo banco.
     */
    public Disciplina save(Disciplina disciplina) {
        String sql = "INSERT INTO disciplinas (nome) VALUES (?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, disciplina.getNome());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                disciplina.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a disciplina.");
            e.printStackTrace();
        }
        return disciplina;
    }

    /**
     * Busca todas as disciplinas cadastradas, ordenadas por nome.
     * @return Uma lista de todas as disciplinas.
     */
    public List<Disciplina> findAll() {
        String sql = "SELECT * FROM disciplinas ORDER BY nome";
        List<Disciplina> disciplinas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Disciplina disciplina = new Disciplina();
                disciplina.setId(rs.getInt("id"));
                disciplina.setNome(rs.getString("nome"));
                disciplinas.add(disciplina);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as disciplinas.");
            e.printStackTrace();
        }
        return disciplinas;
    }
}