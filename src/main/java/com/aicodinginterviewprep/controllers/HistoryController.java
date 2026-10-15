package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.History;
import com.aicodinginterviewprep.db.HistoryRepository;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.AppErrorHandler;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class HistoryController implements SceneAware {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final HistoryRepository historyRepository = new HistoryRepository();
    private final UserRepository userRepository = new UserRepository();
    private SceneManager sceneManager;
    private Background background = Background.THREAD;

    @FXML
    public ListView<History> listDates;
    @FXML 
    public TextFlow textFlowTopics;

    @FXML 
    public TextFlow textFlowScores;

    @FXML
    public Button buttonBack;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        listDates.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(History item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_FORMAT.format(item.historyDate()));
            }
        });
        listDates.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldHistory, newHistory) -> showHistory(newHistory));
    }

    @Override
    public void onSceneShown() {
        String username = sceneManager.getCurrentUsername();
        if (username == null || username.isBlank()) {
            listDates.getItems().clear();
            return;
        }

        background.run(
                () -> userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException(
                                "Logged-in user was not found: " + username)),
                user -> background.run(
                        () -> historyRepository.findByUser(user.id()),
                        histories -> listDates.getItems().setAll(histories),
                        error -> AppErrorHandler.handle(error, "Loading history")),
                error -> AppErrorHandler.handle(error, "Loading history"));
    }

    private void showHistory(History history) {
        textFlowTopics.getChildren().clear();
        textFlowScores.getChildren().clear();
        if (history == null) {
            return;
        }

        textFlowTopics.getChildren().add(new Text(describeTopics(history)));
        textFlowScores.getChildren().add(new Text(describeScores(history)));
    }

    static String describeTopics(History history) {
        return history.topics().stream()
                .collect(Collectors.joining("\n"));
    }

    static String describeScores(History history) {
        return "Correctness: " + score(history.correctnessScore()) + "\n"
                + "Efficiency: " + score(history.efficiencyScore()) + "\n"
                + "Communication: " + score(history.communicationScore()) + "\n"
                + "Code quality: " + score(history.codeQualityScore()) + "\n"
                + "Average: " + score(history.averageScore());
    }

    private static String score(BigDecimal score) {
        return score == null ? "N/A" : score.toPlainString();
    }

    @FXML
    public void onBack() {
        sceneManager.switchToScene("practice");
    }
}
