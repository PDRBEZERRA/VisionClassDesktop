package br.com.undb.visionclass.visionclassdesktop.database;

import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class ConnectionFactory {

    // 1. URL alterada para SQLite. O ficheiro visionclass.db será criado na raiz do projeto.
    private static final String DB_URL = "jdbc:sqlite:visionclass.db";

    // 2. Utilizador e senha REMOVIDOS, não são necessários para o SQLite.

    public static Connection getConnection() {
        try {
            // 3. Conexão simplificada, sem utilizador/senha.
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados SQLite.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Inicializa a base de dados: cria as tabelas se não existirem
     * e adiciona um utilizador administrador padrão se a tabela de utilizadores estiver vazia.
     */
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

        String countUsersSql = "SELECT COUNT(*) FROM users;";
        String insertAdminSql = "INSERT INTO users (id, nome, email, senha, matricula, role, cpf) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Garante que ambas as tabelas existem
            stmt.execute(createUserTableSql);
            stmt.execute(createTurmaTableSql);
            System.out.println("Tabelas 'users' e 'turmas' prontas.");

            // Verifica se a tabela de utilizadores está vazia e cria o admin se necessário
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