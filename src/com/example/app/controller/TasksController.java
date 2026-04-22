package com.example.app.controller;

import com.example.app.model.Task;
import com.example.app.service.TaskService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TasksController {

    @FXML private VBox tasksRoot;
    @FXML private TextField searchField;
    @FXML private ListView<HBox> draftListView;
    @FXML private ListView<HBox> inProgressListView;
    @FXML private ListView<HBox> doneListView;
    @FXML private Label draftCountLabel;
    @FXML private Label progressCountLabel;
    @FXML private Label doneCountLabel;
    @FXML private Button addTaskBtn;
    @FXML private Label titleLabel;

    private TaskService taskService;
    private int currentUserId;
    private List<Task> tasks;
    private String searchQuery = "";
    private boolean isDarkTheme = true;
    private boolean isEnglish = false;
    private int draftCount = 0;
    private int progressCount = 0;
    private int doneCount = 0;

    public void setUserId(int userId) {
        this.currentUserId = userId;
        taskService = new TaskService();
        setupDragAndDrop();
        loadTasks();
    }

    private void setupDragAndDrop() {
        setupListViewForDrag(draftListView);
        setupListViewForDrag(inProgressListView);
        setupListViewForDrag(doneListView);

        setupListViewForDrop(draftListView, "draft");
        setupListViewForDrop(inProgressListView, "in_progress");
        setupListViewForDrop(doneListView, "done");
    }

    private void setupListViewForDrag(ListView<HBox> listView) {
        listView.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                    for (Task task : tasks) {
                        VBox textBox = (VBox) item.getChildren().get(0);
                        if (textBox.getChildren().get(0) instanceof HBox) {
                            HBox titleBox = (HBox) textBox.getChildren().get(0);
                            if (titleBox.getChildren().get(1) instanceof Label) {
                                Label titleLabel = (Label) titleBox.getChildren().get(1);
                                if (titleLabel.getText().equals(task.getTitle())) {
                                    int taskId = task.getId();
                                    setOnDragDetected(event -> {
                                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                                        ClipboardContent content = new ClipboardContent();
                                        content.putString(String.valueOf(taskId));
                                        db.setContent(content);
                                        event.consume();
                                    });
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupListViewForDrop(ListView<HBox> listView, String targetStatus) {
        listView.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int taskId = Integer.parseInt(db.getString());
                taskService.updateTaskStatus(taskId, targetStatus);
                loadTasks();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void setTheme(boolean isDark) {
        this.isDarkTheme = isDark;
        refreshTheme();
    }

    private void refreshTheme() {
        if (isDarkTheme) {
            tasksRoot.setStyle("-fx-background-color: transparent; -fx-padding: 20;");
            if (searchField != null) {
                searchField.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-pref-height: 35; -fx-background-radius: 10;");
            }
            addTaskBtn.setStyle("-fx-font-weight: bold; -fx-pref-height: 35; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 10;");
        } else {
            tasksRoot.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");
            if (searchField != null) {
                searchField.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-prompt-text-fill: #999; -fx-pref-height: 35; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-radius: 10;");
            }
            addTaskBtn.setStyle("-fx-font-weight: bold; -fx-pref-height: 35; -fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 10;");
            // Добавь это для заголовков колонок
            draftCountLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 13;");
            progressCountLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-font-size: 13;");
            doneCountLabel.setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold; -fx-font-size: 13;");

            // Заголовок "МОИ ЗАДАЧИ"
            Node titleLabel = tasksRoot.lookup(".title");
            if (titleLabel instanceof Label) {
                ((Label) titleLabel).setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 20px;");
            }
        }

        updateColumnStyle(draftListView, isDarkTheme);
        updateColumnStyle(inProgressListView, isDarkTheme);
        updateColumnStyle(doneListView, isDarkTheme);
        loadTasks();
    }

    private void updateColumnStyle(ListView<HBox> listView, boolean isDark) {
        if (listView.getParent() instanceof VBox) {
            VBox column = (VBox) listView.getParent();
            if (isDark) {
                column.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-padding: 10;");
            } else {
                column.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10; -fx-padding: 10;");
            }
        }
        if (isDark) {
            listView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        } else {
            listView.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: transparent;");
        }
    }

    public void setLanguage(boolean isEnglish) {
        this.isEnglish = isEnglish;
        updateLanguage();
    }

    private void updateLanguage() {
        Platform.runLater(() -> {
            if (isEnglish) {
                titleLabel.setText("MY TASKS");
                searchField.setPromptText("🔍 Search tasks...");
                addTaskBtn.setText("+ NEW TASK");
                draftCountLabel.setText("📝 TO DO (" + draftCount + ")");
                progressCountLabel.setText("🔄 IN PROGRESS (" + progressCount + ")");
                doneCountLabel.setText("✅ DONE (" + doneCount + ")");
            } else {
                titleLabel.setText("МОИ ЗАДАЧИ");
                searchField.setPromptText("🔍 Поиск задач...");
                addTaskBtn.setText("+ НОВАЯ ЗАДАЧА");
                draftCountLabel.setText("📝 НУЖНО СДЕЛАТЬ (" + draftCount + ")");
                progressCountLabel.setText("🔄 В ПРОЦЕССЕ (" + progressCount + ")");
                doneCountLabel.setText("✅ ГОТОВО (" + doneCount + ")");
            }
        });
    }

    private void loadTasks() {
        tasks = taskService.getUserTasks(currentUserId);
        updateKanban();
    }

    private void updateKanban() {
        Platform.runLater(() -> {
            List<Task> filteredTasks = tasks;
            if (searchQuery != null && !searchQuery.isEmpty()) {
                filteredTasks = tasks.stream()
                        .filter(t -> t.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchQuery.toLowerCase())))
                        .collect(java.util.stream.Collectors.toList());
            }

            draftListView.getItems().clear();
            inProgressListView.getItems().clear();
            doneListView.getItems().clear();

            draftCount = 0;
            progressCount = 0;
            doneCount = 0;

            for (Task task : filteredTasks) {
                HBox taskRow = createTaskRow(task);
                switch (task.getStatus()) {
                    case "draft":
                        draftListView.getItems().add(taskRow);
                        draftCount++;
                        break;
                    case "in_progress":
                        inProgressListView.getItems().add(taskRow);
                        progressCount++;
                        break;
                    case "done":
                        doneListView.getItems().add(taskRow);
                        doneCount++;
                        break;
                }
            }

            updateLanguage();
        });
    }

    private HBox createTaskRow(Task task) {
        HBox row = new HBox(10);
        if (isDarkTheme) {
            row.setStyle("-fx-padding: 10; -fx-background-color: #2a2a2a; -fx-background-radius: 10;");
        } else {
            row.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");
        }
        row.setPrefWidth(260);
        row.setMaxWidth(260);

        VBox textBox = new VBox(5);
        textBox.setStyle("-fx-background-color: transparent;");

        Label priorityLabel = new Label();
        switch (task.getPriority()) {
            case "high":
                priorityLabel.setText("!!!");
                priorityLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-font-size: 12;");
                break;
            case "medium":
                priorityLabel.setText("!!");
                priorityLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold; -fx-font-size: 12;");
                break;
            case "low":
                priorityLabel.setText("!");
                priorityLabel.setStyle("-fx-text-fill: #00cc44; -fx-font-weight: bold; -fx-font-size: 12;");
                break;
            default:
                priorityLabel.setText("!!");
                priorityLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold; -fx-font-size: 12;");
        }

        Label titleLabel = new Label(task.getTitle());
        if (isDarkTheme) {
            titleLabel.setStyle(task.isCompleted() ? "-fx-text-fill: #666; -fx-font-size: 14; -fx-font-weight: bold;" : "-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle(task.isCompleted() ? "-fx-text-fill: #999; -fx-font-size: 14; -fx-font-weight: bold;" : "-fx-text-fill: #333; -fx-font-size: 14; -fx-font-weight: bold;");
        }
        titleLabel.setWrapText(true);

        HBox titleBox = new HBox(5);
        titleBox.getChildren().addAll(priorityLabel, titleLabel);

        Label descLabel = new Label(task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "Нет описания");
        if (isDarkTheme) {
            descLabel.setStyle(task.isCompleted() ? "-fx-text-fill: #444; -fx-font-size: 11;" : "-fx-text-fill: #888; -fx-font-size: 11;");
        } else {
            descLabel.setStyle(task.isCompleted() ? "-fx-text-fill: #ccc; -fx-font-size: 11;" : "-fx-text-fill: #666; -fx-font-size: 11;");
        }
        descLabel.setWrapText(true);

        Label dateLabel = new Label();
        if (task.getCreatedAt() != null) {
            dateLabel.setText("📅 " + task.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        } else {
            dateLabel.setText("📅 только что");
        }
        if (isDarkTheme) {
            dateLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
        } else {
            dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10");
        }

        if (task.getDueDateTime() != null) {
            Label dueLabel = new Label("⏰ " + task.getDueDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            dueLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 10;");
            textBox.getChildren().add(dueLabel);
        }

        textBox.getChildren().addAll(titleBox, descLabel, dateLabel);

        HBox actionBox = new HBox(5);
        actionBox.setStyle("-fx-padding: 5 0 0 0;");

        Button editBtn = new Button("✏️");
        if (isDarkTheme) {
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 11;");
        } else {
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 11;");
        }
        editBtn.setOnAction(e -> showEditDialog(task));

        Button deleteBtn = new Button("🗑");
        if (isDarkTheme) {
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 11;");
        } else {
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 11;");
        }
        deleteBtn.setOnAction(e -> showDeleteDialog(task));


        actionBox.getChildren().addAll(editBtn, deleteBtn);
        textBox.getChildren().add(actionBox);

        row.getChildren().add(textBox);
        row.setOnDragDetected(event -> {
            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId()));
            db.setContent(content);
            event.consume();
        });
        return row;
    }

    private String getStatusText(String status) {
        if (isEnglish) {
            switch (status) {
                case "draft": return "To Do";
                case "in_progress": return "In Progress";
                case "done": return "Done";
                default: return "To Do";
            }
        } else {
            switch (status) {
                case "draft": return "Нужно сделать";
                case "in_progress": return "В процессе";
                case "done": return "Готово";
                default: return "Нужно сделать";
            }
        }
    }

    private String getStatusCode(String statusText) {
        if (isEnglish) {
            switch (statusText) {
                case "To Do": return "draft";
                case "In Progress": return "in_progress";
                case "Done": return "done";
                default: return "draft";
            }
        } else {
            switch (statusText) {
                case "Нужно сделать": return "draft";
                case "В процессе": return "in_progress";
                case "Готово": return "done";
                default: return "draft";
            }
        }
    }

    private void showEditDialog(Task task) {
        Stage editStage = new Stage();
        editStage.initStyle(StageStyle.TRANSPARENT);
        Stage ownerStage = (Stage) draftListView.getScene().getWindow();
        editStage.initOwner(ownerStage);

        VBox dialogBox = new VBox(15);
        dialogBox.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            dialogBox.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);");
        } else {
            dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");
        }

        Label iconLabel = new Label("✏️");
        iconLabel.setStyle("-fx-font-size: 40;");

        Label titleLabel = new Label(isEnglish ? "Edit Task" : "Редактировать задачу");
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 18; -fx-font-weight: bold;");
        }

        TextField titleField = new TextField(task.getTitle());
        titleField.setPromptText(isEnglish ? "Task title" : "Название задачи");
        if (isDarkTheme) {
            titleField.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-pref-height: 35; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
        } else {
            titleField.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-pref-height: 35; -fx-prompt-text-fill: #999; -fx-border-color: #ccc; -fx-border-radius: 5;");
        }
        titleField.setPrefWidth(300);

        TextArea descArea = new TextArea(task.getDescription());
        descArea.setPromptText(isEnglish ? "Description (optional)" : "Описание (необязательно)");
        if (isDarkTheme) {
            descArea.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-control-inner-background: #181818; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
        } else {
            descArea.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-control-inner-background: #f5f5f5; -fx-prompt-text-fill: #999; -fx-border-color: #ccc; -fx-border-radius: 5;");
        }
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(300);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button(isEnglish ? "Save" : "Сохранить");
        if (isDarkTheme) {
            saveBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 25;");
        } else {
            saveBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 8; -fx-padding: 8 25;");
        }

        Button cancelBtn = new Button(isEnglish ? "Cancel" : "Отмена");
        if (isDarkTheme) {
            cancelBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #888; -fx-background-radius: 8; -fx-padding: 8 25;");
        } else {
            cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #666; -fx-background-radius: 8; -fx-padding: 8 25;");
        }

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        dialogBox.getChildren().addAll(iconLabel, titleLabel, titleField, descArea, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        editStage.setScene(scene);

        editStage.setOnShown(event -> {
            editStage.setX(ownerStage.getX() + (ownerStage.getWidth() - editStage.getWidth()) / 2);
            editStage.setY(ownerStage.getY() + (ownerStage.getHeight() - editStage.getHeight()) / 2);
        });

        saveBtn.setOnAction(e -> {
            taskService.updateTask(task.getId(), titleField.getText(), descArea.getText());
            loadTasks();
            editStage.close();
        });
        cancelBtn.setOnAction(e -> editStage.close());

        editStage.showAndWait();
    }

    private void showDeleteDialog(Task task) {
        Stage deleteStage = new Stage();
        deleteStage.initStyle(StageStyle.TRANSPARENT);
        Stage ownerStage = (Stage) draftListView.getScene().getWindow();
        deleteStage.initOwner(ownerStage);

        VBox dialogBox = new VBox(15);
        dialogBox.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            dialogBox.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);");
        } else {
            dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");
        }

        Label iconLabel = new Label("🗑");
        iconLabel.setStyle("-fx-font-size: 40;");

        Label titleLabel = new Label(isEnglish ? "Delete Task?" : "Удалить задачу?");
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 18; -fx-font-weight: bold;");
        }

        Label messageLabel = new Label(isEnglish ? "Are you sure you want to delete the task \"" + task.getTitle() + "\"?" : "Вы уверены, что хотите удалить задачу \"" + task.getTitle() + "\"?");
        if (isDarkTheme) {
            messageLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
        }
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button okBtn = new Button(isEnglish ? "Delete" : "Удалить");
        okBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 25; -fx-font-weight: bold;");
        okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 25; -fx-font-weight: bold;"));
        okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 25; -fx-font-weight: bold;"));

        Button cancelBtn = new Button(isEnglish ? "Cancel" : "Отмена");
        if (isDarkTheme) {
            cancelBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #888; -fx-background-radius: 8; -fx-padding: 8 25;");
        } else {
            cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #666; -fx-background-radius: 8; -fx-padding: 8 25;");
        }

        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        dialogBox.getChildren().addAll(iconLabel, titleLabel, messageLabel, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        deleteStage.setScene(scene);

        deleteStage.setOnShown(event -> {
            deleteStage.setX(ownerStage.getX() + (ownerStage.getWidth() - deleteStage.getWidth()) / 2);
            deleteStage.setY(ownerStage.getY() + (ownerStage.getHeight() - deleteStage.getHeight()) / 2);
        });

        okBtn.setOnAction(e -> {
            taskService.deleteTask(task.getId());
            loadTasks();
            deleteStage.close();
        });
        cancelBtn.setOnAction(e -> deleteStage.close());

        deleteStage.showAndWait();
    }

    @FXML
    public void handleAddTask() {
        Stage addStage = new Stage();
        addStage.initStyle(StageStyle.TRANSPARENT);
        Stage ownerStage = (Stage) draftListView.getScene().getWindow();
        addStage.initOwner(ownerStage);

        VBox dialogBox = new VBox(15);
        dialogBox.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            dialogBox.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);");
        } else {
            dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");
        }

        Label iconLabel = new Label("➕");
        iconLabel.setStyle("-fx-font-size: 40;");

        Label titleLabel = new Label(isEnglish ? "New Task" : "Новая задача");
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 18; -fx-font-weight: bold;");
        }

        TextField titleField = new TextField();
        titleField.setPromptText(isEnglish ? "Task title" : "Название задачи");
        if (isDarkTheme) {
            titleField.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-pref-height: 35; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
        } else {
            titleField.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-pref-height: 35; -fx-prompt-text-fill: #999; -fx-border-color: #ccc; -fx-border-radius: 5;");
        }
        titleField.setPrefWidth(300);

        TextArea descArea = new TextArea();
        descArea.setPromptText(isEnglish ? "Description (optional)" : "Описание (необязательно)");
        if (isDarkTheme) {
            descArea.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-control-inner-background: #181818; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
        } else {
            descArea.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-control-inner-background: #f5f5f5; -fx-prompt-text-fill: #999; -fx-border-color: #ccc; -fx-border-radius: 5;");
        }
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(300);

        // Дата
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(isEnglish ? "Due date" : "Дата дедлайна");
        if (isDarkTheme) {
            datePicker.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
            datePicker.getEditor().setStyle("-fx-background-color: #181818; -fx-text-fill: white;");
        } else {
            datePicker.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-prompt-text-fill: #999; -fx-border-color: #ccc; -fx-border-radius: 5;");
            datePicker.getEditor().setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333;");
        }
        datePicker.setPrefWidth(300);

        // Тёмная тема для календаря
        // Тёмная тема для календаря (через lookup)
        if (isDarkTheme) {
            datePicker.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-border-color: #333; -fx-border-radius: 5;");
            datePicker.getEditor().setStyle("-fx-background-color: #181818; -fx-text-fill: white;");

            // Ждём, пока попап откроется, и меняем его стиль
            datePicker.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                javafx.application.Platform.runLater(() -> {
                    Node popup = datePicker.lookup(".date-picker-popup");
                    if (popup != null) {
                        popup.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-text-fill: white;");
                    }
                });
            });
        }

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #999;");
                }
            }
        });

