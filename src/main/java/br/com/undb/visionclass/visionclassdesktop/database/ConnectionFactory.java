package br.com.undb.visionclass.visionclassdesktop.database;

import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class ConnectionFactory {

    // Seus dados de conexão (mantenha como estão)
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/visionclass";
    private static final String USER = "postgres";
    private static final String PASS = "AScb321$";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados PostgreSQL.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Inicializa a base de dados: cria a tabela de utilizadores se não existir
     * e adiciona um utilizador administrador padrão se a tabela estiver vazia.
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

        String countUsersSql = "SELECT COUNT(*) FROM users;";
        String insertAdminSql = "INSERT INTO users (id, nome, email, senha, matricula, role, cpf) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Garante que a tabela existe
            stmt.execute(createUserTableSql);
            System.out.println("Tabela 'users' pronta.");

            // 2. Verifica se a tabela está vazia
            ResultSet rs = stmt.executeQuery(countUsersSql);
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Tabela de utilizadores vazia. A criar utilizador admin padrão...");

                // 3. Se estiver vazia, insere o admin com senha encriptada
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAdminSql)) {
                    insertStmt.setString(1, UUID.randomUUID().toString());
                    insertStmt.setString(2, "Administrador");
                    insertStmt.setString(3, "admin@visionclass.com");
                    insertStmt.setString(4, PasswordUtil.hashPassword("admin123")); // Encripta a senha
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