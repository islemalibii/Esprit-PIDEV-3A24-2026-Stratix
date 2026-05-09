package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.DashboardNotificationManager;

public class EmailComposerDialog {

    public static void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("📧 Nouveau message");

        // Main layout
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label headerLabel = new Label("📧 Nouveau message");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Separator sep1 = new Separator();

        // Email fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // À
        Label toLabel = new Label("À:");
        toLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField toField = new TextField();
        toField.setPromptText("destinataire@email.com");
        toField.setPrefWidth(400);

        // Cc
        Label ccLabel = new Label("Cc:");
        ccLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField ccField = new TextField();
        ccField.setPromptText("copie@email.com (optionnel)");

        // Cci
        Label cciLabel = new Label("Cci:");
        cciLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField cciField = new TextField();
        cciField.setPromptText("copie cachée@email.com (optionnel)");

        // Objet
        Label subjectLabel = new Label("Objet:");
        subjectLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField subjectField = new TextField();
        subjectField.setPromptText("Objet du message");

        // Ajout au grid
        grid.add(toLabel, 0, 0);
        grid.add(toField, 1, 0);
        grid.add(ccLabel, 0, 1);
        grid.add(ccField, 1, 1);
        grid.add(cciLabel, 0, 2);
        grid.add(cciField, 1, 2);
        grid.add(subjectLabel, 0, 3);
        grid.add(subjectField, 1, 3);

        ColumnConstraints col1 = new ColumnConstraints(50);
        ColumnConstraints col2 = new ColumnConstraints(450);
        grid.getColumnConstraints().addAll(col1, col2);

        // Corps du message
        Label bodyLabel = new Label("Message:");
        bodyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Contenu de votre message...");
        bodyArea.setPrefRowCount(8);
        bodyArea.setWrapText(true);

        VBox bodyBox = new VBox(5, bodyLabel, bodyArea);

        Separator sep2 = new Separator();

        // Type de notification
        Label typeLabel = new Label("Type de notification:");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                "📋 Notification de tâche",
                "📅 Notification de planning",
                "📊 Notification de projet",
                "📧 Email personnalisé"
        );
        typeCombo.setValue("📧 Email personnalisé");
        typeCombo.setPrefWidth(300);

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button sendButton = new Button("📤 Envoyer");
        sendButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");

        Button cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;");

        buttonBox.getChildren().addAll(sendButton, cancelButton);

        // Status label
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Assemble all
        mainLayout.getChildren().addAll(
                headerLabel, sep1, grid, bodyBox,
                new Separator(), typeLabel, typeCombo,
                buttonBox, statusLabel
        );

        // Button actions
        sendButton.setOnAction(e -> {
            String to = toField.getText().trim();
            if (to.isEmpty()) {
                statusLabel.setText("❌ Veuillez saisir au moins un destinataire");
                statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                return;
            }

            String subject = subjectField.getText().trim();
            if (subject.isEmpty()) {
                subject = "Notification de Stratix";
            }

            String body = bodyArea.getText().trim();
            if (body.isEmpty()) {
                body = "Message par défaut";
            }

            String selectedType = typeCombo.getValue();
            statusLabel.setText("📤 Envoi en cours...");
            statusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");

            // Envoyer selon le type sélectionné
            if (selectedType.contains("tâche")) {
                DashboardNotificationManager.getInstance().notifyNewTask(to, subject, body,
                        () -> javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("✅ Notification de tâche envoyée à " + to);
                            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        }));
            } else if (selectedType.contains("planning")) {
                DashboardNotificationManager.getInstance().notifyNewPlanning(to, "INFO", "Date", body);
                statusLabel.setText("✅ Notification de planning envoyée à " + to);
                statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else if (selectedType.contains("projet")) {
                DashboardNotificationManager.getInstance().notifyProjectUpdate(to, subject, body);
                statusLabel.setText("✅ Notification de projet envoyée à " + to);
                statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else {
                // Email personnalisé simple
                DashboardNotificationManager.getInstance().notifyNewTask(to, subject, body,
                        () -> javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("✅ Email envoyé à " + to);
                            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        }));
            }

            // Fermer après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> dialog.close());
                } catch (InterruptedException ex) {}
            }).start();
        });

        cancelButton.setOnAction(e -> dialog.close());

        // Scene
        Scene scene = new Scene(mainLayout, 600, 650);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}//