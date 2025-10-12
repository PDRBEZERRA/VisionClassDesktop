package br.com.undb.visionclass.visionclassdesktop.database;

import br.com.undb.visionclass.visionclassdesktop.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class ConnectionFactory {

    private static final String DB_URL = "jdbc:sqlite:visionclass.db";

    public static Connection getConnection() {
        try {
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
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // --- TABELAS EXISTENTES ---
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, nome TEXT NOT NULL, email TEXT NOT NULL UNIQUE, senha TEXT NOT NULL, matricula TEXT NOT NULL, role TEXT NOT NULL, foto TEXT, cpf TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS turmas (id TEXT PRIMARY KEY, nome TEXT NOT NULL, ano TEXT, periodo TEXT, professorId TEXT REFERENCES users(id), desempenho NUMERIC(5, 2));");
            stmt.execute("CREATE TABLE IF NOT EXISTS turma_alunos (turma_id TEXT NOT NULL, aluno_id TEXT NOT NULL, PRIMARY KEY (turma_id, aluno_id), FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE, FOREIGN KEY (aluno_id) REFERENCES users(id) ON DELETE CASCADE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS avaliacoes_comportamentais (id TEXT PRIMARY KEY, aluno_id TEXT NOT NULL, professor_id TEXT NOT NULL, turma_id TEXT NOT NULL, data TEXT NOT NULL, assiduidade INTEGER, participacao INTEGER, responsabilidade INTEGER, sociabilidade INTEGER, observacoes TEXT, FOREIGN KEY (aluno_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (professor_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS disciplinas (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS assuntos (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, disciplina_id INTEGER NOT NULL, FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id) ON DELETE CASCADE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS questoes (id INTEGER PRIMARY KEY AUTOINCREMENT, enunciado TEXT NOT NULL, tipo TEXT NOT NULL, nivel_dificuldade TEXT NOT NULL, disciplina_id INTEGER NOT NULL, assunto_id INTEGER NOT NULL, professor_criador_id TEXT NOT NULL, FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id) ON DELETE CASCADE, FOREIGN KEY (assunto_id) REFERENCES assuntos(id) ON DELETE CASCADE, FOREIGN KEY (professor_criador_id) REFERENCES users(id));");
            stmt.execute("CREATE TABLE IF NOT EXISTS alternativas (id INTEGER PRIMARY KEY AUTOINCREMENT, texto TEXT NOT NULL, correta BOOLEAN NOT NULL, questao_id INTEGER NOT NULL, FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE);");

            // --- INÍCIO DAS NOVAS TABELAS PARA SIMULADOS ---

            String createSimuladosTableSql = "CREATE TABLE IF NOT EXISTS simulados ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " titulo TEXT NOT NULL,"
                    + " data_criacao TEXT NOT NULL,"
                    + " status TEXT NOT NULL," // Ex: 'RASCUNHO', 'PUBLICADO', 'CONCLUIDO'
                    + " professor_criador_id TEXT NOT NULL,"
                    + " FOREIGN KEY (professor_criador_id) REFERENCES users(id)"
                    + ");";

            String createSimuladoQuestoesTableSql = "CREATE TABLE IF NOT EXISTS simulado_questoes ("
                    + " simulado_id INTEGER NOT NULL,"
                    + " questao_id INTEGER NOT NULL,"
                    + " PRIMARY KEY (simulado_id, questao_id),"
                    + " FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE"
                    + ");";

            String createSimuladoTurmasTableSql = "CREATE TABLE IF NOT EXISTS simulado_turmas ("
                    + " simulado_id INTEGER NOT NULL,"
                    + " turma_id TEXT NOT NULL,"
                    + " PRIMARY KEY (simulado_id, turma_id),"
                    + " FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE"
                    + ");";

            String createAlunoRespostasTableSql = "CREATE TABLE IF NOT EXISTS aluno_respostas ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " aluno_id TEXT NOT NULL,"
                    + " simulado_id INTEGER NOT NULL,"
                    + " questao_id INTEGER NOT NULL,"
                    + " alternativa_selecionada_id INTEGER," // Para múltipla escolha
                    + " resposta_discursiva TEXT," // Para discursivas
                    + " data_resposta TEXT NOT NULL,"
                    + " FOREIGN KEY (aluno_id) REFERENCES users(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (questao_id) REFERENCES questoes(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (alternativa_selecionada_id) REFERENCES alternativas(id)"
                    + ");";

            stmt.execute(createSimuladosTableSql);
            stmt.execute(createSimuladoQuestoesTableSql);
            stmt.execute(createSimuladoTurmasTableSql);
            stmt.execute(createAlunoRespostasTableSql);

            // --- FIM DAS NOVAS TABELAS ---

            System.out.println("Tabelas do banco de dados prontas.");

            // --- INSERÇÃO DE DADOS PADRÃO ---
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users;");
            if (rs.next() && rs.getInt(1) == 0) {
                // (código para inserir admin padrão)
            }

            rs = stmt.executeQuery("SELECT COUNT(*) FROM disciplinas;");
            if (rs.next() && rs.getInt(1) == 0) {
                // (código para inserir disciplinas/assuntos de exemplo)
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar a base de dados.");
            e.printStackTrace();
        }
    }
}