package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GroqAPI {

    // 🔥 TA NOUVELLE CLÉ API GROQ
    private static final String API_KEY = "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";

    // URL de l'API Groq
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String MODEL = "llama-3.3-70b-versatile";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Envoie un message à Groq et retourne la réponse
     */
    public static String sendMessage(String message) {
        try {
            System.out.println("📤 Envoi à Groq: " + message);

            // Créer le corps de la requête (format OpenAI)
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 1024);

            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Tu es un assistant IA spécialisé dans l'aide à la gestion de projet pour l'application Stratix. Réponds de manière concise et utile.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messages.add(userMessage);

            requestBody.add("messages", messages);

            String jsonBody = requestBody.toString();

            // Construire la requête
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(jsonBody, JSON))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Exécuter la requête
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    System.err.println("❌ Erreur API Groq: " + response.code());
                    System.err.println("📄 Réponse: " + responseBody);

                    if (response.code() == 401) {
                        return "❌ Clé API invalide. Vérifiez votre clé Groq.";
                    } else if (response.code() == 429) {
                        return "❌ Trop de requêtes. Veuillez patienter quelques instants.";
                    } else {
                        return getLocalResponse(message);
                    }
                }

                return extractTextFromResponse(responseBody);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur réseau: " + e.getMessage());
            e.printStackTrace();
            return getLocalResponse(message);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return getLocalResponse(message);
        }
    }

    /**
     * Extrait le texte de la réponse JSON de Groq
     */
    private static String extractTextFromResponse(String jsonResponse) {
        try {
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (responseObj.has("choices") && responseObj.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = responseObj.getAsJsonArray("choices").get(0).getAsJsonObject();

                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");

                    if (message.has("content")) {
                        return message.get("content").getAsString();
                    }
                }
            }

            return getLocalResponse("");

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON: " + e.getMessage());
            return getLocalResponse("");
        }
    }

    /**
     * Réponses locales de secours
     */
    private static String getLocalResponse(String message) {
        String lowerMsg = message.toLowerCase();

        if (lowerMsg.contains("bonjour") || lowerMsg.contains("salut") || lowerMsg.contains("hello")) {
            return "Bonjour ! Comment puis-je vous aider aujourd'hui ?";
        }
        else if (lowerMsg.contains("tâche") || lowerMsg.contains("tache")) {
            return "Les tâches sont gérées dans l'onglet 'Tâches'. Vous pouvez voir leur statut (À faire, En cours, Terminée) et leur priorité. Utilisez le Whiteboard pour un tableau Kanban interactif !";
        }
        else if (lowerMsg.contains("planning") || lowerMsg.contains("shift")) {
            return "Le planning affiche les shifts des employés. Consultez le calendrier pour voir les plannings par jour. Les types disponibles : JOUR (☀️), SOIR (🌙), NUIT (🌌), CONGÉ (🏖️), MALADIE (🤒), FORMATION (📚).";
        }
        else if (lowerMsg.contains("employé") || lowerMsg.contains("employe")) {
            return "Les employés sont listés dans la sidebar. Vous pouvez voir leurs emails et les plannings associés. Le dashboard affiche les statistiques : total, en poste, absents.";
        }
        else if (lowerMsg.contains("mot de passe") || lowerMsg.contains("password")) {
            return "Pour réinitialiser votre mot de passe, cliquez sur 'Mot de passe oublié' sur la page de connexion. Un email vous sera envoyé avec les instructions.";
        }
        else if (lowerMsg.contains("capitale") && lowerMsg.contains("france")) {
            return "La capitale de la France est Paris ! 🇫🇷";
        }
        else if (lowerMsg.contains("productivité") || lowerMsg.contains("productivite")) {
            return "Pour améliorer la productivité d'une équipe : 1. Fixez des objectifs clairs 2. Utilisez des outils comme Stratix 3. Communiquez régulièrement 4. Célébrez les réussites 5. Formez continuellement l'équipe.";
        }
        else {
            String[] responses = {
                    "Intéressant ! Pouvez-vous me donner plus de détails ?",
                    "Je vois. Dans Stratix, vous pouvez gérer cela via les différents modules.",
                    "Bonne question ! Je vous suggère de consulter la documentation ou d'explorer les fonctionnalités.",
                    "Je suis spécialisé dans l'aide à la gestion de projet avec Stratix. Que voulez-vous savoir exactement ?",
                    "Pouvez-vous reformuler votre question ? Je suis là pour vous aider avec les tâches, plannings et employés."
            };
            return responses[(int)(Math.random() * responses.length)];
        }
    }

    /**
     * Test de connexion simple
     */
    public static boolean testConnection() {
        try {
            String response = sendMessage("Dis 'OK' en un mot");
            System.out.println("📥 Réponse test: " + response);
            return response != null && !response.isEmpty() && !response.contains("❌");
        } catch (Exception e) {
            return false;
        }
    }
}//