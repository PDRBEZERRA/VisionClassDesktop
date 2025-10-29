package br.com.undb.visionclass.visionclassdesktop.dao;

import br.com.undb.visionclass.visionclassdesktop.database.ConnectionFactory;
import br.com.undb.visionclass.visionclassdesktop.model.User;
import br.com.undb.visionclass.visionclassdesktop.model.UserRole;
import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByEmailAndPassword(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("senha");
                if (PasswordUtil.checkPassword(plainPassword, hashedPasswordFromDB)) {
                    user = new User();
                    user.setId(rs.getString("id"));
                    user.setNome(rs.getString("nome"));
                    user.setEmail(rs.getString("email"));
                    user.setMatricula(rs.getString("matricula"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    user.setCpf(rs.getString("cpf"));
                    user.setFoto(rs.getString("foto"));
                    user.setSenha(hashedPasswordFromDB);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar utilizador por email e senha.");
            e.printStackTrace();
        }
        return user;
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY nome";
        List<User> users = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                user.setMatricula(rs.getString("matricula"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setCpf(rs.getString("cpf"));
                user.setFoto(rs.getString("foto"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os utilizadores.");
            e.printStackTrace();
        }
        return users;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (id, nome, email, senha, matricula, role, cpf, foto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(user.getSenha());

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getNome());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, user.getMatricula());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getCpf());
            stmt.setString(8, user.getFoto());

            stmt.executeUpdate();
            System.out.println("Utilizador guardado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao guardar o utilizador.");
            e.printStackTrace();
        }
    }

    public void delete(String userId) {
        String sqlDeleteSimulados = "DELETE FROM simulados WHERE professor_criador_id = ?";
        String sqlDeleteQuestoes = "DELETE FROM questoes WHERE professor_criador_id = ?";
        String sqlDeleteAvaliacoes = "DELETE FROM avaliacoes_comportamentais WHERE professor_id = ?";
        String sqlUpdateTurmas = "UPDATE turmas SET professorId = NULL WHERE professorId = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE id = ?";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteSimulados)) {
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteQuestoes)) {
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAvaliacoes)) {
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateTurmas)) {
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteUser)) {
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Utilizador excluído com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao excluir o utilizador. Realizando rollback.");
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET nome = ?, email = ?, matricula = ?, role = ?, cpf = ?, foto = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getNome());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getMatricula());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getCpf());
            stmt.setString(6, user.getFoto());
            stmt.setString(7, user.getId());

            stmt.executeUpdate();
            System.out.println("Utilizador atualizado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar o utilizador.");
            e.printStackTrace();
        }
    }

    public void updatePassword(String userId, String hashedPassword) {
        String sql = "UPDATE users SET senha = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, userId);

            stmt.executeUpdate();
            System.out.println("Senha do utilizador " + userId + " atualizada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar a senha do utilizador.");
            e.printStackTrace();
        }
    }

    public int countByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        int count = 0;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao contar utilizadores por função: " + role.name());
            e.printStackTrace();
        }

        return count;
    }

    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY nome";
        List<User> users = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("nome"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar utilizadores por função: " + role.name());
            e.printStackTrace();
        }
        return users;
    }

    public User findById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                user.setMatricula(rs.getString("matricula"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setCpf(rs.getString("cpf"));
                user.setFoto(rs.getString("foto"));
                // CORREÇÃO PARA GARANTIR O HASH
                user.setSenha(rs.getString("senha"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar utilizador por ID.");
            e.printStackTrace();
        }
        return user;
    }

    public List<User> findAlunosNotInTurmaByName(String nome, String turmaId) {
        String sql = "SELECT * FROM users " +
                "WHERE role = 'ALUNO' AND nome LIKE ? AND id NOT IN (" +
                "  SELECT aluno_id FROM turma_alunos WHERE turma_id = ?" +
                ") ORDER BY nome";

        List<User> users = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            stmt.setString(2, turmaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("nome"));
                user.setMatricula(rs.getString("matricula"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alunos por nome fora da turma.");
            e.printStackTrace();
        }
        return users;
    }


    public List<User> findAlunosByTurmaId(String turmaId) {
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN turma_alunos ta ON u.id = ta.aluno_id " +
                "WHERE ta.turma_id = ? ORDER BY u.nome";

        List<User> alunos = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, turmaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("nome"));
                user.setMatricula(rs.getString("matricula"));
                user.setEmail(rs.getString("email"));
                alunos.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alunos da turma.");
            e.printStackTrace();
        }
        return alunos;
    }
}