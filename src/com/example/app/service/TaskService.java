package com.example.app.service;

import com.example.app.model.Task;
import com.example.app.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskService {


    private final TaskRepository taskRepository = new TaskRepository();

    public List<Task> getUserTasks(int userId) {
        return taskRepository.findByUserId(userId);

    }

    public String addTask(int userId, String title, String description, LocalDateTime dueDateTime) {
        if (title == null || title.trim().isEmpty()) {
            return "Название задачи не может быть пустым";
        }

        Task task = new Task(title.trim(), description != null ? description.trim() : "");
        task.setUserId(userId);
        task.setDueDateTime(dueDateTime);

        return taskRepository.save(task) ? "success" : "Ошибка при сохранении задачи";
    }

    public boolean deleteTask(int taskId) {
        return taskRepository.delete(taskId);
    }

    public String updateTask(int taskId, String title, String description) {

        if (title == null || title.trim().isEmpty()) {
            return "Название задачи не может быть пустой";
        }
        Task task = new Task();
        task.setId(taskId);
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : "");

        return taskRepository.update(task) ? "success" : "Ошибка при обновлении задачи";
    }

    public boolean toggleTaskCompleted(int taskId, boolean completed) {
        return taskRepository.toggleCompleted(taskId, completed);
    }

    public boolean updateTaskStatus(int taskId, String status) {
        return taskRepository.updateStatus(taskId, status);
    }

    public boolean updateTaskPriority(int taskId, String priority) {
        return taskRepository.updatePriority(taskId, priority);
    }
}
