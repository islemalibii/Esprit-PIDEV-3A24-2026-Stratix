package services;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;

import java.util.Random;

public class CaptchaService {
    private static CaptchaService instance;
    private String currentCaptchaText;
    private final Random random;
    
    // Caractères utilisés pour le CAPTCHA (sans caractères ambigus)
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CAPTCHA_LENGTH = 6;
    
    private CaptchaService() {
        this.random = new Random();
    }
    
    public static CaptchaService getInstance() {
        if (instance == null) {
            instance = new CaptchaService();
        }
        return instance;
    }
    
    /**
     * Générer un nouveau CAPTCHA sur un Canvas
     */
    public Canvas generateCaptchaCanvas() {
        // Générer le texte aléatoire
        currentCaptchaText = generateRandomText();
        
        // Créer le canvas (taille réduite)
        Canvas canvas = new Canvas(220, 60);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Fond avec dégradé
        for (int y = 0; y < 60; y++) {
            double gradient = (double) y / 60;
            gc.setFill(Color.rgb(
                (int)(240 + gradient * 15),
                (int)(245 + gradient * 10),
                (int)(250 + gradient * 5)
            ));
            gc.fillRect(0, y, 220, 1);
        }
        
        // Ajouter des lignes de distorsion
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            gc.setStroke(Color.rgb(
                random.nextInt(100) + 100,
                random.nextInt(100) + 100,
                random.nextInt(100) + 100,
                0.3
            ));
            
            int y1 = random.nextInt(60);
            int y2 = random.nextInt(60);
            
            gc.beginPath();
            gc.moveTo(0, y1);
            for (int x = 0; x < 220; x += 5) {
                double y = y1 + (y2 - y1) * Math.sin(x * 0.05) * 0.5;
                gc.lineTo(x, y);
            }
            gc.stroke();
        }
        
        // Dessiner le texte
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        int charWidth = 220 / (CAPTCHA_LENGTH + 1);
        
        for (int i = 0; i < currentCaptchaText.length(); i++) {
            char c = currentCaptchaText.charAt(i);
            int x = charWidth * (i + 1);
            int y = 40 + random.nextInt(8) - 4;
            
            // Rotation aléatoire
            double rotation = random.nextInt(30) - 15;
            
            // Couleur aléatoire
            gc.setFill(Color.rgb(
                random.nextInt(100),
                random.nextInt(100),
                random.nextInt(100)
            ));
            
            // Sauvegarder l'état
            gc.save();
            
            // Appliquer la rotation
            gc.translate(x, y);
            gc.rotate(rotation);
            
            // Dessiner le caractère
            gc.fillText(String.valueOf(c), 0, 0);
            
            // Restaurer l'état
            gc.restore();
        }
        
        // Ajouter du bruit (points)
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(220);
            int y = random.nextInt(60);
            gc.setFill(Color.rgb(
                random.nextInt(100) + 150,
                random.nextInt(100) + 150,
                random.nextInt(100) + 150
            ));
            gc.fillOval(x, y, 2, 2);
        }
        
        return canvas;
    }
    
    /**
     * Vérifier si le texte saisi correspond au CAPTCHA
     */
    public boolean verifyCaptcha(String userInput) {
        if (userInput == null || currentCaptchaText == null) {
            return false;
        }
        boolean isValid = userInput.trim().equalsIgnoreCase(currentCaptchaText);
        
        // Réinitialiser après vérification (pour éviter la réutilisation)
        if (isValid) {
            currentCaptchaText = null;
        }
        
        return isValid;
    }
    
    /**
     * Obtenir le texte du CAPTCHA actuel (pour debug uniquement)
     */
    public String getCurrentCaptchaText() {
        return currentCaptchaText;
    }
    
    /**
     * Générer un texte aléatoire
     */
    private String generateRandomText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = random.nextInt(CAPTCHA_CHARS.length());
            text.append(CAPTCHA_CHARS.charAt(index));
        }
        return text.toString();
    }
}
