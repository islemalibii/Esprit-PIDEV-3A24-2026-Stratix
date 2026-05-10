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
            comboFiltre.setItems(FXCollections.observableArrayList(
                    "Tous les projets", "Planifié", "En cours", "Terminé", "Annulé"));
            comboFiltre.setValue("Tous les projets");
            comboFiltre.setOnAction(e -> filtrerEtAfficher());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> filtrerEtAfficher());
        }

        rafraichirDonnees();
    }

    public void rafraichirDonnees() {
        listeCompleteProjets = projetService.listerTousLesProjets();
        updateStatistics(listeCompleteProjets);
        filtrerEtAfficher();
    }

    private void updateStatistics(List<Projet> projets) {
        if (lblTotal   != null) lblTotal.setText(String.valueOf(projets.size()));
        if (lblEnCours != null) lblEnCours.setText(String.valueOf(projets.stream().filter(p -> "En cours".equals(p.getStatut())).count()));
        if (lblTermine != null) lblTermine.setText(String.valueOf(projets.stream().filter(p -> "Terminé".equals(p.getStatut())).count()));
        if (lblAnnule  != null) lblAnnule.setText(String.valueOf(projets.stream().filter(p -> "Annulé".equals(p.getStatut())).count()));
    }

    private void filtrerEtAfficher() {
        if (containerProjets == null) return;
        containerProjets.getChildren().clear();
        String statut    = (comboFiltre  != null) ? comboFiltre.getValue() : "Tous les projets";
        String recherche = (searchField  != null) ? searchField.getText()  : "";
        List<Projet> filtree = projetService.rechercherProjets(recherche, statut);
        for (Projet p : filtree) {
            containerProjets.getChildren().add(creerCardProjet(p));
        }
    }

    // ── Carte projet ──────────────────────────────────────────────────────────

    private VBox creerCardProjet(Projet p) {
        VBox card = new VBox(15);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(350);

        // Badge statut
        Label statutBadge = new Label(p.getStatut().toUpperCase());
        statutBadge.getStyleClass().addAll("statut-badge", getStatutClass(p.getStatut()));

        // Nom
        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("project-title");

        // Description
        Label desc = new Label(p.getDescription());
        desc.getStyleClass().add("project-desc");
        desc.setWrapText(true);
        desc.setMaxHeight(50);

        // Progression
        HBox labelBox = new HBox();
        Label lblProgText = new Label("Progression");
        lblProgText.getStyleClass().add("summary-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblPct = new Label(p.getProgression() + "%");
        lblPct.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        labelBox.getChildren().addAll(lblProgText, spacer, lblPct);

        ProgressBar pb = new ProgressBar(p.getProgression() / 100.0);
        pb.getStyleClass().add("progress-bar");
        pb.setPrefWidth(Double.MAX_VALUE);

        VBox progBox = new VBox(8, labelBox, pb);

        // ── Bouton Voir Détails (pleine largeur) ──
        Button btnView = new Button("🔍 Voir Détails");
        btnView.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 14;");
        btnView.setMaxWidth(Double.MAX_VALUE);
        btnView.setOnAction(e -> voirDetails(p));

        // ── Bouton Chat (pleine largeur) ──
        Button btnChat = new Button("💬 Chat Projet");
        btnChat.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 14;");
        btnChat.setMaxWidth(Double.MAX_VALUE);
        btnChat.setOnAction(e -> ouvrirChatSpecifique(p));

        // ── Ligne 3 : Modifier | PDF | Archiver ──
        Button btnModifier = new Button("✏ Modifier");
        btnModifier.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 12;");
        btnModifier.setOnAction(e -> ouvrirFenetreModification(p));

        Button btnPdf = new Button("📄 PDF");
        btnPdf.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 12;");
        btnPdf.setOnAction(e -> exporterEnPDF(p));

        Button btnArchiver = new Button("📁 Archiver");
        btnArchiver.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #ea580c; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 7 12;");
        btnArchiver.setOnAction(e -> handleArchiver(p));

        HBox actionsSecondaires = new HBox(8, btnModifier, btnPdf, btnArchiver);
        actionsSecondaires.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
                statutBadge, nom, desc,
                new Separator(),
                progBox,
                btnView,
                btnChat,          // ← ajouté ici
                actionsSecondaires
        );

        return card;
    }

    // ── Navigation détails ────────────────────────────────────────────────────

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

    // ── Chat ──────────────────────────────────────────────────────────────────

    private void ouvrirChatSpecifique(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatProjet.fxml"));
            Parent root = loader.load();
            ChatProjetController chatCtrl = loader.getController();
            chatCtrl.initChat(p.getId(), p.getNom());
            Stage stage = new Stage();
            stage.setTitle("Chat - " + p.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    private void exporterEnPDF(Projet p) {
        Document document = new Document();
        try {
            String fileName = "Rapport_" + p.getNom().replace(" ", "_") + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            com.lowagie.text.Paragraph titre = new com.lowagie.text.Paragraph("STRATIX - RAPPORT PROJET", titleFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(new com.lowagie.text.Paragraph(
                    "\nNom: " + p.getNom() +
                            "\nStatut: " + p.getStatut() +
                            "\nBudget: " + p.getBudget() + " DT" +
                            "\nDescription: " + p.getDescription() +
                            "\nProgression: " + p.getProgression() + "%" +
                            "\nResponsable: " + p.getChefProjet()
            ));

            String qrData = "Projet: " + p.getNom() + " | Statut: " + p.getStatut();
            String qrUrl  = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="
                    + URLEncoder.encode(qrData, StandardCharsets.UTF_8);
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(new java.net.URL(qrUrl));
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            document.close();
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF.").showAndWait();
        }
    }

    // ── Modification ──────────────────────────────────────────────────────────

    private void ouvrirFenetreModification(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierProjet.fxml"));
            Parent root = loader.load();
            ModifierProjetController ctrl = loader.getController();
            ctrl.chargerDonnees(p.getId());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le projet");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            rafraichirDonnees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Archivage ─────────────────────────────────────────────────────────────

    private void handleArchiver(Projet p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Archiver le projet \"" + p.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                projetService.archiverUnProjet(p.getId());
                rafraichirDonnees();
            }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void voirArchives()       { chargerFenetre("/ListeArchives.fxml",  "Archives"); }
    @FXML private void allerAjouterProjet() { chargerFenetre("/AjouterProjet.fxml",  "Nouveau Projet"); }

    private void chargerFenetre(String fxmlPath, String titre) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            rafraichirDonnees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private String getStatutClass(String statut) {
        return switch (statut != null ? statut.toLowerCase() : "") {
            case "terminé"  -> "badge-termine";
            case "en cours" -> "badge-en-cours";
            case "annulé"   -> "badge-annule";
            default         -> "badge-planifie";
        };
    }
}