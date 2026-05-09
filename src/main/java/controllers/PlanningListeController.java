package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;

import models.Planning;
import models.Utilisateur;  // ← CHANGER: Utilisateur au lieu de Employe
import services.SERVICEPlanning;
import services.UtilisateurService;  // ← CHANGER
import api.WeatherAPI;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PlanningListeController implements Initializable {

    @FXML private VBox cardsContainer;
    @FXML private Label lblTotalPlannings;
    @FXML private Label lblTotalJour;
    @FXML private Label lblTotalSoir;
    @FXML private Label lblTotalNuit;
    @FXML private Label lblConges;
    @FXML private Label lblMaladie;
    @FXML private Label lblFormation;
    @FXML private Label lblAutre;
    @FXML private Label lblMeteo;
    @FXML private Label lblPlanningCount;
    @FXML private Label lblHeaderStats;

    private SERVICEPlanning planningService;
    private UtilisateurService utilisateurService;  // ← CHANGER
    private ObservableList<Planning> planningList = FXCollections.observableArrayList();
    private FilteredList<Planning> filteredData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== Initialisation PlanningListeController (Version Utilisateur) ===");

        planningService = new SERVICEPlanning();
        utilisateurService = UtilisateurService.getInstance();  // ← CHANGER

        chargerPlannings();
    }

    private void chargerPlannings() {
        List<Planning> tousPlannings = planningService.getAllPlannings();
        planningList.setAll(tousPlannings);
        filteredData = new FilteredList<>(planningList, p -> true);

        afficherCartes(filteredData);
        mettreAJourStatistiques(tousPlannings);

        lblPlanningCount.setText(tousPlannings.size() + " planning" + (tousPlannings.size() > 1 ? "s" : ""));
        lblHeaderStats.setText("Visualisation globale - " + tousPlannings.size() + " planning(s)");
        System.out.println("✅ " + tousPlannings.size() + " plannings chargés");
    }

    private void afficherCartes(FilteredList<Planning> plannings) {
        cardsContainer.getChildren().clear();

        if (plannings.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

            Label iconLabel = new Label("📅");
            iconLabel.setFont(Font.font("System", 36));

            Label messageLabel = new Label("Aucun planning trouvé");
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            messageLabel.setTextFill(javafx.scene.paint.Color.web("#64748b"));

            cardsContainer.getChildren().add(emptyBox);
            emptyBox.getChildren().addAll(iconLabel, messageLabel);
            return;
        }

        for (Planning p : plannings) {
            cardsContainer.getChildren().add(createPlanningCard(p));
        }
    }

    private String getEmployeName(int employeId) {
        try {
            Utilisateur u = utilisateurService.getById(employeId);
            if (u != null) {
                // ✅ Affiche "Prénom Nom" comme Symfony
                return u.getPrenom() + " " + u.getNom();
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement employé " + employeId + ": " + e.getMessage());
        }
        return "Employé #" + employeId;
    }

    private HBox createPlanningCard(Planning planning) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefHeight(80);
        card.setMaxWidth(Double.MAX_VALUE);

        // Indicateur de type (petite barre)
        Region typeIndicator = new Region();
        typeIndicator.setPrefWidth(4);
        typeIndicator.setPrefHeight(50);
        typeIndicator.setStyle(getTypeColor(planning.getTypeShift()));

        // Icône selon le type
        Label iconLabel = new Label(getTypeIcon(planning.getTypeShift()));
        iconLabel.setFont(Font.font("System", 20));
        iconLabel.setPrefWidth(35);
        iconLabel.setAlignment(Pos.CENTER);

        // Informations principales
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Employé et type
        HBox headerBox = new HBox(8);

        String employeName = getEmployeName(planning.getEmployeId());

        Label employeLabel = new Label(employeName);
        employeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        employeLabel.setTextFill(javafx.scene.paint.Color.web("#0f172a"));

        Label typeBadge = new Label(getDisplayType(planning.getTypeShift()));
        typeBadge.setStyle(getBadgeStyle(planning.getTypeShift()));
        typeBadge.setPadding(new Insets(2, 8, 2, 8));
        typeBadge.setFont(Font.font("System", FontWeight.BOLD, 10));

        headerBox.getChildren().addAll(employeLabel, typeBadge);

        // Date et heures
        HBox metaBox = new HBox(15);

        String dateStr = planning.getDate() != null ?
                planning.getDate().toLocalDate().format(dateFormatter) : "Date inconnue";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setFont(Font.font("System", 12));
        dateLabel.setTextFill(javafx.scene.paint.Color.web("#475569"));

        String debutStr = planning.getHeureDebut() != null ?
                planning.getHeureDebut().toLocalTime().format(timeFormatter) : "--:--";
        String finStr = planning.getHeureFin() != null ?
                planning.getHeureFin().toLocalTime().format(timeFormatter) : "--:--";
        Label timeLabel = new Label("⏰ " + debutStr + " - " + finStr);
        timeLabel.setFont(Font.font("System", 12));
        timeLabel.setTextFill(javafx.scene.paint.Color.web("#475569"));

        metaBox.getChildren().addAll(dateLabel, timeLabel);

        infoBox.getChildren().addAll(headerBox, metaBox);

        // Petits boutons d'action
        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8; -fx-background-radius: 3; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> modifierPlanning(planning));

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8; -fx-background-radius: 3; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> supprimerPlanning(planning));

        Button btnMeteo = new Button("🌤️");
        btnMeteo.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8; -fx-background-radius: 3; -fx-cursor: hand;");
        btnMeteo.setOnAction(e -> afficherMeteo(planning));

        actionBox.getChildren().addAll(btnEdit, btnDelete, btnMeteo);

        card.getChildren().addAll(typeIndicator, iconLabel, infoBox, actionBox);

        return card;
    }

    private String getDisplayType(String type) {
        if (type == null) return "AUTRE";
        switch(type) {
            case "JOUR": return "JOUR";
            case "SOIR": return "SOIR";
            case "NUIT": return "NUIT";
            case "CONGÉ": return "CONGÉ";
            case "MALADIE": return "MALADIE";
            case "FORMATION": return "FORMATION";
            default: return type.toUpperCase();
        }
    }

    private String getTypeColor(String type) {
        if (type == null) return "-fx-background-color: #94a3b8; -fx-background-radius: 2;";
        switch(type) {
            case "JOUR": return "-fx-background-color: #f6c23e; -fx-background-radius: 2;";
            case "SOIR": return "-fx-background-color: #3b82f6; -fx-background-radius: 2;";
            case "NUIT": return "-fx-background-color: #1e293b; -fx-background-radius: 2;";
            case "CONGÉ": return "-fx-background-color: #1cc88a; -fx-background-radius: 2;";
            case "MALADIE": return "-fx-background-color: #e74a3b; -fx-background-radius: 2;";
            case "FORMATION": return "-fx-background-color: #36b9cc; -fx-background-radius: 2;";
            default: return "-fx-background-color: #94a3b8; -fx-background-radius: 2;";
        }
    }

    private String getTypeIcon(String type) {
        if (type == null) return "📋";
        switch(type) {
            case "JOUR": return "☀️";
            case "SOIR": return "🌆";
            case "NUIT": return "🌙";
            case "CONGÉ": return "🌴";
            case "MALADIE": return "🤒";
            case "FORMATION": return "🎓";
            default: return "📋";
        }
    }

    private String getBadgeStyle(String type) {
        if (type == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12;";
        switch(type) {
            case "JOUR": return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 12;";
            case "SOIR": return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-background-radius: 12;";
            case "NUIT": return "-fx-background-color: #e2e8f0; -fx-text-fill: #0f172a; -fx-background-radius: 12;";
            case "CONGÉ": return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 12;";
            case "MALADIE": return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 12;";
            case "FORMATION": return "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-background-radius: 12;";
            default: return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12;";
        }
    }

    private void mettreAJourStatistiques(List<Planning> plannings) {
        int jour = 0, soir = 0, nuit = 0, conge = 0, maladie = 0, formation = 0, autre = 0;

        for (Planning p : plannings) {
            if (p.getTypeShift() == null) continue;
            switch(p.getTypeShift()) {
                case "JOUR": jour++; break;
                case "SOIR": soir++; break;
                case "NUIT": nuit++; break;
                case "CONGÉ": conge++; break;
                case "MALADIE": maladie++; break;
                case "FORMATION": formation++; break;
                default: autre++;
            }
        }

        lblTotalPlannings.setText(String.valueOf(plannings.size()));
        lblTotalJour.setText(String.valueOf(jour));
        lblTotalSoir.setText(String.valueOf(soir));
        lblTotalNuit.setText(String.valueOf(nuit));
        lblConges.setText(String.valueOf(conge));
        lblMaladie.setText(String.valueOf(maladie));
        lblFormation.setText(String.valueOf(formation));
        lblAutre.setText(String.valueOf(autre));
    }

    private void modifierPlanning(Planning planning) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlanningView.fxml"));
            Parent root = loader.load();

            PlanningController controller = loader.getController();
            controller.setPlanningToEdit(planning);

            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerPlanning(Planning planning) {
        String employeName = getEmployeName(planning.getEmployeId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce planning ?");
        confirm.setContentText("Planning du " + planning.getDate() + " pour " + employeName);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                planningService.deletePlanning(planning.getId());
                chargerPlannings();
            }
        });
    }

    private void afficherMeteo(Planning planning) {
        try {
            String date = planning.getDate().toString();
            String employeName = getEmployeName(planning.getEmployeId());
            String dateF = planning.getDate().toLocalDate().format(dateFormatter);
            String meteo = WeatherAPI.getWeatherForDate(date);
            lblMeteo.setText("🌤️ " + employeName + " le " + dateF + " : " + meteo);
        } catch (Exception e) {
            lblMeteo.setText("Sélectionnez un planning pour voir la météo");
        }
    }

    @FXML
    private void nouveauPlanning() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/PlanningView.fxml"));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les plannings en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("plannings_" + LocalDate.now() + ".pdf");

            File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());

            if (file != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("✅ PDF exporté avec succès !\n" + file.getName());
                alert.showAndWait();
            }
        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void exportToExcel() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les plannings en Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );
            fileChooser.setInitialFileName("plannings_" + LocalDate.now() + ".xlsx");

            File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());

            if (file != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("✅ Excel exporté avec succès !\n" + file.getName());
                alert.showAndWait();
            }
        } catch (Exception e) {
            showAlert("Erreur", "❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void showStatistics() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText(null);
        alert.setContentText("📊 Statistiques des plannings");
        alert.showAndWait();
    }

    @FXML private void showDashboardFromButton() { loadView("/dashboard-view.fxml"); }
    @FXML private void showPlanningFromButton() { loadView("/PlanningListeView.fxml"); }
    @FXML private void showTachesFromButton() { loadView("/TacheListeView.fxml"); }
    @FXML private void showCalendarFromButton() { loadView("/calendar-view.fxml"); }
    @FXML private void showWhiteboardFromButton() { loadView("/WhiteboardView.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            System.err.println("Navigation error: " + fxmlPath);
        }
    }

    @FXML
    private void retourFormulaire() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/PlanningView.fxml"));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}