package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Service;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GroqService {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final OkHttpClient client;
    private final String apiKey;
    private List<Service> services;

    public GroqService(String apiKey, List<Service> services) {
        this.apiKey = apiKey;
        this.services = services;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String ask(String question) {
        String contexte = construireContexte();

        String prompt = "Tu es un assistant spécialisé dans la gestion de services. " +
                "Voici les données actuelles des services au format JSON :\n\n" + contexte +
                "\n\nRéponds à cette question en français de façon naturelle et utile : " + question;

        try {
            return appelerGroq(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur : " + e.getMessage();
        }
    }

    private String construireContexte() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < services.size(); i++) {
            Service s = services.get(i);
            sb.append("  {\n");
            sb.append("    \"titre\": \"").append(s.getTitre()).append("\",\n");
            sb.append("    \"budget\": ").append(s.getBudget()).append(",\n");
            sb.append("    \"categorie\": \"").append(s.getCategorie() != null ?
                    s.getCategorie().getNom() : "Non catégorisé").append("\",\n");
            sb.append("    \"date_debut\": \"").append(s.getDateDebut()).append("\",\n");
            sb.append("    \"date_fin\": \"").append(s.getDateFin()).append("\",\n");
            sb.append("    \"responsable_id\": ").append(s.getUtilisateurId() > 0 ? s.getUtilisateurId() : "null").append("\n");
            sb.append("  }");
            if (i < services.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    private String appelerGroq(String prompt) throws IOException {
        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un assistant IA spécialisé dans l'analyse de données. Réponds toujours en français.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        JsonObject body = new JsonObject();

        // Modèle Llama 3.3 70B sur Groq (ultra rapide)
        body.addProperty("model", "llama-3.3-70b-versatile");
        body.add("messages", messages);
        body.addProperty("max_tokens", 500);
        body.addProperty("temperature", 0.7);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erreur API: " + response.code() + " - " + errorBody);
            }

            String jsonResponse = response.body().string();
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }
}