// Время
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00");
        timeCombo.setValue("23:59");
        timeCombo.setPromptText(isEnglish ? "Time" : "Время");
        if (isDarkTheme) {
            timeCombo.setStyle("-fx-background-color: #181818; -fx-text-fill: white; -fx-border-color: #333; -fx-border-radius: 5;");
        } else {
            timeCombo.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-border-color: #ccc; -fx-border-radius: 5;");
        }
        timeCombo.setPrefWidth(300);

// Контейнер для даты и времени
        HBox dateTimeBox = new HBox(10);
        dateTimeBox.setAlignment(Pos.CENTER);
        dateTimeBox.getChildren().addAll(datePicker, timeCombo);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addBtn = new Button(isEnglish ? "Add" : "Добавить");
        if (isDarkTheme) {
            addBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 25;");
        } else {
            addBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 8; -fx-padding: 8 25;");
        }

        Button cancelBtn = new Button(isEnglish ? "Cancel" : "Отмена");
        if (isDarkTheme) {
            cancelBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #888; -fx-background-radius: 8; -fx-padding: 8 25;");
        } else {
            cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #666; -fx-background-radius: 8; -fx-padding: 8 25;");
        }

        buttonBox.getChildren().addAll(addBtn, cancelBtn);
        dialogBox.getChildren().addAll(iconLabel, titleLabel, titleField, descArea, datePicker, timeCombo, buttonBox);
        Scene scene = new Scene(dialogBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        addStage.setScene(scene);

        addStage.setOnShown(event -> {
            addStage.setX(ownerStage.getX() + (ownerStage.getWidth() - addStage.getWidth()) / 2);
            addStage.setY(ownerStage.getY() + (ownerStage.getHeight() - addStage.getHeight()) / 2);
        });

        addBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            String selectedTime = timeCombo.getValue();
            LocalDateTime dueDateTime = null;
            if (selectedDate != null && selectedTime != null) {
                dueDateTime = LocalDateTime.of(selectedDate, LocalTime.parse(selectedTime));
            }
            String result = taskService.addTask(currentUserId, titleField.getText(), descArea.getText(), dueDateTime);
            if (result.equals("success")) {
                loadTasks();
                addStage.close();
            }
        });
        cancelBtn.setOnAction(e -> addStage.close());

        addStage.showAndWait();
    }



    @FXML
    public void initialize() {

        addTaskBtn.setOnAction(event -> handleAddTask());


        searchField.textProperty().addListener((obs, old, newVal) -> {
            searchQuery = newVal;
            updateKanban();
        });
    }
}