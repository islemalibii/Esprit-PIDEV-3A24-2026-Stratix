package services;

import javafx.scene.control.Alert;
import java.util.concurrent.CompletableFuture;

public class DashboardNotificationManager {

    private static DashboardNotificationManager instance;
    private final EmailService emailService;

    private DashboardNotificationManager() {
        this.emailService = EmailService.getInstance();
    }

    public static DashboardNotificationManager getInstance() {
        if (instance == null) {
            instance = new DashboardNotificationManager();
        }
        return instance;
    }//

    public void notifyNewTask(String recipientEmail, String taskTitle, String taskDescription, Runnable onSuccess) {
        emailService.sendEmailAsync(recipientEmail,
                        "📋 Nouvelle tâche assignée - Stratix",
                        buildTaskEmail(taskTitle, taskDescription))
                .thenAccept(success -> {
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            showNotification("✅ Notification envoyée",
                                    "L'email a été envoyé avec succès à " + recipientEmail);
                            if (onSuccess != null) onSuccess.run();
                        } else {
                            showNotification("❌ Erreur d'envoi",
                                    "Impossible d'envoyer l'email à " + recipientEmail);
                        }
                    });
                });
    }

    public void notifyNewPlanning(String recipientEmail, String shiftType, String date, String hours) {
        emailService.sendPlanningNotification(recipientEmail, shiftType, date, hours);
        showNotification("✅ Planning notifié", "L'employé " + recipientEmail + " a été informé de son planning");
    }

    public void notifyProjectUpdate(String recipientEmail, String projectName, String updateMessage) {
        emailService.sendProjectUpdate(recipientEmail, projectName, updateMessage);
        showNotification("✅ Projet mis à jour", "Notification envoyée à " + recipientEmail);
    }

    public void testEmailConnection() {
        // Test simple - envoyer un email de test
        emailService.sendWelcomeEmail("test@example.com", "Test User");
        showNotification("📧 Test", "Email de test envoyé (vérifie ta console)");
    }

    private String buildTaskEmail(String taskTitle, String taskDescription) {
        return "<h2>Nouvelle tâche: " + taskTitle + "</h2>" +
                "<p>" + taskDescription + "</p>" +
                "<p>Connectez-vous à l'application pour plus de détails.</p>";
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}