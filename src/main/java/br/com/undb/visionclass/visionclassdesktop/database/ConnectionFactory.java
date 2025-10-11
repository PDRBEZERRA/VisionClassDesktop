package br.com.undb.visionclass.visionclassdesktop.database;

import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class ConnectionFactory {

    private static final String DB_URL = "jdbc:sqlite:visionclass.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados SQLite.");
            throw new RuntimeException(e);
        }
    }

    public static void initializeDatabase() {
        String createUserTableSql = "CREATE TABLE IF NOT EXISTS users ("
                + " id TEXT PRIMARY KEY,"
                + " nome TEXT NOT NULL,"
                + " email TEXT NOT NULL UNIQUE,"
                + " senha TEXT NOT NULL,"
                + " matricula TEXT NOT NULL,"
                + " role TEXT NOT NULL,"
                + " foto TEXT,"
                + " cpf TEXT"
                + ");";

        String createTurmaTableSql = "CREATE TABLE IF NOT EXISTS turmas ("
                + " id TEXT PRIMARY KEY,"
                + " nome TEXT NOT NULL,"
                + " ano TEXT,"
                + " periodo TEXT,"
                + " professorId TEXT REFERENCES users(id),"
                + " desempenho NUMERIC(5, 2)"
                + ");";

        String createTurmaAlunosTableSql = "CREATE TABLE IF NOT EXISTS turma_alunos ("
                + " turma_id TEXT NOT NULL,"
                + " aluno_id TEXT NOT NULL,"
                + " PRIMARY KEY (turma_id, aluno_id),"
                + " FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (aluno_id) REFERENCES users(id) ON DELETE CASCADE"
                + ");";

        // --- NOVA TABELA ADICIONADA AQUI ---
        String createAvaliacaoTableSql = "CREATE TABLE IF NOT EXISTS avaliacoes_comportamentais ("
                + " id TEXT PRIMARY KEY,"
                + " aluno_id TEXT NOT NULL,"
                + " professor_id TEXT NOT NULL,"
                + " turma_id TEXT NOT NULL,"
                + " data TEXT NOT NULL,"
                + " assiduidade INTEGER,"
                + " participacao INTEGER,"
                + " responsabilidade INTEGER,"
                + " sociabilidade INTEGER,"
                + " observacoes TEXT,"
                + " FOREIGN KEY (aluno_id) REFERENCES users(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (professor_id) REFERENCES users(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE"
                + ");";

        String countUsersSql = "SELECT COUNT(*) FROM users;";
        String insertAdminSql = "INSERT INTO users (id, nome, email, senha, matricula, role, cpf) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUserTableSql);
            stmt.execute(createTurmaTableSql);
            stmt.execute(createTurmaAlunosTableSql);
            stmt.execute(createAvaliacaoTableSql); // Executa a criação da nova tabela
            System.out.println("Tabelas prontas.");

            ResultSet rs = stmt.executeQuery(countUsersSql);
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Tabela de utilizadores vazia. A criar utilizador admin padrão...");
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAdminSql)) {
                    insertStmt.setString(1, UUID.randomUUID().toString());
                    insertStmt.setString(2, "Administrador");
                    insertStmt.setString(3, "admin@visionclass.com");
                    insertStmt.setString(4, PasswordUtil.hashPassword("admin123"));
                    insertStmt.setString(5, "00001");
                    insertStmt.setString(6, "ADMIN");
                    insertStmt.setString(7, "000.000.000-00");
                    insertStmt.executeUpdate();
                    System.out.println("Utilizador admin padrão criado com a senha 'admin123'.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar a base de dados.");
            e.printStackTrace();
        }
    }
}