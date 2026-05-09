package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import services.*;
import models.DashboardStats;
import models.Tache;
import models.Employe;
import api.QuoteAPI;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    // Labels principaux
    @FXML private Label lblTotalTaches;
    @FXML private Label lblEnCours;
    @FXML private Label lblTerminees;
    @FXML private Label lblEnRetard;
    @FXML private Label lblTotalEmployes;
    @FXML private Label lblEnPoste;
    @FXML private Label lblAbsents;
    @FXML private Label lblAFaire;

    // Mini indicateurs
    @FXML private Label lblTotalEmployesMini;
    @FXML private Label lblEnRetardMini;
    @FXML private Label lblProjetsActifs;
    @FXML private Label lblTauxCompletion;

    // Labels du graphique
    @FXML private Label lblAFaireGraph;
    @FXML private Label lblEnCoursGraph;
    @FXML private Label lblTermineesGraph;

    // Barres du graphique
    @FXML private Rectangle barAFaire;
    @FXML private Rectangle barEnCours;
    @FXML private Rectangle barTerminees;

    // Citation
    @FXML private Label lblCitation;
    @FXML private Button btnRefreshQuote;

    // Recherche
    @FXML private TextField searchField;
    @FXML private VBox searchResultsContainer;
    @FXML private ListView<String> searchResultsList;

    // ⭐ Widget IA
    @FXML private Label iaStatusLabel;
    @FXML private VBox chatWidgetContainer;
    @FXML private Label iaMessage;

    // ⭐ Bouton unique pour email
    @FXML private Button btnComposeEmail;

    private StatsService statsService;
    private SERVICETache tacheService;
    private EmployeeService employeService;
    private SERVICEPlanning planningService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALISATION DASHBOARD ===");

        statsService = new StatsService();
        tacheService = new SERVICETache();
        employeService = new EmployeeService();
        planningService = new SERVICEPlanning();

        chargerStatistiques();
        chargerCitation();

        // ⭐ Charger le widget IA
        loadChatWidget();

        // ⭐ Vérifier le statut de l'IA
        checkIAStatus();

        // ⭐ Initialiser le bouton email
        setupEmailButton();

        if (btnRefreshQuote != null) {
            btnRefreshQuote.setOnAction(e -> chargerCitation());
        }

        // Listener pour la recherche
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    searchResultsContainer.setVisible(false);
                    searchResultsContainer.setManaged(false);
                } else {
                    effectuerRecherche(newVal.trim().toLowerCase());
                }
            });
        }
    }

    /**
     * ⭐ Initialise le bouton email
     */
    private void setupEmailButton() {
        if (btnComposeEmail != null) {
            btnComposeEmail.setOnAction(e -> openEmailComposer());
        }
    }

    /**
     * ⭐ Ouvre le composeur d'email
     */
    @FXML
    private void openEmailComposer() {
        System.out.println("📧 Ouverture du composeur d'email");
        Stage stage = (Stage) btnComposeEmail.getScene().getWindow();
        EmailComposerDialog.show(stage);
    }

    /**
     * ⭐ Vérifie le statut de l'IA Groq
     */
    private void checkIAStatus() {
        new Thread(() -> {
            boolean isConnected = GroqAPI.testConnection();
            javafx.application.Platform.runLater(() -> {
                if (iaStatusLabel != null) {
                    if (isConnected) {
                        iaStatusLabel.setText("En ligne");
                        iaStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        iaStatusLabel.setText("Hors ligne");
                        iaStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            });
        }).start();
    }

    /**
     * ⭐ Charge le widget IA
     */
    private void loadChatWidget() {
        try {
            String[] paths = {
                    "/DashboardChatWidget.fxml",
                    "/dashboard-chat-widget.fxml"
            };

            FXMLLoader loader = null;
            for (String path : paths) {
                if (getClass().getResource(path) != null) {
                    loader = new FXMLLoader(getClass().getResource(path));
                    break;
                }
            }

            if (loader == null) {
                if (iaMessage != null) {
                    iaMessage.setText("❌ Widget IA non disponible");
                }
                return;
            }

            VBox chatWidget = loader.load();
            if (chatWidgetContainer != null) {
                chatWidgetContainer.getChildren().add(chatWidget);
                if (iaMessage != null) {
                    iaMessage.setVisible(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerStatistiques() {
        try {
            DashboardStats stats = statsService.getDashboardStats();

            if (lblTotalTaches != null) lblTotalTaches.setText(String.valueOf(stats.getTotalTaches()));
            if (lblEnCours != null) lblEnCours.setText(String.valueOf(stats.getTachesEnCours()));
            if (lblTerminees != null) lblTerminees.setText(String.valueOf(stats.getTachesTerminees()));
            if (lblEnRetard != null) lblEnRetard.setText(String.valueOf(stats.getTachesEnRetard()));
            if (lblTotalEmployes != null) lblTotalEmployes.setText(String.valueOf(stats.getTotalEmployes()));
            if (lblEnPoste != null) lblEnPoste.setText(String.valueOf(stats.getEmployesEnPoste()));
            if (lblAbsents != null) lblAbsents.setText(String.valueOf(stats.getEmployesAbsents()));
            if (lblAFaire != null) lblAFaire.setText(String.valueOf(stats.getTachesAFaire()));

            if (lblTotalEmployesMini != null) lblTotalEmployesMini.setText(String.valueOf(stats.getTotalEmployes()));
            if (lblEnRetardMini != null) lblEnRetardMini.setText(String.valueOf(stats.getTachesEnRetard()));
            if (lblProjetsActifs != null) lblProjetsActifs.setText("3");

            int total = stats.getTotalTaches();
            int terminees = stats.getTachesTerminees();
            int taux = total > 0 ? (terminees * 100 / total) : 0;
            if (lblTauxCompletion != null) lblTauxCompletion.setText(taux + "%");

            if (lblAFaireGraph != null) lblAFaireGraph.setText(String.valueOf(stats.getTachesAFaire()));
            if (lblEnCoursGraph != null) lblEnCoursGraph.setText(String.valueOf(stats.getTachesEnCours()));
            if (lblTermineesGraph != null) lblTermineesGraph.setText(String.valueOf(stats.getTachesTerminees()));

            int max = Math.max(stats.getTachesAFaire(),
                    Math.max(stats.getTachesEnCours(), stats.getTachesTerminees()));
            if (max > 0) {
                if (barAFaire != null) barAFaire.setHeight((stats.getTachesAFaire() * 100.0) / max);
                if (barEnCours != null) barEnCours.setHeight((stats.getTachesEnCours() * 100.0) / max);
                if (barTerminees != null) barTerminees.setHeight((stats.getTachesTerminees() * 100.0) / max);
            }

            System.out.println("✅ Dashboard mis à jour!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerCitation() {
        try {
            String citation = QuoteAPI.getRandomQuote();
            if (lblCitation != null) lblCitation.setText(citation);
        } catch (Exception e) {
            if (lblCitation != null) lblCitation.setText("“Le succès c'est d'aller d'échec en échec sans perdre son enthousiasme.” — Winston Churchill");
        }
    }

    private void effectuerRecherche(String recherche) {
        if (searchResultsList == null) return;

        searchResultsList.getItems().clear();

        List<Tache> taches = tacheService.getAllTaches();
        List<String> resultats = taches.stream()
                .filter(t -> t.getTitre().toLowerCase().contains(recherche))
                .map(t -> "📋 Tâche: " + t.getTitre())
                .collect(Collectors.toList());

        List<Employe> employes = employeService.getAllEmployes();
        resultats.addAll(employes.stream()
                .filter(e -> e.getUsername().toLowerCase().contains(recherche))
                .map(e -> "👤 Employé: " + e.getUsername())
                .collect(Collectors.toList()));

        if (!resultats.isEmpty()) {
            searchResultsList.getItems().addAll(resultats);
            if (searchResultsContainer != null) {
                searchResultsContainer.setVisible(true);
                searchResultsContainer.setManaged(true);
            }
        } else {
            searchResultsList.getItems().add("🔍 Aucun résultat");
            if (searchResultsContainer != null) {
                searchResultsContainer.setVisible(true);
                searchResultsContainer.setManaged(true);
            }
        }
    }

    @FXML
    private void openTaches() {
        MainController.showTachesFromDashboard();
    }

    @FXML
    private void openPlanning() {
        MainController.showPlanningFromDashboard();
    }

    @FXML
    private void openCalendar() {
        MainController.showCalendarFromDashboard();
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboardFromButton() { loadView("/dashboard-view.fxml"); }
    @FXML
    private void showPlanningFromButton() { loadView("/PlanningListeView.fxml"); }
    @FXML
    private void showTachesFromButton() { loadView("/TacheListeView.fxml"); }
    @FXML
    private void showCalendarFromButton() { loadView("/calendar-view.fxml"); }
    @FXML
    private void showWhiteboardFromButton() { loadView("/WhiteboardView.fxml"); }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().logout();
            Parent root = FXMLLoader.load(getClass().getResource("/PagePrincipaleView.fxml"));
            Stage stage = (Stage) lblTotalTaches.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//*