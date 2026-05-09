package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.UserRole;
import models.Utilisateur;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatProjetController {

    @FXML private VBox chatContainer;
    @FXML private TextField msgInput;
    @FXML private Label projetTitle;
    @FXML private ScrollPane scrollPane;

    private int currentProjetId;
    private String myName;
    private ScheduledExecutorService scheduler;

    private final Set<String> displayedMessageIds = new HashSet<>();
    private final String FIREBASE_URL = "https://stratixchat-default-rtdb.firebaseio.com/";

    public void initChat(int idProjet, String nomProjet) {
        this.currentProjetId = idProjet;
        this.projetTitle.setText("Discussion : " + nomProjet);

        Utilisateur user = UserRole.getInstance().getUser();
        this.myName = (user != null) ? user.getNom() + " " + user.getPrenom() : "Anonyme";

        chatContainer.getChildren().clear();
        displayedMessageIds.clear();

        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (scrollPane != null) scrollPane.setVvalue(1.0);
        });

        startRealTimeSync();
    }

    @FXML
    private void handleSend() {
        String text = msgInput.getText().trim();
        if (text.isEmpty()) return;

        envoyerVersFirebase(text);
        msgInput.clear();
    }

    private void envoyerVersFirebase(String text) {
        JSONObject payload = new JSONObject();
        payload.put("sender", myName);
        payload.put("text", text);
        payload.put("timestamp", System.currentTimeMillis());

        String url = FIREBASE_URL + "projet-" + currentProjetId + ".json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private void startRealTimeSync() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::fetchMessages, 0, 2, TimeUnit.SECONDS);
    }

    private void fetchMessages() {
        String url = FIREBASE_URL + "projet-" + currentProjetId + ".json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    String body = response.body();
                    if (body != null && !body.equals("null")) {
                        JSONObject allMsgs = new JSONObject(body);

                        Platform.runLater(() -> {
                            allMsgs.keySet().stream()
                                    .filter(key -> !displayedMessageIds.contains(key))
                                    .sorted((k1, k2) -> Long.compare(
                                            allMsgs.getJSONObject(k1).getLong("timestamp"),
                                            allMsgs.getJSONObject(k2).getLong("timestamp")))
                                    .forEach(key -> {
                                        JSONObject m = allMsgs.getJSONObject(key);
                                        displayMessage(m.getString("sender"), m.getString("text"), m.getLong("timestamp"));
                                        displayedMessageIds.add(key);
                                    });
                        });
                    }
                });
    }

    private void displayMessage(String sender, String content, long timestamp) {
        VBox msgWrapper = new VBox(3);
        boolean isMe = sender.equals(myName);

        String heure = new SimpleDateFormat("HH:mm").format(new Date(timestamp));
        Label headerLbl = new Label(isMe ? "Moi • " + heure : sender + " • " + heure);
        headerLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        Label contentLbl = new Label(content);
        contentLbl.setWrapText(true);
        contentLbl.setMaxWidth(280);
        contentLbl.setMinHeight(Region.USE_PREF_SIZE);

        if (isMe) {
            msgWrapper.setAlignment(Pos.TOP_RIGHT);
            contentLbl.setStyle(
                    "-fx-background-color: #3b82f6; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 12; " +
                            "-fx-background-radius: 15 15 0 15;"
            );
        } else {
            msgWrapper.setAlignment(Pos.TOP_LEFT);
            contentLbl.setStyle(
                    "-fx-background-color: #b3b3b3; " +
                            "-fx-text-fill: #1e293b; " +
                            "-fx-padding: 8 12; " +
                            "-fx-background-radius: 15 15 15 0;"
            );
        }

        VBox.setMargin(msgWrapper, new Insets(5, 0, 5, 0));

        msgWrapper.getChildren().addAll(headerLbl, contentLbl);
        chatContainer.getChildren().add(msgWrapper);
    }

    public void stopChat() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}