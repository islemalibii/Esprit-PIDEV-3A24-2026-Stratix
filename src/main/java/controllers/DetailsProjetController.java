package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Phase;
import models.Projet;
import models.Role;
import models.UserRole;
import models.Utilisateur;
import services.PhaseService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class DetailsProjetController {

    @FXML private Label labelNomTitre;
    @FXML private Label labelNom;
    @FXML private Label labelDescription;
    @FXML private Label labelBudget;
    @FXML private Label labelStatut;
    @FXML private Label labelDateDebut;
    @FXML private Label labelDateFin;
    @FXML private Label labelResponsable;
    @FXML private Label labelEquipe;
    @FXML private VBox containerPhases;

    private final PhaseService phaseService = new PhaseService();
    private Projet projetCourant;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    // ── Init ──────────────────────────────────────────────────────────────────

    public void setProjet(Projet p) {
        if (p == null) return;
        this.projetCourant = p;

        labelNomTitre.setText(p.getNom());
        labelNom.setText(p.getNom());
        labelDescription.setText(p.getDescription());
        labelBudget.setText(String.format("%.2f DT", p.getBudget()));
        labelStatut.setText(p.getStatut().toUpperCase());
        appliquerStyleStatut(p.getStatut());

        labelDateDebut.setText(p.getDateDebut() != null ? sdf.format(p.getDateDebut()) : "--/--/----");
        labelDateFin.setText(p.getDateFin() != null ? sdf.format(p.getDateFin()) : "--/--/----");

        if (labelResponsable != null) labelResponsable.setText(p.getChefProjet());
        if (labelEquipe != null) labelEquipe.setText(p.getEquipe());

        chargerPhases();
    }

    // ── Phases ────────────────────────────────────────────────────────────────

    private void chargerPhases() {
        if (containerPhases == null || projetCourant == null) return;
        containerPhases.getChildren().clear();

        List<Phase> phases = phaseService.listerParProjet(projetCourant.getId());

        if (phases.isEmpty()) {
            Label vide = new Label("Aucune phase planifiée.");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13;");
            vide.setPadding(new Insets(30));
            vide.setMaxWidth(Double.MAX_VALUE);
            vide.setAlignment(Pos.CENTER);
            containerPhases.getChildren().add(vide);
        } else {
            for (Phase phase : phases) {
                containerPhases.getChildren().add(creerLignePhase(phase));
            }
        }
    }

    private HBox creerLignePhase(Phase phase) {
        HBox ligne = new HBox(15);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(14, 20, 14, 20));
        ligne.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1;");

        // Badge statut
        Label badge = new Label(phase.getStatut());
        badge.setStyle(getBadgeStyle(phase.getStatut()));
        badge.setMinWidth(85);

        // Nom + objectif
        VBox infos = new VBox(3);
        Label nom = new Label(phase.getNom());
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1e293b;");
        infos.getChildren().add(nom);
        if (phase.getObjectif() != null && !phase.getObjectif().isEmpty()) {
            Label obj = new Label(phase.getObjectif());
            obj.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b;");
            infos.getChildren().add(obj);
        }
        HBox.setHgrow(infos, Priority.ALWAYS);

        // Dates
        Label labelDates = new Label(
                sdf.format(phase.getDateDebut()) + "  →  " + sdf.format(phase.getDateFin()));
        labelDates.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");

        // Vélocités (si renseignées)
        VBox velocites = new VBox(2);
        velocites.setAlignment(Pos.CENTER_RIGHT);
        velocites.setMinWidth(100);
        if (phase.getVelociteEstimee() != null) {
            Label ve = new Label("Est: " + phase.getVelociteEstimee());
            ve.setStyle("-fx-font-size: 11; -fx-text-fill: #6366f1;");
            velocites.getChildren().add(ve);
        }
        if (phase.getVelociteReelle() != null) {
            Label vr = new Label("Réel: " + phase.getVelociteReelle());
            vr.setStyle("-fx-font-size: 11; -fx-text-fill: #10b981;");
            velocites.getChildren().add(vr);
        }

        // Bouton modifier
        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13; -fx-padding: 5 10;");
        btnEdit.setOnAction(e -> ouvrirModalPhase(phase));

        // Bouton supprimer
        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13; -fx-padding: 5 10;");
        btnDelete.setOnAction(e -> supprimerPhase(phase));

        ligne.getChildren().addAll(badge, infos, labelDates, velocites, btnEdit, btnDelete);
        return ligne;
    }

    @FXML
    private void ajouterPhase() {
        ouvrirModalPhase(null);
    }

    private void ouvrirModalPhase(Phase phase) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPhase.fxml"));
            Parent root = loader.load();

            AjouterPhaseController ctrl = loader.getController();
            ctrl.setProjetId(projetCourant.getId());
            ctrl.setOnSuccess(this::chargerPhases);
            if (phase != null) ctrl.setPhase(phase);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(phase != null ? "Modifier la phase" : "Ajouter une phase");
            modal.setScene(new Scene(root));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            System.err.println("❌ Erreur modal phase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerPhase(Phase phase) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la phase \"" + phase.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                phaseService.supprimerPhase(phase.getId());
                chargerPhases();
            }
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    private void retourListe() {
        if (MainController.staticContentArea == null) return;
        try {
            Utilisateur user = UserRole.getInstance().getUser();
            String fxml = (user != null && user.getRole() == Role.EMPLOYE)
                    ? "/EmployeListeProjets.fxml" : "/ListeProjets.fxml";
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            MainController.staticContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Styles ────────────────────────────────────────────────────────────────

    private void appliquerStyleStatut(String statut) {
        String color = switch (statut.toLowerCase()) {
            case "terminé"  -> "#48bb78";
            case "en cours" -> "#38b2ac";
            case "annulé"   -> "#f56565";
            default         -> "#00bcd4";
        };
        labelStatut.setStyle("-fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-padding: 10 20; " +
                "-fx-background-radius: 20; -fx-font-weight: bold;");
    }

    private String getBadgeStyle(String statut) {
        String color = switch (statut != null ? statut.toLowerCase() : "") {
            case "terminé"  -> "#10b981";
            case "en cours" -> "#f59e0b";
            case "annulé"   -> "#ef4444";
            default         -> "#6366f1"; // Planifié
        };
        return "-fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-padding: 4 10; " +
                "-fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
    }
}