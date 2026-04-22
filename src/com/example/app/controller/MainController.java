package com.example.app.controller;

import com.example.app.model.Task;
import com.example.app.model.User;
import com.example.app.service.TaskService;
import com.example.app.service.UserService;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class MainController {
git

    @FXML private VBox contentArea;
    @FXML private Button userMenuBtn;
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label inProgressTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressPercentLabel;
    @FXML private Button themeToggleBtn;
    @FXML private Button homeBtn;
    @FXML private Button tasksBtn;
    @FXML private Button statsBtn;
    @FXML private Button aboutBtn;
    @FXML private Region leftSpacer;
    private Preferences prefs;


    private boolean isDarkTheme = true;
    private final TaskService taskService = new TaskService();
    private Popup userPopup;
    private User currentUser;
    private final UserService userService = new UserService();
    private boolean isEnglish = false;
    private boolean isNavigatingFromNotification = false;

    public void setUser(User user) {
        if (user == null) {
            this.currentUser = new User("Guest", "guest@mail.ru", "");
            this.currentUser.setId(1);
        } else {
            this.currentUser = user;
        }

        // Загружаем сохранённую тему
        prefs = Preferences.userNodeForPackage(MainController.class);
        boolean savedTheme = prefs.getBoolean("isDarkTheme", true);
        isDarkTheme = savedTheme;

        // Применяем тему
        applyTheme();

        handleHome();
        checkDeadlines();
    }

    private void applyTheme() {
        Scene scene = themeToggleBtn.getScene();

        if (isDarkTheme) {
            scene.getRoot().setStyle("-fx-background-color: linear-gradient(to bottom, #0D0D0D, #121212);");
            themeToggleBtn.setText("🌙");

            homeBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            tasksBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            statsBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            aboutBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");

            userMenuBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");
            themeToggleBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");

            Node topPanel = scene.lookup(".top-panel");
            if (topPanel != null) {
                topPanel.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10 20;");
            }
        } else {
            scene.getRoot().setStyle("-fx-background-color: linear-gradient(to bottom, #e8e8e8, #f5f5f5);");
            themeToggleBtn.setText("☀");

            homeBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            tasksBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            statsBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
            aboutBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");

            userMenuBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 18; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");
            themeToggleBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");

            Node topPanel = scene.lookup(".top-panel");
            if (topPanel != null) {
                topPanel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10 20;");
            }
        }
    }

    private void switchContent(Node newContent) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(newContent);
    }


    private VBox createStatCard(String emoji, String value, String title) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);

        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 15; -fx-padding: 15; -fx-min-width: 100;");
        } else {
            card.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15; -fx-padding: 15; -fx-min-width: 100;");
        }

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24;");

        Label valueLabel = new Label(value);
        if (isDarkTheme) {
            valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        } else {
            valueLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 18; -fx-font-weight: bold;");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        }

        card.getChildren().addAll(emojiLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createStatCardLarge(String title, String value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 120; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 120; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11; -fx-font-weight: bold;");
        }

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 28; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createPieChartCard(String title, PieChart pieChart) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-font-weight: bold;");
        }

        pieChart.setPrefSize(180, 180);
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        pieChart.setStyle("-fx-background-color: transparent;");

        card.getChildren().addAll(titleLabel, pieChart);
        return card;
    }

    private VBox createProgressCard(String title, String subtitle, int progress) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200;");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200;");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14; -fx-font-weight: bold;");
        }

        Label subtitleLabel = new Label(subtitle);
        if (isDarkTheme) {
            subtitleLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        } else {
            subtitleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        }

        ProgressBar progressBar = new ProgressBar(progress / 100.0);
        progressBar.setPrefWidth(160);
        progressBar.setStyle("-fx-accent: #3498db; -fx-background-color: #2a2a2a;");

        Label percentLabel = new Label(progress + "%");
        if (isDarkTheme) {
            percentLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12;");
        } else {
            percentLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        }

        card.getChildren().addAll(titleLabel, subtitleLabel, progressBar, percentLabel);
        return card;
    }

    private VBox createListCard(String title, List<String> items, List<String> values) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200;");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 200;");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14; -fx-font-weight: bold;");
        }

        VBox itemsBox = new VBox(5);
        for (int i = 0; i < items.size(); i++) {
            HBox itemRow = new HBox(10);
            Label nameLabel = new Label(items.get(i));
            Label valueLabel = new Label(values.get(i));

            if (isDarkTheme) {
                nameLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");
                valueLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14; -fx-font-weight: bold;");
            } else {
                nameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
                valueLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14; -fx-font-weight: bold;");
            }

            itemRow.getChildren().addAll(nameLabel, valueLabel);
            itemsBox.getChildren().add(itemRow);
        }

        card.getChildren().addAll(titleLabel, itemsBox);
        return card;
    }

    @FXML
    public void handleHome() {
        contentArea.getChildren().clear();

        VBox homeBox = new VBox(100);
        homeBox.setAlignment(Pos.TOP_CENTER);
        homeBox.setStyle("-fx-padding: 60 30 30 30;");
        themeToggleBtn.setPrefSize(50, 35);
        Label titleLabel = new Label(isEnglish ? "WELCOME!" : "ДОБРО ПОЖАЛОВАТЬ!");
        Label messageLabel = new Label(isEnglish ? "Glad to see you again!" : "Рады снова тебя видеть!");



        // ===== 1. ПРИВЕТСТВЕННАЯ КАРТОЧКА =====
        VBox welcomeCard = new VBox(15);
        welcomeCard.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            welcomeCard.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 30;");
        } else {
            welcomeCard.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15; -fx-padding: 30;");
        }

        Label waveLabel = new Label("👋");
        waveLabel.setStyle("-fx-font-size: 48;");

        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 24; -fx-font-weight: bold;");
        }

        if (isDarkTheme) {
            messageLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 16;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16;");
        }

        welcomeCard.getChildren().addAll(waveLabel, titleLabel, messageLabel);
        homeBox.getChildren().add(welcomeCard);

        // ===== 2. 5 КАРТОЧЕК СО СТАТИСТИКОЙ =====
        HBox statsCardsBox = new HBox(20);
        statsCardsBox.setAlignment(Pos.CENTER);
        statsCardsBox.setStyle("-fx-padding: 20 0 0 0;");

        List<Task> allTasks = taskService.getUserTasks(currentUser.getId());
        int total = allTasks.size();
        long completed = allTasks.stream().filter(Task::isCompleted).count();
        long inProgress = allTasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        long draft = allTasks.stream().filter(t -> "draft".equals(t.getStatus())).count();
        int efficiency = total > 0 ? (int) ((double) completed / total * 100) : 0;
        VBox card1 = createStatCardLarge(isEnglish ? "TOTAL" : "ВСЕГО", String.valueOf(total), "#3498db");
        VBox card2 = createStatCardLarge(isEnglish ? "COMPLETED" : "ВЫПОЛНЕНО", String.valueOf(completed), "#27ae60");
        VBox card3 = createStatCardLarge(isEnglish ? "IN PROGRESS" : "В ПРОЦЕССЕ", String.valueOf(inProgress), "#f39c12");
        VBox card4 = createStatCardLarge(isEnglish ? "PENDING" : "ОЖИДАЮТ", String.valueOf(draft), "#e74c3c");
        VBox card5 = createStatCardLarge(isEnglish ? "EFFICIENCY" : "ЭФФЕКТИВНОСТЬ", efficiency + "%", "#9b59b6");

        statsCardsBox.getChildren().addAll(card1, card2, card3, card4, card5);
        homeBox.getChildren().add(statsCardsBox);


        contentArea.getChildren().add(homeBox);
    }

    private VBox createBarCard(String title, List<String> labels, List<Double> values, List<String> colors) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 280;");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 280;");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14; -fx-font-weight: bold;");
        }

        VBox barsBox = new VBox(8);
        double maxValue = values.stream().max(Double::compare).orElse(1.0);

        for (int i = 0; i < labels.size(); i++) {
            HBox barRow = new HBox(10);
            barRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(labels.get(i));
            if (isDarkTheme) {
                nameLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11; -fx-min-width: 80;");
            } else {
                nameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11; -fx-min-width: 80;");
            }

            Region bar = new Region();
            double percent = (values.get(i) / maxValue) * 100;
            bar.setPrefWidth(160 * percent / 100);
            bar.setPrefHeight(20);
            bar.setStyle("-fx-background-color: " + colors.get(i) + "; -fx-background-radius: 4;");

            Label valueLabel = new Label(String.valueOf(values.get(i).intValue()));
            if (isDarkTheme) {
                valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-min-width: 35;");
            } else {
                valueLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 11; -fx-min-width: 35;");
            }

            barRow.getChildren().addAll(nameLabel, bar, valueLabel);
            barsBox.getChildren().add(barRow);
        }

        card.getChildren().addAll(titleLabel, barsBox);
        return card;
    }

    private VBox createCircularProgressCard(String title, int progress, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        if (isDarkTheme) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 150;");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 150;");
        }

        Label titleLabel = new Label(title);
        if (isDarkTheme) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        } else {
            titleLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14; -fx-font-weight: bold;");
        }

        // Круговой прогресс через CSS (или можно использовать ProgressBar с круглой формой)
        ProgressBar circularProgress = new ProgressBar(progress / 100.0);
        circularProgress.setPrefWidth(100);
        circularProgress.setPrefHeight(100);
        circularProgress.setStyle("-fx-accent: " + color + "; -fx-background-color: #2a2a2a; -fx-background-radius: 50; -fx-padding: 0;");

        Label percentLabel = new Label(progress + "%");
        if (isDarkTheme) {
            percentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        } else {
            percentLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 18; -fx-font-weight: bold;");
        }

        // Накладываем процент на прогресс-бар
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(circularProgress, percentLabel);

        card.getChildren().addAll(titleLabel, stackPane);
        return card;
    }

    @FXML
    public void handleStatistics() {
        contentArea.getChildren().clear();

        VBox statsView = new VBox(25);
        statsView.setAlignment(Pos.TOP_CENTER);
        statsView.setStyle("-fx-padding: 30;");
        statsView.setId("statisticsView");
        // Заголовок
        Label title = new Label(isEnglish ? "STATISTICS" : "СТАТИСТИКА");
        if (isDarkTheme) {
            title.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
        } else {
            title.setStyle("-fx-text-fill: #333; -fx-font-size: 24; -fx-font-weight: bold;");
        }
        statsView.getChildren().add(title);

        // Горизонтальные диаграммы
        List<Task> tasks = taskService.getUserTasks(currentUser.getId());

        // Статусы задач
        long draft = tasks.stream().filter(t -> "draft".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "done".equals(t.getStatus())).count();

        List<String> statusLabels = isEnglish ? Arrays.asList("To Do", "In Progress", "Done") : Arrays.asList("Нужно сделать", "В процессе", "Готово");
        List<Double> statusValues = Arrays.asList((double) draft, (double) inProgress, (double) done);
        List<String> statusColors = Arrays.asList("#e74c3c", "#f39c12", "#27ae60");
        VBox statusBarCard = createBarCard(isEnglish ? "TASK STATUS" : "СТАТУС ЗАДАЧ", statusLabels, statusValues, statusColors);

        // Приоритеты задач
        long high = tasks.stream().filter(t -> "high".equals(t.getPriority())).count();
        long medium = tasks.stream().filter(t -> "medium".equals(t.getPriority())).count();
        long low = tasks.stream().filter(t -> "low".equals(t.getPriority())).count();

        List<String> priorityLabels = isEnglish ? Arrays.asList("High", "Medium", "Low") : Arrays.asList("Высокий", "Средний", "Низкий");
        List<Double> priorityValues = Arrays.asList((double) high, (double) medium, (double) low);
        List<String> priorityColors = Arrays.asList("#e74c3c", "#f39c12", "#27ae60");
        VBox priorityBarCard = createBarCard(isEnglish ? "PRIORITIES" : "ПРИОРИТЕТЫ", priorityLabels, priorityValues, priorityColors);

        // Задачи по дням
        Map<String, Long> tasksByDate = tasks.stream()
                .filter(t -> t.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM")),
                        Collectors.counting()
                ));

        List<String> dateLabels = new ArrayList<>(tasksByDate.keySet());
        List<Double> dateValues = new ArrayList<>();
        for (String label : dateLabels) {
            dateValues.add((double) tasksByDate.get(label));
        }
        List<String> dateColors = Collections.nCopies(dateLabels.size(), "#3498db");

        VBox dateBarCard = createBarCard(isEnglish ? "TASKS BY DAY" : "ЗАДАЧИ ПО ДНЯМ", dateLabels, dateValues, dateColors);

        // Размещаем диаграммы в сетке
        HBox row1 = new HBox(20);
        row1.setAlignment(Pos.CENTER);
        row1.getChildren().addAll(statusBarCard, priorityBarCard);

        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER);
        row2.getChildren().add(dateBarCard);

        statsView.getChildren().addAll(row1, row2);
        switchContent(statsView);
    }

    private void checkDeadlines() {
        List<Task> tasks = taskService.getUserTasks(1);
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = new ArrayList<>();
        List<Task> todayDeadlines = new ArrayList<>();
        List<Task> hourReminderTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDueDateTime() != null && !task.isCompleted()) {
                LocalDateTime due = task.getDueDateTime();
                if (due.isBefore(now)) {
                    overdueTasks.add(task);
                } else if (due.toLocalDate().equals(now.toLocalDate())) {
                    todayDeadlines.add(task);
                }

                // Проверка: дедлайн через 1 час (± 5 минут)
                long minutesUntilDue = java.time.Duration.between(now, due).toMinutes();
                if (minutesUntilDue > 55 && minutesUntilDue < 65) {
                    hourReminderTasks.add(task);
                }
            }
        }

        // Уведомление о просроченных
        if (!overdueTasks.isEmpty()) {
            showDeadlineNotification(overdueTasks, "Просроченные задачи");
        }
        // Уведомление о дедлайне через час
        else if (!hourReminderTasks.isEmpty()) {
            showDeadlineNotification(hourReminderTasks, "Дедлайн через час!");
        }
        // Уведомление о сегодняшних дедлайнах
        else if (!todayDeadlines.isEmpty()) {
            showDeadlineNotification(todayDeadlines, "Сегодняшние дедлайны");
        }
    }

    private void showDeadlineNotification(List<Task> tasks, String title) {
        Platform.runLater(() -> {
            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.TRANSPARENT);
            notificationStage.setTitle(title);

            String icon;
            switch (title) {
                case "Просроченные задачи":
                    icon = "⚠️";
                    break;
                case "Дедлайн через час!":
                    icon = "⏰";
                    break;
                default:
                    icon = "📅";
            }

            // Выбираем цвета в зависимости от темы
            String bgColor;
            String cardColor;
            String textColor;
            String buttonColor;

            if (isDarkTheme) {
                bgColor = "#2d2d2d";
                cardColor = "#2a2a2a";
                textColor = "white";
                buttonColor = "#4a4a4a";
            } else {
                bgColor = "#f5f5f5";
                cardColor = "#ffffff";
                textColor = "#333333";
                buttonColor = "#e0e0e0";
            }

            // Основной контейнер
            VBox mainBox = new VBox(15);
            mainBox.setAlignment(Pos.CENTER);
            mainBox.setStyle(
                    "-fx-background-color: " + bgColor + "; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 25; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);"
            );
            mainBox.setPrefWidth(400);

            Region topBar = new Region();
            if (isDarkTheme) {
                topBar.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 20 20 0 0;");
            } else {
                topBar.setStyle("-fx-background-color: #dddddd; -fx-background-radius: 20 20 0 0;");
            }
            topBar.setPrefHeight(8);
            topBar.setPrefWidth(400);

            // Иконка
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 48;");

            // Заголовок
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 20; -fx-font-weight: bold;");

            // Счётчик
            Label countLabel = new Label("У вас " + tasks.size() + " " + getTaskWord(tasks.size()));
            countLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");

            // Список задач
            VBox tasksBox = new VBox(8);
            tasksBox.setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10; -fx-padding: 10;");

            for (Task task : tasks) {
                HBox taskRow = new HBox(10);
                taskRow.setAlignment(Pos.CENTER_LEFT);
                taskRow.setStyle("-fx-padding: 5;");

                Label bulletLabel = new Label("📌");
                bulletLabel.setStyle("-fx-font-size: 14;");

                VBox taskInfo = new VBox(2);

                Label taskTitle = new Label(task.getTitle());
                taskTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14; -fx-font-weight: bold;");

                String timeStr = task.getDueDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                Label taskTime = new Label("⏱️ " + timeStr);
                taskTime.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

                taskInfo.getChildren().addAll(taskTitle, taskTime);
                taskRow.getChildren().addAll(bulletLabel, taskInfo);
                tasksBox.getChildren().add(taskRow);
            }

            // Кнопки
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button okBtn = new Button("Понятно");
            okBtn.setStyle("-fx-background-color: " + buttonColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-cursor: hand;");
            okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: #5a5a5a; -fx-text-fill: " + textColor + "; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-cursor: hand;"));
            okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: " + buttonColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-cursor: hand;"));
            okBtn.setOnAction(e -> notificationStage.close());

            Button goToTasksBtn = new Button("Перейти к задачам");
            if (isDarkTheme) {
                goToTasksBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;");
                goToTasksBtn.setOnMouseEntered(e -> goToTasksBtn.setStyle("-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;"));
                goToTasksBtn.setOnMouseExited(e -> goToTasksBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;"));
            } else {
                goToTasksBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;");
                goToTasksBtn.setOnMouseEntered(e -> goToTasksBtn.setStyle("-fx-background-color: #d0d0d0; -fx-text-fill: #333; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;"));
                goToTasksBtn.setOnMouseExited(e -> goToTasksBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 25; -fx-padding: 8 25; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;"));
            }
            goToTasksBtn.setOnAction(e -> {
                notificationStage.close();
                handleTasks();
            });

            buttonBox.getChildren().addAll(okBtn, goToTasksBtn);

            mainBox.getChildren().addAll(topBar, iconLabel, titleLabel, countLabel, tasksBox, buttonBox);

            Scene scene = new Scene(mainBox);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            notificationStage.setScene(scene);

            // Центрируем окно
            Stage ownerStage = (Stage) themeToggleBtn.getScene().getWindow();
            notificationStage.initOwner(ownerStage);
            notificationStage.setOnShown(event -> {
                notificationStage.setX(ownerStage.getX() + (ownerStage.getWidth() - mainBox.getPrefWidth()) / 2);
                notificationStage.setY(ownerStage.getY() + (ownerStage.getHeight() - 400) / 2);
            });

            notificationStage.show();
        });
    }

    private String getTaskWord(int count) {
        if (count == 1) return "задача";
        if (count >= 2 && count <= 4) return "задачи";
        return "задач";
    }

    @FXML
    public void handleAbout() {
        contentArea.getChildren().clear();

        VBox aboutBox = new VBox(15);
        aboutBox.setAlignment(Pos.CENTER);
        aboutBox.setStyle("-fx-padding: 40;");
        aboutBox.setId("aboutView");

        Label title = new Label(isEnglish ? "ABOUT" : "О ПРОГРАММЕ");
        if (isDarkTheme) {
            title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        } else {
            title.setStyle("-fx-text-fill: #333; -fx-font-size: 20; -fx-font-weight: bold;");
        }

        Label version = new Label(isEnglish ? "Version: 2.0.0" : "Версия: 2.0.0");
        if (isDarkTheme) {
            version.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");
        } else {
            version.setStyle("-fx-text-fill: #555; -fx-font-size: 14;");
        }

        Label author = new Label(isEnglish ? "Developer: maxino" : "Разработчик: maxino");
        if (isDarkTheme) {
            author.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");
        } else {
            author.setStyle("-fx-text-fill: #555; -fx-font-size: 14;");
        }

        Label description = new Label(isEnglish ? "Task manager for everyday routine!" : "Менеджер задач для повседневной рутины!");
        if (isDarkTheme) {
            description.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
        } else {
            description.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        }
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        aboutBox.getChildren().addAll(title, version, author, description);
        switchContent(aboutBox);
    }

    @FXML
    private void showUserMenu() {
        if (userPopup == null) {
            createUserPopup();
        }
        userPopup.show(userMenuBtn,
                userMenuBtn.localToScreen(userMenuBtn.getBoundsInLocal()).getMinX(),
                userMenuBtn.localToScreen(userMenuBtn.getBoundsInLocal()).getMaxY());
    }

    private void createUserPopup() {
        userPopup = new Popup();
        userPopup.setAutoHide(true);

        VBox menuBox = new VBox(5);

        if (isDarkTheme) {
            menuBox.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-padding: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        } else {
            menuBox.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10; -fx-padding: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        }

        Button settingsBtn = new Button(isEnglish ? "⚙ Settings" : "⚙ Настройки");
        if (isDarkTheme) {
            settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;");
            settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;"));
            settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;"));
        } else {
            settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;");
            settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle("-fx-background-color: #d0d0d0; -fx-text-fill: #333; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;"));
            settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-alignment: CENTER_LEFT; -fx-padding: 8 20; -fx-background-radius: 5;"));
        }
        settingsBtn.setOnAction(e -> { userPopup.hide(); showSettings(); });

        menuBox.getChildren().add(settingsBtn);
        userPopup.getContent().add(menuBox);
    }

    private void updateUserPopupLanguage() {
        if (userPopup != null && userPopup.getContent().size() > 0) {
            Node content = userPopup.getContent().get(0);
            if (content instanceof VBox) {
                VBox menuBox = (VBox) content;
                if (menuBox.getChildren().size() > 0 && menuBox.getChildren().get(0) instanceof Button) {
                    Button settingsBtn = (Button) menuBox.getChildren().get(0);
                    settingsBtn.setText(isEnglish ? "⚙ Settings" : "⚙ Настройки");
                }
            }
        }
    }

    private void showSettings() {
        contentArea.getChildren().clear();

        VBox settingsBox = new VBox(15);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setStyle("-fx-padding: 40;");
        settingsBox.setId("settingsView");

        Label title = new Label(isEnglish ? "SETTINGS" : "НАСТРОЙКИ");
        if (isDarkTheme) {
            title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        } else {
            title.setStyle("-fx-text-fill: #333; -fx-font-size: 20; -fx-font-weight: bold;");
        }

        Label languageLabel = new Label(isEnglish ? "Language:" : "Язык:");
        if (isDarkTheme) {
            languageLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");
        } else {
            languageLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14;");
        }

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Русский", "English");
        languageCombo.setValue(isEnglish ? "English" : "Русский");
        languageCombo.setStyle("-fx-background-color: #181818; -fx-text-fill: white;");

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            if (selected.equals("English")) {
                changeLanguageToEnglish();
            } else {
                changeLanguageToRussian();
            }
        });

        settingsBox.getChildren().addAll(title, languageLabel, languageCombo);
        switchContent(settingsBox);
    }

    private void changeLanguageToEnglish() {
        homeBtn.setText("Home");
        tasksBtn.setText("Tasks");
        statsBtn.setText("Statistics");
        aboutBtn.setText("About");
        isEnglish = true;

        // Перерисовываем текущую страницу
        if (contentArea.getChildren().size() > 0) {
            Node currentPage = contentArea.getChildren().get(0);
            String pageId = currentPage.getId();

            if (pageId != null) {
                switch (pageId) {
                    case "tasksView":
                        handleTasks();
                        break;
                    case "aboutView":
                        handleAbout();
                        break;
                    case "statisticsView":
                        handleStatistics();
                        break;
                    default:
                        showSettings();
                        break;
                }
            } else {
                showSettings();
            }
        } else {
            showSettings();
        }

        updateUserPopupLanguage();
    }

    private void changeLanguageToRussian() {
        homeBtn.setText("Главная");
        tasksBtn.setText("Мои задачи");
        statsBtn.setText("Статистика");
        aboutBtn.setText("О программе");
        isEnglish = false;

        // Перерисовываем текущую страницу
        if (contentArea.getChildren().size() > 0) {
            Node currentPage = contentArea.getChildren().get(0);
            String pageId = currentPage.getId();

            if (pageId != null) {
                switch (pageId) {
                    case "tasksView":
                        handleTasks();
                        break;
                    case "aboutView":
                        handleAbout();
                        break;
                    case "statisticsView":
                        handleStatistics();
                        break;
                    default:
                        showSettings();
                        break;
                }
            } else {
                showSettings();
            }
        } else {
            showSettings();
        }

        updateUserPopupLanguage();
    }

    private void shakeField(Node field) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), field);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
        shake.setOnFinished(event -> field.setTranslateX(0));
    }

    @FXML
    public void handleTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tasks.fxml"));
            VBox tasksView = loader.load();
            tasksView.setId("tasksView");
            TasksController tasksController = loader.getController();
            tasksController.setUserId(currentUser.getId());
            tasksController.setTheme(isDarkTheme);
            tasksController.setLanguage(isEnglish);
            switchContent(tasksView);
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Ошибка при загрузке задач");
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    @FXML
    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        prefs.putBoolean("isDarkTheme", isDarkTheme);
        applyTheme();
        refreshCurrentPage();
    }

    private void applyDarkTheme() {
        Scene scene = themeToggleBtn.getScene();
        scene.getRoot().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0D0D0D, #121212)"
        );
        themeToggleBtn.getScene().getRoot().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0D0D0D, #121212);"
        );
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        themeToggleBtn.setText("🌙");

        homeBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        tasksBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        statsBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        aboutBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");

        userMenuBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");
        themeToggleBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");

        Node topPanel = themeToggleBtn.getScene().lookup(".top-panel");
        if (topPanel != null) {
            topPanel.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10 20;");
        }
        handleHome();
    }

    private void applyLightTheme() {
        Scene scene = themeToggleBtn.getScene();
        scene.getRoot().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e8e8e8, #f5f5f5);"
        );
        themeToggleBtn.getScene().getRoot().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e8e8e8, #f5f5f5);"
        );
        themeToggleBtn.setText("☀️");

        homeBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        tasksBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        statsBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");
        aboutBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 8 15; -fx-background-radius: 5;");

        userMenuBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 18; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");
        themeToggleBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 20; -fx-pref-width: 35; -fx-pref-height: 35;");

        Node topPanel = themeToggleBtn.getScene().lookup(".top-panel");
        if (topPanel != null) {
            topPanel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10 20;");
        }
        handleHome();
    }

    private void refreshCurrentPage() {
        if (contentArea.getChildren().size() > 0) {
            Node currentPage = contentArea.getChildren().get(0);
            String pageId = currentPage.getId();

            if (pageId != null) {
                switch (pageId) {
                    case "tasksView":
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tasks.fxml"));
                            VBox tasksView = loader.load();
                            tasksView.setId("tasksView");
                            TasksController tasksController = loader.getController();
                            tasksController.setUserId(1);
                            tasksController.setTheme(isDarkTheme);
                            tasksController.setLanguage(isEnglish);
                            switchContent(tasksView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "statisticsView":
                        handleStatistics();
                        break;
                    case "aboutView":
                        handleAbout();
                        break;
                    default:
                        handleHome();
                        break;
                }
            } else {
                handleHome();
            }
        } else {
            handleHome();
        }
    }
}