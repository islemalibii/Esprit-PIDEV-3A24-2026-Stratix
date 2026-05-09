package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import models.Service;
import services.GroqService;
import services.ServiceService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AIAssistantController implements Initializable {

    @FXML private VBox chatContainer;
    @FXML private TextField questionField;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;
    @FXML private ScrollPane scrollPane;

    private GroqService aiService;
    private List<Service> services;

    private static final String API_KEY = "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";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            statusLabel.setText("📦 Chargement des services...");

            // Charger les services depuis la base de données
            ServiceService serviceService = new ServiceService();
            services = serviceService.afficherAll();

            aiService = new GroqService(API_KEY, services);

            statusLabel.setText("✅ " + services.size() + " services chargés - Assistant prêt");

            Platform.runLater(() -> {
                addAIMessage(" Bonjour ! Je suis votre assistant .\n\n" +
                        "J'ai analysé " + services.size() + " services. " +
                        "Vous pouvez me poser des questions comme :\n\n" +
                        "\"Quel est le plus gros budget ?\"\n" +
                        "\"Combien de formations ?\"\n" +
                        "\"Services sans responsable\"\n" +
                        " \"Budget total des services\"\n" +
                        "  Détails du service X\"");
            });

            // Configuration des boutons
            sendButton.setOnAction(e -> askQuestion());
            questionField.setOnAction(e -> askQuestion());

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur de chargement");
            addAIMessage("Désolé, je n'ai pas pu charger les services : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void askQuestion() {
        String question = questionField.getText().trim();
        if (question.isEmpty()) return;

        addUserMessage(question);
        questionField.clear();

        sendButton.setDisable(true);
        statusLabel.setText("🤔 Réflexion en cours...");

        new Thread(() -> {
            try {
                // Essayer l'API Groq
                String reponse = aiService.ask(question);

                Platform.runLater(() -> {
                    addAIMessage(reponse);
                    sendButton.setDisable(false);
                    statusLabel.setText("✅ Prêt");
                });

            } catch (Exception e) {
                // Fallback sur la version locale
                String localResponse = getIntelligentLocalResponse(question);

                Platform.runLater(() -> {
                    addAIMessage("🤖 (Mode local) " + localResponse);
                    sendButton.setDisable(false);
                    statusLabel.setText("✅ Prêt (mode local)");
                });
            }
        }).start();
    }

    private String getIntelligentLocalResponse(String question) {
        String q = question.toLowerCase().trim();

        // Salutations
        if (q.contains("bonjour") || q.contains("hello") || q.contains("salut") || q.contains("hi")) {
            return "Bonjour ! Comment puis-je vous aider avec vos services ?";
        }

        // Budget total
        if (q.contains("budget") && (q.contains("total") || q.contains("somme"))) {
            double total = services.stream()
                    .mapToDouble(Service::getBudget)
                    .sum();
            return String.format("💰 Le budget total de tous les services est de *%.0f DT*.", total);
        }

        // Plus gros budget
        if ((q.contains("plus") || q.contains("max") || q.contains("grand")) && q.contains("budget")) {
            Service max = services.stream()
                    .max((a, b) -> Double.compare(a.getBudget(), b.getBudget()))
                    .orElse(null);

            if (max != null) {
                return String.format("🏆 Le service avec le plus gros budget est *%s* avec *%.0f DT* (Catégorie: %s)",
                        max.getTitre(), max.getBudget(),
                        max.getCategorie() != null ? max.getCategorie().getNom() : "Non catégorisé");
            }
        }

        // Nombre de formations
        if (q.contains("formation") || q.contains("formations")) {
            long count = services.stream()
                    .filter(s -> s.getTitre().toLowerCase().contains("formation") ||
                            (s.getDescription() != null && s.getDescription().toLowerCase().contains("formation")))
                    .count();

            if (count > 0) {
                return String.format("🎓 Vous avez *%d* service(s) de formation.", count);
            } else {
                return "📚 Vous n'avez aucun service de formation pour le moment.";
            }
        }

        // Services sans responsable
        if ((q.contains("sans") || q.contains("pas")) && (q.contains("responsable") || q.contains("assigné"))) {
            List<Service> without = services.stream()
                    .filter(s -> s.getUtilisateurId() <= 0)
                    .collect(Collectors.toList());

            if (without.isEmpty()) {
                return "✅ Tous les services ont un responsable assigné.";
            } else {
                StringBuilder sb = new StringBuilder("⚠️ Services sans responsable (*" + without.size() + "*) :\n\n");
                for (Service s : without) {
                    sb.append("• ").append(s.getTitre()).append("\n");
                }
                return sb.toString();
            }
        }

        // Budget moyen
        if (q.contains("moyen") || q.contains("moyenne")) {
            double avg = services.stream()
                    .mapToDouble(Service::getBudget)
                    .average()
                    .orElse(0);
            return String.format("📊 Le budget moyen par service est de *%.0f DT*.", avg);
        }

        // Liste des services
        if (q.contains("liste") || q.contains("tous les services")) {
            StringBuilder sb = new StringBuilder("📋 *Liste des " + services.size() + " services* :\n\n");
            for (Service s : services) {
                sb.append("• ").append(s.getTitre()).append(" (").append(s.getBudget()).append(" DT)\n");
            }
            return sb.toString();
        }

        // Détails d'un service spécifique
        if (q.contains("détail") || q.contains("details") || q.contains("info")) {
            for (Service s : services) {
                if (q.contains(s.getTitre().toLowerCase())) {
                    return String.format(
                            "📋 **%s**\n" +
                                    "   💰 Budget: **%.0f DT**\n" +
                                    "   📅 Période: %s → %s\n" +
                                    "   🏷️ Catégorie: %s\n" +
                                    "   👤 Responsable: %s",
                            s.getTitre(),
                            s.getBudget(),
                            s.getDateDebut() != null ? s.getDateDebut() : "Non définie",
                            s.getDateFin() != null ? s.getDateFin() : "Non définie",
                            s.getCategorie() != null ? s.getCategorie().getNom() : "Non catégorisé",
                            s.getUtilisateurId() > 0 ? "ID " + s.getUtilisateurId() : "Non assigné"
                    );
                }
            }
            return "Pour voir les détails d'un service, tapez par exemple : 'détails du service Formation Java'";
        }

        // Aide par défaut
        return "Je peux vous aider avec :\n" +
                "• 'Quel est le plus gros budget ?'\n" +
                "• 'Budget total'\n" +
                "• 'Combien de formations ?'\n" +
                "• 'Services sans responsable'\n" +
                "• 'Budget moyen'\n" +
                "• 'Liste des services'\n" +
                "• 'Détails du service [nom]'";
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-background-color: #1a73e8; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 15 15 5 15; " +
                        "-fx-padding: 10 15; " +
                        "-fx-wrap-text: true; " +
                        "-fx-font-size: 14px;"
        );
        messageLabel.setMaxWidth(300);
        messageLabel.setWrapText(true);

        messageBox.getChildren().add(messageLabel);

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            // Petit délai pour que le scroll se fasse après l'ajout
            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(e -> scrollToBottom());
            pause.play();
        });
    }

    private void addAIMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));
        messageBox.setSpacing(10);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label prefix = new Label("🤖");
        prefix.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-padding: 0 5 0 0;"
        );

        // Conteneur pour le message avec fond
        VBox messageContainer = new VBox();
        messageContainer.setMaxWidth(350);

        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-background-color: #f1f3f4; " +
                        "-fx-text-fill: #202124; " +
                        "-fx-background-radius: 15 15 15 5; " +
                        "-fx-padding: 12 15; " +
                        "-fx-wrap-text: true; " +
                        "-fx-font-size: 14px;"
        );
        messageLabel.setWrapText(true);

        messageContainer.getChildren().add(messageLabel);
        messageBox.getChildren().addAll(prefix, messageContainer);

        Platform.runLater(() -> {
            chatContainer.getChildren().add(messageBox);
            // Petit délai pour que le scroll se fasse après l'ajout
            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(e -> scrollToBottom());
            pause.play();
        });
    }

    private void scrollToBottom() {
        if (scrollPane != null) {
            // Scroll progressif pour voir le dernier message
            scrollPane.setVvalue(1.0);

            // Force le focus sur le scrollPane pour s'assurer que le scroll est pris en compte
            scrollPane.requestFocus();

            // Deuxième tentative après un court délai
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> scrollPane.setVvalue(1.0));
            pause.play();
        }
    }
}