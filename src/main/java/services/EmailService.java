package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {
    private static EmailService instance;

    // Configuration Gmail (à modifier selon votre serveur SMTP)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "tahahamdouni11@gmail.com";
    private static final String EMAIL_PASSWORD = "nvrg otuh bpji nlnr";

    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public boolean sendPasswordResetEmail(String toEmail, String code) {
        String subject = "Réinitialisation de votre mot de passe - Stratix";
        String body = buildPasswordResetEmailBody(code);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Envoyer un code 2FA par email
     */
    public boolean send2FACode(String toEmail, String code) {
        String subject = "Code de vérification 2FA - Stratix";
        String body = build2FAEmailBody(code);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Envoyer un email de bienvenue
     */
    public boolean sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Bienvenue sur Stratix!";
        String body = buildWelcomeEmailBody(userName);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * ⭐ NOUVELLE MÉTHODE - Notification de tâche
     */
    public boolean sendTaskNotification(String toEmail, String taskTitle, String taskDescription) {
        String subject = "📋 Nouvelle tâche assignée - Stratix";
        String body = buildTaskEmailBody(taskTitle, taskDescription);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * ⭐ NOUVELLE MÉTHODE - Notification de planning
     */
    public boolean sendPlanningNotification(String toEmail, String shiftType, String date, String hours) {
        String subject = "📅 Nouveau planning - Stratix";
        String body = buildPlanningEmailBody(shiftType, date, hours);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * ⭐ NOUVELLE MÉTHODE - Notification de projet
     */
    public boolean sendProjectUpdate(String toEmail, String projectName, String message) {
        String subject = "📊 Mise à jour projet - " + projectName;
        String body = buildProjectEmailBody(projectName, message);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * ⭐ Version asynchrone pour ne pas bloquer l'interface
     */
    public CompletableFuture<Boolean> sendEmailAsync(String toEmail, String subject, String body) {
        return CompletableFuture.supplyAsync(() -> sendEmail(toEmail, subject, body));
    }

    /**
     * Méthode générique pour envoyer un email
     */
    private boolean sendEmail(String toEmail, String subject, String body) {
        try {
            // Configuration des propriétés
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Stratix"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // Envoyer
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ⭐ Template pour notification de tâche
     */
    private String buildTaskEmailBody(String taskTitle, String taskDescription) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".task-box { background-color: #F3F4F6; border-left: 4px solid #159895; padding: 20px; margin: 20px 0; }" +
                ".task-title { font-size: 18px; font-weight: bold; color: #1F2937; }" +
                ".task-desc { color: #6B7280; margin-top: 10px; }" +
                ".message { color: #6B7280; line-height: 1.6; margin: 20px 0; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>📋 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Nouvelle tâche assignée</h2>" +
                "<p class='message'>Une nouvelle tâche vous a été assignée :</p>" +
                "<div class='task-box'>" +
                "<div class='task-title'>" + taskTitle + "</div>" +
                "<div class='task-desc'>" + taskDescription + "</div>" +
                "</div>" +
                "<p class='message'>Connectez-vous à l'application pour plus de détails.</p>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * ⭐ Template pour notification de planning
     */
    private String buildPlanningEmailBody(String shiftType, String date, String hours) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".planning-box { background-color: #EEF2FF; border-left: 4px solid #159895; padding: 20px; margin: 20px 0; }" +
                ".shift { font-size: 18px; font-weight: bold; color: #1F2937; }" +
                ".info { color: #6B7280; margin-top: 10px; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>📅 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Nouveau planning enregistré</h2>" +
                "<div class='planning-box'>" +
                "<div class='shift'>Shift : " + shiftType + "</div>" +
                "<div class='info'>📅 Date : " + date + "</div>" +
                "<div class='info'>⏰ Horaires : " + hours + "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * ⭐ Template pour notification de projet
     */
    private String buildProjectEmailBody(String projectName, String message) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".message-box { background-color: #F3F4F6; border-radius: 8px; padding: 20px; margin: 20px 0; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>📊 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Mise à jour projet : " + projectName + "</h2>" +
                "<div class='message-box'>" +
                "<p style='color: #4B5563;'>" + message + "</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // Templates existants
    private String buildPasswordResetEmailBody(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".code-box { background-color: #F3F4F6; border: 2px dashed #159895; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }" +
                ".code { font-size: 32px; font-weight: bold; color: #159895; letter-spacing: 5px; }" +
                ".message { color: #6B7280; line-height: 1.6; margin: 20px 0; }" +
                ".warning { background-color: #FEF3C7; border-left: 4px solid #F59E0B; padding: 15px; margin: 20px 0; color: #92400E; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>🔐 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Réinitialisation de mot de passe</h2>" +
                "<p class='message'>Vous avez demandé à réinitialiser votre mot de passe. Utilisez le code ci-dessous pour continuer:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + code + "</div>" +
                "</div>" +
                "<p class='message'>Ce code est valide pendant <strong>1 heure</strong>.</p>" +
                "<div class='warning'>" +
                "⚠️ Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe restera inchangé." +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String build2FAEmailBody(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".code-box { background-color: #EEF2FF; border: 2px solid #159895; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }" +
                ".code { font-size: 36px; font-weight: bold; color: #159895; letter-spacing: 8px; }" +
                ".message { color: #6B7280; line-height: 1.6; margin: 20px 0; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>🔒 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Code de vérification</h2>" +
                "<p class='message'>Voici votre code de vérification pour vous connecter:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + code + "</div>" +
                "</div>" +
                "<p class='message'>Ce code est valide pour cette session uniquement.</p>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildWelcomeEmailBody(String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".logo { font-size: 32px; font-weight: bold; color: #159895; }" +
                ".title { font-size: 24px; color: #1F2937; margin: 20px 0; }" +
                ".message { color: #6B7280; line-height: 1.6; margin: 20px 0; }" +
                ".footer { text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo'>🎉 STRATIX</div>" +
                "</div>" +
                "<h2 class='title'>Bienvenue " + userName + "!</h2>" +
                "<p class='message'>Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter et profiter de toutes les fonctionnalités de Stratix.</p>" +
                "<p class='message'>Nous sommes ravis de vous compter parmi nous!</p>" +
                "<div class='footer'>" +
                "<p>© 2026 Stratix - Tous droits réservés</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}//