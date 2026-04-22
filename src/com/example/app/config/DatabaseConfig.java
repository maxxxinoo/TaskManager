package com.example.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            createTables();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTables() {
        String createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER DEFAULT 1,
                title TEXT NOT NULL,
                description TEXT,
                is_completed INTEGER DEFAULT 0,
                status TEXT DEFAULT 'draft',
                priority TEXT DEFAULT 'medium',
                due_date TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTasksTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}