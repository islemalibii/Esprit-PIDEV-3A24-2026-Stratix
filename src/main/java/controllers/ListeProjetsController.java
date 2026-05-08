package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Projet;
import services.ProjetService;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListeProjetsController {

    @FXML private FlowPane containerProjets;
    @FXML private Label lblTotal, lblEnCours, lblTermine, lblAnnule;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private ImageView logoImageView;

    private ProjetService projetService;
    private List<Projet> listeCompleteProjets;

    @FXML
    public void initialize() {
        projetService = new ProjetService();

        if (logoImageView != null) {
            try {
                Image logo = new Image(getClass().getResourceAsStream("/stratix.png"));
                logoImageView.setImage(logo);
            } catch (Exception e) {
                System.out.println("Logo introuvable.");
            }
        }

        if (comboFiltre != null) {
            comboFiltre.setItems(FXCollections.observableArrayList("Tous les projets", "Planifié", "En cours", "Terminé", "Annulé"));
            comboFiltre.setValue("Tous les projets");
            comboFiltre.setOnAction(e -> filtrerEtAfficher());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerEtAfficher());
        }

        rafraichirDonnees();
    }

    public void rafraichirDonnees() {
        listeCompleteProjets = projetService.listerTousLesProjets();
        updateStatistics(listeCompleteProjets);
        filtrerEtAfficher();
    }

    private void updateStatistics(List<Projet> projets) {
        if (lblTotal != null) lblTotal.setText(String.valueOf(projets.size()));
        if (lblEnCours != null) lblEnCours.setText(String.valueOf(projets.stream().filter(p -> "En cours".equals(p.getStatut())).count()));
        if (lblTermine != null) lblTermine.setText(String.valueOf(projets.stream().filter(p -> "Terminé".equals(p.getStatut())).count()));
        if (lblAnnule != null) lblAnnule.setText(String.valueOf(projets.stream().filter(p -> "Annulé".equals(p.getStatut())).count()));
    }

    private void filtrerEtAfficher() {
        if (containerProjets == null) return;
        containerProjets.getChildren().clear();
        String statut = (comboFiltre != null) ? comboFiltre.getValue() : "Tous les projets";
        String recherche = (searchField != null) ? searchField.getText() : "";
        List<Projet> filtree = projetService.rechercherProjets(recherche, statut);
        for (Projet p : filtree) {
            containerProjets.getChildren().add(creerCardProjet(p));
        }
    }

    private VBox creerCardProjet(Projet p) {
        VBox card = new VBox(15);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(350);

        Label statutBadge = new Label(p.getStatut().toUpperCase());
        statutBadge.getStyleClass().add("statut-badge");
        String statusClass = switch (p.getStatut().toLowerCase()) {
            case "terminé"  -> "badge-termine";
            case "en cours" -> "badge-en-cours";
            case "annulé"   -> "badge-annule";
            default         -> "badge-planifie";
        };
        statutBadge.getStyleClass().add(statusClass);

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("project-title");

        Label desc = new Label(p.getDescription());
        desc.getStyleClass().add("project-desc");
        desc.setWrapText(true);
        desc.setMaxHeight(50);

        VBox progBox = new VBox(8);
        HBox labelBox = new HBox();
        Label lblProgText = new Label("Progression");
        lblProgText.getStyleClass().add("summary-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblPercentage = new Label(p.getProgression() + "%");
        lblPercentage.getStyleClass().add("summary-value");
        lblPercentage.setStyle("-fx-font-size: 14px;");
        labelBox.getChildren().addAll(lblProgText, spacer, lblPercentage);
        ProgressBar pb = new ProgressBar(p.getProgression() / 100.0);
        pb.getStyleClass().add("progress-bar");
        pb.setPrefWidth(Double.MAX_VALUE);
        progBox.getChildren().addAll(labelBox, pb);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button btnView = new Button("Voir Détails");
        btnView.getStyleClass().add("btn-secondary");
        btnView.setOnAction(e -> voirDetails(p));
        actions.getChildren().add(btnView);

        card.getChildren().addAll(statutBadge, nom, desc, new Separator(), progBox, actions);
        return card;
    }

    /**
     * Charge la vue détails dans le contentArea du MainController (sidebar reste visible)
     */
    public void voirDetails(Projet p) {
        if (MainController.staticContentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailsProjet.fxml"));
            Parent root = loader.load();
            DetailsProjetController controller = loader.getController();
            controller.setProjet(p);
            MainController.staticContentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exporterEnPDF(Projet p) {
        Document document = new Document();
        try {
            String fileName = "Rapport_" + p.getNom().replace(" ", "_") + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Paragraph titre = new Paragraph("STRATIX - RAPPORT PROJET", titleFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(new Paragraph("\nNom: " + p.getNom() + "\nStatut: " + p.getStatut() + "\nBudget: " + p.getBudget() + " DT\nDescription: " + p.getDescription()));
            String qrData = "Projet: " + p.getNom() + " | Statut: " + p.getStatut();
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + URLEncoder.encode(qrData, StandardCharsets.UTF_8);
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(new java.net.URL(qrCodeUrl));
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);
            document.close();
            Desktop.getDesktop().open(new File(fileName));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleArchiver(Projet p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Archiver '" + p.getNom() + "' ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) { projetService.archiverUnProjet(p.getId()); rafraichirDonnees(); }
        });
    }

    private void ouvrirFenetreModification(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierProjet.fxml"));
            Parent root = loader.load();
            ModifierProjetController ctrl = loader.getController();
            ctrl.chargerDonnees(p.getId());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            rafraichirDonnees();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void ouvrirChatSpecifique(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatProjet.fxml"));
            Parent root = loader.load();
            ChatProjetController chatCtrl = loader.getController();
            chatCtrl.initChat(p.getId(), p.getNom());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void voirArchives() { chargerFenetre("/ListeArchives.fxml", "Archives"); }
    @FXML private void allerAjouterProjet() { chargerFenetre("/AjouterProjet.fxml", "Nouveau Projet"); }

    private void chargerFenetre(String fxmlPath, String titre) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            rafraichirDonnees();
        } catch (IOException e) { e.printStackTrace(); }
    }
}