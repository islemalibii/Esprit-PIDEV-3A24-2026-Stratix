package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import services.GroqAPI; // ← Changé ici !

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardChatWidget {

    @FXML private VBox chatContainer;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Label typingIndicator;
    @FXML private ScrollPane scrollPane;
    @FXML private Label welcomeMessage;

    @FXML
    public void initialize() {
        welcomeMessage.setText("👋 Bonjour ! Je suis votre assistant IA (Mixtral). Posez-moi une question !");

        FadeTransition fade = new FadeTransition(Duration.seconds(1), chatContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        inputField.setOnAction(e -> sendMessage());
        sendButton.disableProperty().bind(inputField.textProperty().isEmpty());

        addMessage("Bonjour ! Je suis là pour vous aider avec vos projets. Que voulez-vous savoir ?", false);

        // Test de connexion
        new Thread(() -> {
            boolean connected = GroqAPI.testConnection();
            System.out.println(connected ? "✅ Groq API connectée" : "⚠️ Groq API non disponible, mode local actif");
        }).start();
    }

    @FXML
    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) return;

        addMessage(userMessage, true);
        inputField.clear();

        showTypingIndicator(true);

        new Thread(() -> {
            try {
                String response = GroqAPI.sendMessage(userMessage);
                Thread.sleep(500);

                javafx.application.Platform.runLater(() -> {
                    showTypingIndicator(false);
                    addMessage(response, false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showTypingIndicator(false);
                    addMessage("Désolé, une erreur est survenue.", false);
                });
            }
        }).start();
    }

    private void addMessage(String text, boolean isUser) {
        HBox messageBox = createMessageBox(text, isUser);
        chatContainer.getChildren().add(messageBox);

        if (welcomeMessage.isVisible()) {
            welcomeMessage.setVisible(false);
            welcomeMessage.setManaged(false);
        }

        javafx.application.Platform.runLater(() ->
                scrollPane.setVvalue(1.0)
        );

        FadeTransition fade = new FadeTransition(Duration.millis(300), messageBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private HBox createMessageBox(String text, boolean isUser) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5, 0, 5, 0));
        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label avatarLabel = new Label(isUser ? "👤" : "🤖");
        avatarLabel.setFont(Font.font("Arial", 18));
        avatarLabel.setPrefWidth(30);
        avatarLabel.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(3);
        contentBox.setMaxWidth(300);

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Arial", 12));
        textLabel.setWrapText(true);
        textLabel.setPadding(new Insets(8, 12, 8, 12));

        String bubbleStyle;
        if (isUser) {
            bubbleStyle = "-fx-background-color: #3498db; -fx-background-radius: 15 15 5 15; -fx-text-fill: white;";
        } else {
            bubbleStyle = "-fx-background-color: #f0f0f0; -fx-background-radius: 15 15 15 5; -fx-text-fill: #2c3e50;";
        }
        textLabel.setStyle(bubbleStyle);

        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(Font.font("Arial", 9));
        timeLabel.setTextFill(Color.GRAY);
        timeLabel.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        contentBox.getChildren().addAll(textLabel, timeLabel);

        if (isUser) {
            box.getChildren().addAll(contentBox, avatarLabel);
        } else {
            box.getChildren().addAll(avatarLabel, contentBox);
        }

        return box;
    }

    private void showTypingIndicator(boolean show) {
        typingIndicator.setVisible(show);
        typingIndicator.setManaged(show);

        if (show) {
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(500), e -> {
                        typingIndicator.setOpacity(typingIndicator.getOpacity() == 1 ? 0.3 : 1);
                    })
            );
            timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeline.play();
            typingIndicator.setUserData(timeline);
        } else {
            javafx.animation.Timeline timeline = (javafx.animation.Timeline) typingIndicator.getUserData();
            if (timeline != null) {
                timeline.stop();
            }
            typingIndicator.setOpacity(1);
        }
    }

    @FXML
    private void clearChat() {
        chatContainer.getChildren().clear();
        welcomeMessage.setVisible(true);
        welcomeMessage.setManaged(true);
        addMessage("Bonjour ! Je suis là pour vous aider avec vos projets. Que voulez-vous savoir ?", false);
    }
}//