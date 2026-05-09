package services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SendGridService {

    private static SendGridService instance;

    // 🔑 TA CLÉ API SENDGRID - À CONFIGURER
    private static final String SENDGRID_API_KEY = "SG.xxxxx"; // Remplace par ta clé API

    // 📧 NOUVEAU TEMPLATE ID
    private static final String TEMPLATE_ID = "d-499e7b2342134d4bb07c7cfe7e51b543";

    // 📧 Email expéditeur par défaut
    private static final String FROM_EMAIL = "noreply@stratix.com";
    private static final String FROM_NAME = "Stratix";

    private SendGrid sendGrid;

    private SendGridService() {
        this.sendGrid = new SendGrid(SENDGRID_API_KEY);
    }

    public static SendGridService getInstance() {
        if (instance == null) {
            instance = new SendGridService();
        }
        return instance;
    }

    /**
     * Envoie un email simple avec SendGrid
     */
    public CompletableFuture<Boolean> sendEmail(String toEmail, String subject, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Email from = new Email(FROM_EMAIL, FROM_NAME);
                Email to = new Email(toEmail);
                Content emailContent = new Content("text/html", content);
                Mail mail = new Mail(from, subject, to, emailContent);

                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sendGrid.api(request);

                boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;

                if (success) {
                    System.out.println("✅ Email envoyé à " + toEmail + " - Status: " + response.getStatusCode());
                } else {
                    System.err.println("❌ Échec envoi email - Status: " + response.getStatusCode());
                    System.err.println("📄 Réponse: " + response.getBody());
                }

                return success;

            } catch (IOException e) {
                System.err.println("❌ Erreur envoi email: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Envoie un email avec template SendGrid (utilise le nouveau template ID)
     */
    public CompletableFuture<Boolean> sendTemplateEmail(String toEmail, String templateData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Mail mail = new Mail();
                mail.setFrom(new Email(FROM_EMAIL, FROM_NAME));
                mail.setTemplateId(TEMPLATE_ID); // ← Nouveau template ID

                Personalization personalization = new Personalization();
                personalization.addTo(new Email(toEmail));

                // Ajouter les données dynamiques au template
                personalization.addDynamicTemplateData("name", extractNameFromEmail(toEmail));
                personalization.addDynamicTemplateData("date", java.time.LocalDate.now().toString());
                personalization.addDynamicTemplateData("content", templateData);

                mail.addPersonalization(personalization);

                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sendGrid.api(request);

                boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;

                if (success) {
                    System.out.println("✅ Email template envoyé à " + toEmail);
                } else {
                    System.err.println("❌ Échec envoi template - Status: " + response.getStatusCode());
                }

                return success;

            } catch (IOException e) {
                System.err.println("❌ Erreur envoi template: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Envoie une notification de nouvelle tâche
     */
    public CompletableFuture<Boolean> sendTaskNotification(String toEmail, String taskTitle, String taskDescription) {
        String subject = "📋 Nouvelle tâche assignée - Stratix";
        String content = buildTaskEmail(taskTitle, taskDescription);
        return sendEmail(toEmail, subject, content);
    }

    /**
     * Envoie une notification de nouveau planning
     */
    public CompletableFuture<Boolean> sendPlanningNotification(String toEmail, String shiftType, String date, String hours) {
        String subject = "📅 Nouveau planning - Stratix";
        String content = buildPlanningEmail(shiftType, date, hours);
        return sendEmail(toEmail, subject, content);
    }

    /**
     * Envoie une notification de mise à jour de projet
     */
    public CompletableFuture<Boolean> sendProjectUpdate(String toEmail, String projectName, String updateMessage) {
        String subject = "📊 Mise à jour projet - " + projectName;
        String content = buildProjectEmail(projectName, updateMessage);
        return sendEmail(toEmail, subject, content);
    }

    /**
     * Envoie un rapport hebdomadaire avec le template
     */
    public CompletableFuture<Boolean> sendWeeklyReport(String toEmail, String reportData) {
        return sendTemplateEmail(toEmail, reportData);
    }

    /**
     * Test de connexion à SendGrid
     */
    public boolean testConnection() {
        try {
            Request request = new Request();
            request.setMethod(Method.GET);
            request.setEndpoint("marketing/field_definitions");

            Response response = sendGrid.api(request);
            return response.getStatusCode() == 200;

        } catch (IOException e) {
            System.err.println("❌ Erreur test connexion SendGrid: " + e.getMessage());
            return false;
        }
    }

    // ==================== CONSTRUCTEURS D'EMAILS ====================

    private String buildTaskEmail(String taskTitle, String taskDescription) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".task { border-left: 4px solid #667eea; padding: 15px; margin: 20px 0; background: white; }" +
                ".button { background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block; }" +
                "</style></head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1 style='margin:0;'>📋 Stratix</h1>" +
                "<p>Nouvelle tâche assignée</p>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='task'>" +
                "<h2 style='margin:0 0 10px 0;'>" + taskTitle + "</h2>" +
                "<p style='color: #4a5568;'>" + taskDescription + "</p>" +
                "</div>" +
                "<p style='text-align: center;'>" +
                "<a href='http://localhost:8080' class='button'>Voir la tâche</a>" +
                "</p>" +
                "<p style='color: #718096; font-size: 12px; text-align: center;'>Stratix - Gestion de projet intelligente</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildPlanningEmail(String shiftType, String date, String hours) {
        String shiftColor;
        switch(shiftType) {
            case "JOUR": shiftColor = "#f6c23e"; break;
            case "SOIR": shiftColor = "#3b82f6"; break;
            case "NUIT": shiftColor = "#1e293b"; break;
            case "CONGE": shiftColor = "#1cc88a"; break;
            case "MALADIE": shiftColor = "#e74a3b"; break;
            case "FORMATION": shiftColor = "#36b9cc"; break;
            default: shiftColor = "#64748b"; break;
        }

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".planning { border-left: 4px solid " + shiftColor + "; padding: 15px; margin: 20px 0; background: white; }" +
                ".info { margin: 5px 0; color: #4a5568; }" +
                "</style></head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1 style='margin:0;'>📅 Stratix</h1>" +
                "<p>Nouveau planning enregistré</p>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='planning'>" +
                "<h2 style='margin:0 0 10px 0;'>Shift : " + shiftType + "</h2>" +
                "<p class='info'>📅 Date : <strong>" + date + "</strong></p>" +
                "<p class='info'>⏰ Horaires : <strong>" + hours + "</strong></p>" +
                "</div>" +
                "<p style='color: #718096; font-size: 12px; text-align: center;'>Stratix - Gestion de projet intelligente</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildProjectEmail(String projectName, String message) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".message { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                "</style></head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1 style='margin:0;'>📊 Stratix</h1>" +
                "<p>Mise à jour projet</p>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>" + projectName + "</h2>" +
                "<div class='message'>" +
                "<p>" + message + "</p>" +
                "</div>" +
                "<p style='color: #718096; font-size: 12px; text-align: center;'>Stratix - Gestion de projet intelligente</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) return "Utilisateur";
        return email.substring(0, email.indexOf('@'));
    }
}//*