package br.com.undb.visionclass.visionclassdesktop.database;

import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class ConnectionFactory {

    private static final String DB_URL = "jdbc:sqlite:visionclass.db";

    public static Connection getConnection() {
        try {
            // Habilita o suporte a chaves estrangeiras, essencial para a integridade dos dados
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            return conn;
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

        // --- INÍCIO DAS NOVAS TABELAS PARA O BANCO DE QUESTÕES ---

        String createDisciplinasTableSql = "CREATE TABLE IF NOT EXISTS disciplinas ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome TEXT NOT NULL UNIQUE"
                + ");";

        String createAssuntosTableSql = "CREATE TABLE IF NOT EXISTS assuntos ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome TEXT NOT NULL,"
                + " disciplina_id INTEGER NOT NULL,"
                + " FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id) ON DELETE CASCADE"
                + ");";

        String createQuestoesTableSql = "CREATE TABLE IF NOT EXISTS questoes ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " enunciado TEXT NOT NULL,"
                + " tipo TEXT NOT NULL," // Ex: 'MULTIPLA_ESCOLHA', 'DISCURSIVA'
                + " nivel_dificuldade TEXT NOT NULL," // Ex: 'FACIL', 'MEDIA', 'DIFICIL'
                + " disciplina_id INTEGER NOT NULL,"
                + " assunto_id INTEGER NOT NULL,"
                + " professor_criador_id TEXT NOT NULL,"
                + " FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (assunto_id) REFERENCES assuntos(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (professor_criador_id) REFERENCES users(id)"
                + ");";

        String createAlternativasTableSql = "CREATE TABLE IF NOT EXISTS alternativas ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " texto TEXT NOT NULL,"
                + " correta BOOLEAN NOT NULL,"
                + " questao_id INTEGER NOT NULL,"
                + " FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE"
                + ");";

        // --- FIM DAS NOVAS TABELAS ---

        String countUsersSql = "SELECT COUNT(*) FROM users;";
        String insertAdminSql = "INSERT INTO users (id, nome, email, senha, matricula, role, cpf) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabelas existentes
            stmt.execute(createUserTableSql);
            stmt.execute(createTurmaTableSql);
            stmt.execute(createTurmaAlunosTableSql);
            stmt.execute(createAvaliacaoTableSql);

            // Executa a criação das novas tabelas
            stmt.execute(createDisciplinasTableSql);
            stmt.execute(createAssuntosTableSql);
            stmt.execute(createQuestoesTableSql);
            stmt.execute(createAlternativasTableSql);

            System.out.println("Tabelas do banco de dados prontas.");

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