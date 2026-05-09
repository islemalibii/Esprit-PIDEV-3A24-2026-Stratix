package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Tache;
import models.Employe;
import services.SERVICETache;
import services.EmployeeService;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmpTacheController {

    @FXML private VBox cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> filterPrioriteCombo;
    @FXML private Label lblTotalTaches;
    @FXML private Label lblEnCours;
    @FXML private Label lblTerminees;
    @FXML private Label lblTachesCount;
    @FXML private Label lblWelcomeName;
    @FXML private Label lblWelcomeRole;

    private SERVICETache tacheService = new SERVICETache();
    private EmployeeService employeeService = new EmployeeService();
    private ObservableList<Tache> tacheList = FXCollections.observableArrayList();
    private FilteredList<Tache> filteredData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int currentUserId;
    private String currentUserName;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation EmpTacheController ===");

        // Récupérer les informations de l'employé connecté
        currentUserId = SessionManager.getInstance().getUserId();
        String userEmail = SessionManager.getInstance().getEmail();
        String userRole = SessionManager.getInstance().getRole();

        System.out.println("📌 ID utilisateur: " + currentUserId);
        System.out.println("📌 Email: " + userEmail);
        System.out.println("📌 Rôle: " + userRole);

        // Récupérer le nom de l'employé
        if (currentUserId > 0) {
            Employe employe = employeeService.getEmployeById(currentUserId);
            if (employe != null) {
                currentUserName = employe.getUsername();
                lblWelcomeName.setText(currentUserName);
                lblWelcomeRole.setText(userRole != null ? userRole : "Employé");
            }
        }

        // Initialiser les combos de filtres
        filterStatutCombo.getItems().addAll("Tous", "A_FAIRE", "EN_COURS", "TERMINEE");
        filterStatutCombo.setValue("Tous");

        filterPrioriteCombo.getItems().addAll("Tous", "HAUTE", "MOYENNE", "BASSE");
        filterPrioriteCombo.setValue("Tous");

        // Charger TOUTES les tâches (sans filtre)
        chargerToutesTaches();

        // Configurer la recherche et les filtres
        setupSearchAndFilters();
    }

    /**
     * Charge TOUTES les tâches de la base de données
     */
    private void chargerToutesTaches() {
        try {
            // Récupérer toutes les tâches
            List<Tache> toutesTaches = tacheService.getAllTaches();
            System.out.println("📋 Total tâches dans la base: " + toutesTaches.size());

            // Afficher les détails dans la console
            for (Tache t : toutesTaches) {
                System.out.println("   - Tâche ID: " + t.getId() +
                        ", Titre: '" + t.getTitre() + "'" +
                        ", Employé ID: " + t.getEmployeId() +
                        ", Statut: " + t.getStatut() +
                        ", Priorité: " + t.getPriorite());
            }

            // Mettre à jour la liste
            tacheList.setAll(toutesTaches);
            filteredData = new FilteredList<>(tacheList, p -> true);

            // Afficher les cartes
            afficherCartes(filteredData);

            // Mettre à jour le compteur
            lblTachesCount.setText(toutesTaches.size() + " tâche" + (toutesTaches.size() > 1 ? "s" : ""));

            // Calculer les statistiques
            calculerStatistiques(toutesTaches);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les tâches: " + e.getMessage());
        }
    }

    private void setupSearchAndFilters() {
        // Listener pour la recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterData(newVal, filterStatutCombo.getValue(), filterPrioriteCombo.getValue());
        });

        // Listener pour les filtres
        filterStatutCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterData(searchField.getText(), newVal, filterPrioriteCombo.getValue());
        });

        filterPrioriteCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterData(searchField.getText(), filterStatutCombo.getValue(), newVal);
        });
    }

    private void filterData(String searchText, String statutFilter, String prioriteFilter) {
        if (filteredData == null) return;

        filteredData.setPredicate(tache -> {
            // Filtre par statut
            if (statutFilter != null && !statutFilter.equals("Tous")) {
                if (!tache.getStatut().equals(statutFilter)) {
                    return false;
                }
            }

            // Filtre par priorité
            if (prioriteFilter != null && !prioriteFilter.equals("Tous")) {
                if (!tache.getPriorite().equals(prioriteFilter)) {
                    return false;
                }
            }

            // Filtre par recherche
            if (searchText != null && !searchText.isEmpty()) {
                String lowerSearch = searchText.toLowerCase();
                String titre = tache.getTitre() != null ? tache.getTitre().toLowerCase() : "";
                String description = tache.getDescription() != null ? tache.getDescription().toLowerCase() : "";

                return titre.contains(lowerSearch) || description.contains(lowerSearch);
            }

            return true;
        });

        afficherCartes(filteredData);
        calculerStatistiques(filteredData);
    }

    private void afficherCartes(FilteredList<Tache> taches) {
        cardsContainer.getChildren().clear();

        if (taches.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

            Label iconLabel = new Label("📋");
            iconLabel.setFont(Font.font("System", 48));

            Label messageLabel = new Label("Aucune tâche trouvée");
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            messageLabel.setTextFill(Color.web("#64748b"));

            Label subLabel = new Label("Modifiez vos critères de recherche");
            subLabel.setFont(Font.font("System", 14));
            subLabel.setTextFill(Color.web("#94a3b8"));

            emptyBox.getChildren().addAll(iconLabel, messageLabel, subLabel);
            cardsContainer.getChildren().add(emptyBox);
            return;
        }

        for (Tache t : taches) {
            cardsContainer.getChildren().add(createTacheCard(t));
        }
    }

    private HBox createTacheCard(Tache tache) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        card.setPrefHeight(100);
        card.setMaxWidth(Double.MAX_VALUE);

        // Indicateur de priorité (barre colorée à gauche)
        Region priorityIndicator = new Region();
        priorityIndicator.setPrefWidth(5);
        priorityIndicator.setPrefHeight(70);
        priorityIndicator.setStyle(getPriorityColor(tache.getPriorite()));

        // Icône selon le statut
        Label iconLabel = new Label(getStatusIcon(tache.getStatut()));
        iconLabel.setFont(Font.font("System", 28));
        iconLabel.setPrefWidth(50);
        iconLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Informations principales
        VBox infoBox = new VBox(8);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Titre et priorité
        HBox titleBox = new HBox(10);
        Label titleLabel = new Label(tache.getTitre() != null ? tache.getTitre() : "Sans titre");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0f172a"));

        Label priorityBadge = new Label(tache.getPriorite() != null ? tache.getPriorite() : "NON_DEFINIE");
        priorityBadge.setStyle(getPriorityBadgeStyle(tache.getPriorite()));
        priorityBadge.setPadding(new Insets(3, 12, 3, 12));
        priorityBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        titleBox.getChildren().addAll(titleLabel, priorityBadge);

        // Description
        String description = tache.getDescription() != null ? tache.getDescription() : "";
        Label descLabel = new Label(truncateText(description, 80));
        descLabel.setFont(Font.font("System", 13));
        descLabel.setTextFill(Color.web("#475569"));

        // Deadline, statut et employé
        HBox metaBox = new HBox(15);

        Label deadlineLabel = new Label("📅 " + formatDate(tache.getDeadline()));
        deadlineLabel.setFont(Font.font("System", 12));
        deadlineLabel.setTextFill(Color.web("#64748b"));

        Label statusBadge = new Label(getStatusText(tache.getStatut()));
        statusBadge.setStyle(getStatusBadgeStyle(tache.getStatut()));
        statusBadge.setPadding(new Insets(3, 12, 3, 12));
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Récupérer le nom de l'employé
        String employeNom = getEmployeName(tache.getEmployeId());
        Label employeLabel = new Label("👤 " + employeNom);
        employeLabel.setFont(Font.font("System", 12));
        employeLabel.setTextFill(Color.web("#64748b"));

        metaBox.getChildren().addAll(deadlineLabel, statusBadge, employeLabel);

        infoBox.getChildren().addAll(titleBox, descLabel, metaBox);

        card.getChildren().addAll(priorityIndicator, iconLabel, infoBox);

        // Animation au survol
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.15), 15, 0, 0, 5);");
            card.setTranslateX(2);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");
            card.setTranslateX(0);
        });

        return card;
    }

    private String getEmployeName(int employeId) {
        if (employeId <= 0) return "Non assigné";
        try {
            Employe emp = employeeService.getEmployeById(employeId);
            return emp != null ? emp.getUsername() : "Employé " + employeId;
        } catch (Exception e) {
            return "Employé " + employeId;
        }
    }

    private String getPriorityColor(String priorite) {
        if (priorite == null) return "-fx-background-color: #94a3b8; -fx-background-radius: 5;";
        switch (priorite) {
            case "HAUTE": return "-fx-background-color: #ef4444; -fx-background-radius: 5;";
            case "MOYENNE": return "-fx-background-color: #f59e0b; -fx-background-radius: 5;";
            case "BASSE": return "-fx-background-color: #10b981; -fx-background-radius: 5;";
            default: return "-fx-background-color: #94a3b8; -fx-background-radius: 5;";
        }
    }

    private String getPriorityBadgeStyle(String priorite) {
        if (priorite == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 20;";
        switch (priorite) {
            case "HAUTE": return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 20;";
            case "MOYENNE": return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 20;";
            case "BASSE": return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 20;";
            default: return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 20;";
        }
    }

    private String getStatusIcon(String statut) {
        if (statut == null) return "⏳";
        switch (statut) {
            case "A_FAIRE": return "⏳";
            case "EN_COURS": return "🔄";
            case "TERMINEE": return "✅";
            default: return "📋";
        }
    }

    private String getStatusText(String statut) {
        if (statut == null) return "À faire";
        switch (statut) {
            case "A_FAIRE": return "À faire";
            case "EN_COURS": return "En cours";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }

    private String getStatusBadgeStyle(String statut) {
        if (statut == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 20;";
        switch (statut) {
            case "A_FAIRE": return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 20;";
            case "EN_COURS": return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-background-radius: 20;";
            case "TERMINEE": return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 20;";
            default: return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 20;";
        }
    }

    private String formatDate(Object date) {
        if (date == null) return "Non définie";
        try {
            if (date instanceof java.sql.Date) {
                return ((java.sql.Date) date).toLocalDate().format(dateFormatter);
            }
        } catch (Exception e) {
            return "Date invalide";
        }
        return "Non définie";
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.isEmpty()) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private void calculerStatistiques(List<Tache> taches) {
        long total = taches.size();
        long enCours = taches.stream().filter(t -> "EN_COURS".equals(t.getStatut())).count();
        long terminees = taches.stream().filter(t -> "TERMINEE".equals(t.getStatut())).count();

        lblTotalTaches.setText(String.valueOf(total));
        lblEnCours.setText(String.valueOf(enCours));
        lblTerminees.setText(String.valueOf(terminees));
    }

    private void calculerStatistiques(FilteredList<Tache> taches) {
        long total = taches.size();
        long enCours = taches.stream().filter(t -> "EN_COURS".equals(t.getStatut())).count();
        long terminees = taches.stream().filter(t -> "TERMINEE".equals(t.getStatut())).count();

        lblTotalTaches.setText(String.valueOf(total));
        lblEnCours.setText(String.valueOf(enCours));
        lblTerminees.setText(String.valueOf(terminees));
        lblTachesCount.setText(total + " tâche" + (total > 1 ? "s" : ""));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showMonPlanning() {
        try {
            if (MainController.staticContentArea != null) {
                Node node = FXMLLoader.load(getClass().getResource("/EmpPlanningView.fxml"));
                MainController.staticContentArea.getChildren().setAll(node);
                System.out.println("🔄 Navigation vers Planning réussie");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue planning");
        }
    }

    @FXML
    private void refreshTaches() {
        chargerToutesTaches();
        searchField.clear();
        filterStatutCombo.setValue("Tous");
        filterPrioriteCombo.setValue("Tous");
    }
}//