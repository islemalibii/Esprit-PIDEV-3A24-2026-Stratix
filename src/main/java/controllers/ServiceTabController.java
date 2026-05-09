package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Service;
import models.UserRole;
import services.PDFService;
import services.ServiceService;
import services.ExchangeRateService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ServiceTabController implements Initializable {

    @FXML private Button btnServicesView;
    @FXML private Button btnStatsView;
    @FXML private StackPane contentPane;
    @FXML private VBox servicesListView;
    @FXML private VBox statisticsView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private GridPane servicesGrid;
    @FXML private Button btnArchives;
    @FXML private Button btnRetour;
    @FXML private Button btnAjouter;
    @FXML private Button btnExporterPDF;
    @FXML private HBox actionButtonsBar;
    @FXML private TextField txtMontantDT;
    @FXML private TextField txtMontantUSD;
    @FXML private TextField txtMontantEUR;
    @FXML private Button btnVisio;

    // 🤖 Bulle flottante
    @FXML private StackPane assistantBubble;
    private Stage assistantStage;
    private boolean assistantVisible = false;

    private ServiceService serviceService;
    private ExchangeRateService exchangeRateService;
    private List<Service> allServices;
    private boolean modeArchive = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            serviceService = new ServiceService();
            exchangeRateService = new ExchangeRateService();

            filterType.getItems().addAll("Tous", "Développement", "Formation", "Maintenance", "Conseil", "Support");
            filterType.setValue("Tous");
            chargerDonnees();

            showServicesList();

            applyRoleRestrictions();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void applyRoleRestrictions() {
        if (UserRole.getInstance().isEmployee()) {
            if (btnAjouter != null) {
                btnAjouter.setVisible(false);
                btnAjouter.setManaged(false);
            }
            if (btnArchives != null) {
                btnArchives.setVisible(false);
                btnArchives.setManaged(false);
            }
            if (btnExporterPDF != null) {
                btnExporterPDF.setVisible(false);
                btnExporterPDF.setManaged(false);
            }
            if (btnVisio != null) {
                btnVisio.setVisible(false);
                btnVisio.setManaged(false);
            }
            if (actionButtonsBar != null) {
                actionButtonsBar.setVisible(false);
                actionButtonsBar.setManaged(false);
            }
        }
    }

    @FXML
    private void toggleAssistant() {
        if (!assistantVisible) {
            openAssistantAsPopup();
        } else {
            closeAssistant();
        }
    }

    private void openAssistantAsPopup() {
        try {
            if (assistantStage == null || !assistantStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ai-assistant-view.fxml"));
                Parent root = loader.load();

                assistantStage = new Stage();
                assistantStage.initStyle(StageStyle.UTILITY); // Fenêtre sans décoration
                assistantStage.setTitle("Assistant IA");

                Scene scene = new Scene(root, 350, 500);
                assistantStage.setScene(scene);

                // Positionner la fenêtre à côté de la bulle
                positionAssistantWindow();

                // Quand la fenêtre est fermée, mettre à jour l'état
                assistantStage.setOnCloseRequest(e -> {
                    assistantVisible = false;
                });

                assistantStage.show();
                assistantVisible = true;
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'assistant: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeAssistant() {
        if (assistantStage != null && assistantStage.isShowing()) {
            assistantStage.close();
            assistantVisible = false;
        }
    }

    private void positionAssistantWindow() {
        if (assistantStage != null && assistantBubble != null) {
            // Récupérer la position de la bulle par rapport à l'écran
            Point2D screenPos = assistantBubble.localToScreen(0, 0);

            if (screenPos != null) {
                // Positionner la fenêtre de l'assistant à gauche de la bulle
                assistantStage.setX(screenPos.getX() - 370); // 350 + marge
                assistantStage.setY(screenPos.getY() - 450); // 500 - hauteur bulle
            }
        }
    }

    // ✅ Méthode openVisio existante
    @FXML
    private void openVisio() {
        if (!UserRole.getInstance().isAdmin()) {
            showAlert("Accès refusé", "Seuls les administrateurs peuvent accéder à la visioconférence.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/visio-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("stratiX - Visioconférence");

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.centerOnScreen();

            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(600);

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la visio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showServicesList() {
        servicesListView.setVisible(true);
        servicesListView.setManaged(true);
        statisticsView.setVisible(false);
        statisticsView.setManaged(false);

        btnServicesView.getStyleClass().remove("sub-nav-button");
        btnServicesView.getStyleClass().add("sub-nav-button-active");
        btnStatsView.getStyleClass().remove("sub-nav-button-active");
        btnStatsView.getStyleClass().add("sub-nav-button");
    }

    @FXML
    private void showStatistics() {
        servicesListView.setVisible(false);
        servicesListView.setManaged(false);
        statisticsView.setVisible(true);
        statisticsView.setManaged(true);

        btnStatsView.getStyleClass().remove("sub-nav-button");
        btnStatsView.getStyleClass().add("sub-nav-button-active");
        btnServicesView.getStyleClass().remove("sub-nav-button-active");
        btnServicesView.getStyleClass().add("sub-nav-button");
    }

    @FXML
    private void handleConvertir() {
        try {
            String montantText = txtMontantDT.getText().trim();
            if (montantText.isEmpty()) {
                showAlert("Erreur", "Veuillez saisir un montant en DT");
                return;
            }

            double montantDT = Double.parseDouble(montantText);
            Map<String, Double> conversions = exchangeRateService.convertirToutesDevises(montantDT);

            txtMontantUSD.setText(String.format("%.2f", conversions.get("USD")));
            txtMontantEUR.setText(String.format("%.2f", conversions.get("EUR")));

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez saisir un nombre valide");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur de conversion: " + e.getMessage());
        }
    }

    private void afficherCartes(List<Service> services) {
        servicesGrid.getChildren().clear();
        servicesGrid.getColumnConstraints().clear();

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            servicesGrid.getColumnConstraints().add(col);
        }

        int col = 0;
        int row = 0;

        for (Service s : services) {
            VBox card = new VBox(12);
            card.getStyleClass().add("service-card");
            card.setMaxWidth(Double.MAX_VALUE);
            card.setPrefWidth(Region.USE_COMPUTED_SIZE);

            // Header
            HBox header = new HBox(10);
            header.getStyleClass().add("card-header");
            header.setAlignment(Pos.CENTER_LEFT);

            Label titre = new Label(s.getTitre());
            titre.getStyleClass().add("card-title");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String catName = (s.getCategorie() != null) ? s.getCategorie().getNom() : "Non catégorisé";
            Label categorie = new Label(catName);
            categorie.getStyleClass().add("card-category");

            header.getChildren().addAll(titre, spacer, categorie);

            // Content
            VBox content = new VBox(8);
            content.getStyleClass().add("card-content");

            HBox budgetRow = new HBox(10);
            budgetRow.getStyleClass().add("card-row");
            budgetRow.setAlignment(Pos.CENTER_LEFT);

            Label budgetLabel = new Label("💰 Budget:");
            budgetLabel.getStyleClass().add("card-label");
            Label budgetValue = new Label(String.format("%,.0f DT", s.getBudget()));
            budgetValue.getStyleClass().add("card-budget");
            budgetRow.getChildren().addAll(budgetLabel, budgetValue);

            HBox periodRow = new HBox(10);
            periodRow.getStyleClass().add("card-row");
            periodRow.setAlignment(Pos.CENTER_LEFT);

            Label periodLabel = new Label("📅 Période:");
            periodLabel.getStyleClass().add("card-label");
            Label periodValue = new Label(s.getDateDebut() + " → " + s.getDateFin());
            periodValue.getStyleClass().add("card-value");
            periodRow.getChildren().addAll(periodLabel, periodValue);

            HBox respRow = new HBox(10);
            respRow.getStyleClass().add("card-row");
            respRow.setAlignment(Pos.CENTER_LEFT);

            Label respLabel = new Label("👤 Responsable:");
            respLabel.getStyleClass().add("card-label");

            String responsableNom = "Non assigné";
            if (s.getUtilisateurId() > 0) {
                try {
                    responsableNom = serviceService.getResponsableNom(s.getUtilisateurId());
                } catch (SQLException e) {
                    responsableNom = "Erreur";
                }
            }

            Label respValue = new Label(responsableNom);
            respValue.getStyleClass().add("card-responsable");
            respRow.getChildren().addAll(respLabel, respValue);

            content.getChildren().addAll(budgetRow, periodRow, respRow);

            // Actions selon le rôle
            HBox actions = new HBox(10);
            actions.getStyleClass().add("card-actions");
            actions.setAlignment(Pos.CENTER_RIGHT);

            if (UserRole.getInstance().isAdmin()) {
                if (modeArchive) {
                    Button btnDesarchiver = new Button("Restaurer");
                    btnDesarchiver.getStyleClass().add("card-button-restore");
                    btnDesarchiver.setOnAction(e -> desarchiverService(s));
                    actions.getChildren().add(btnDesarchiver);
                } else {
                    Button btnArchiver = new Button("Archiver");
                    btnArchiver.getStyleClass().add("card-button-archive");
                    btnArchiver.setOnAction(e -> archiverService(s));

                    Button btnModifier = new Button("Modifier");
                    btnModifier.getStyleClass().add("card-button-modify");
                    btnModifier.setOnAction(e -> ouvrirModification(s));

                    actions.getChildren().addAll(btnArchiver, btnModifier);
                }
            } else {
                Label consultationLabel = new Label("🔍 Consultation seule");
                consultationLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                actions.getChildren().add(consultationLabel);
            }

            card.getChildren().addAll(header, content, actions);
            servicesGrid.add(card, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private void archiverService(Service service) {
        if (!UserRole.getInstance().isAdmin()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Archiver ce service ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                serviceService.archiver(service.getId());
                chargerDonnees();
                showAlert("Succès", "Service archivé!");
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private void desarchiverService(Service service) {
        if (!UserRole.getInstance().isAdmin()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Restaurer ce service ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                serviceService.desarchiver(service.getId());
                chargerDonnees();
                showAlert("Succès", "Service restauré!");
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private void ouvrirModification(Service service) {
        if (!UserRole.getInstance().isAdmin()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajout-service.fxml"));
            Parent root = loader.load();
            AjoutServiceController controller = loader.getController();
            controller.setServiceService(serviceService);
            controller.setServiceAModifier(service);
            Stage stage = new Stage();
            stage.setTitle("Modifier Service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDonnees();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!UserRole.getInstance().isAdmin()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajout-service.fxml"));
            Parent root = loader.load();
            AjoutServiceController controller = loader.getController();
            controller.setServiceService(serviceService);
            Stage stage = new Stage();
            stage.setTitle("Ajouter Service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDonnees();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleExporterPDF() {
        if (!UserRole.getInstance().isAdmin()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les services");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("rapport_services_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(servicesGrid.getScene().getWindow());

        if (file != null) {
            try {
                PDFService.exporterServices(allServices, file.getAbsolutePath());
                showAlert("Succès", "Rapport PDF exporté avec succès !\n" + file.getName());
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'exporter le PDF: " + e.getMessage());
            }
        }
    }

    private void chargerDonnees() {
        try {
            if (modeArchive) {
                allServices = serviceService.afficherArchives();
            } else {
                allServices = serviceService.afficherAll();
            }
            afficherCartes(allServices);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleVoirArchives() {
        if (!UserRole.getInstance().isAdmin()) return;
        modeArchive = true;
        chargerDonnees();
        btnArchives.setVisible(false);
        btnRetour.setVisible(true);
    }

    @FXML
    private void handleRetour() {
        modeArchive = false;
        chargerDonnees();
        btnArchives.setVisible(true);
        btnRetour.setVisible(false);
    }

    @FXML
    private void handleSearch() {
        String texteRecherche = searchField.getText();
        String categorieFiltre = filterType.getValue();
        try {
            List<Service> resultats;
            if (modeArchive) {
                resultats = serviceService.afficherArchives().stream()
                        .filter(s -> texteRecherche.isEmpty() ||
                                s.getTitre().toLowerCase().contains(texteRecherche.toLowerCase()))
                        .toList();
            } else {
                resultats = serviceService.rechercher(texteRecherche, categorieFiltre);
            }
            afficherCartes(resultats);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}