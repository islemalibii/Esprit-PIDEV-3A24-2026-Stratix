package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EventEmailApi {

    public static void sendEmail(String recipientEmail, String eventTitle, String eventDate) {
        final String username = "islem.alibii@gmail.com";
        final String password = "vwpt wzyj jvrq mwah";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "StratiX Management"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Confirmation : " + eventTitle);

            String htmlContent = "<h3>Félicitations !</h3>"
                    + "<p>Vous êtes inscrit à l'événement : <b>" + eventTitle + "</b></p>"
                    + "<p>Date de l'événement : " + eventDate + "</p>"
                    + "<p style='margin-top: 20px; font-size: 12px; color: #888;'>Ceci est un message automatique de StratiX.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email sent from Strati X !");

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}