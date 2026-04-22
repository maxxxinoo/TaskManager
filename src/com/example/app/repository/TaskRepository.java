package com.example.app.repository;

import com.example.app.config.DatabaseConfig;
import com.example.app.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();

        String sql = "SELECT id, user_id, title, description, is_completed, status, priority, due_date, created_at, updated_at " +
                "FROM tasks WHERE user_id = ? ORDER BY is_completed ASC, created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setUserId(rs.getInt("user_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setCompleted(rs.getBoolean("is_completed"));
                task.setStatus(rs.getString("status") != null ? rs.getString("status") : "draft");
                task.setPriority(rs.getString("priority") != null ? rs.getString("priority") : "medium");

                String dueDateStr = rs.getString("due_date");
                if (dueDateStr != null) {
                    task.setDueDateTime(LocalDateTime.parse(dueDateStr));
                }

                if (rs.getTimestamp("created_at") != null) {
                    task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                if (rs.getTimestamp("updated_at") != null) {
                    task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }
                tasks.add(task);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public boolean updateStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, taskId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean save(Task task) {
        String sql = "INSERT INTO tasks(user_id, title, description, is_completed, status, priority, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setBoolean(4, task.isCompleted());
            stmt.setString(5, task.getStatus() != null ? task.getStatus() : "draft");
            stmt.setString(6, task.getPriority() != null ? task.getPriority() : "medium");
            stmt.setString(7, task.getDueDateTime() != null ? task.getDueDateTime().toString() : null);

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (Statement idStmt = conn.createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, is_completed = ?, status = ?, priority = ?, due_date = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setBoolean(3, task.isCompleted());
            stmt.setString(4, task.getStatus());
            stmt.setString(5, task.getPriority());
            stmt.setString(6, task.getDueDateTime() != null ? task.getDueDateTime().toString() : null);
            stmt.setInt(7, task.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePriority(int taskId, String priority) {
        String sql = "UPDATE tasks SET priority = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, priority);
            stmt.setInt(2, taskId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleCompleted(int id, boolean completed) {
        String sql = "UPDATE tasks SET is_completed = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, completed);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}