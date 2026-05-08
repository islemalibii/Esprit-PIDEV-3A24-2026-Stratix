package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import models.Projet;
import models.Role;
import models.UserRole;
import models.Utilisateur;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class DetailsProjetController {

    @FXML private Label labelNomTitre, labelNom, labelBudget, labelStatut, labelDescription, labelDateDebut, labelDateFin;

    @FXML
    private void retourListe() {
        if (MainController.staticContentArea == null) return;
        try {
            Utilisateur user = UserRole.getInstance().getUser();
            String fxml = (user != null && user.getRole() == Role.EMPLOYE)
                    ? "/EmployeListeProjets.fxml"
                    : "/ListeProjets.fxml";
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            MainController.staticContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("❌ Erreur retour liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setProjet(Projet p) {
        if (p == null) return;
        labelNomTitre.setText(p.getNom());
        labelNom.setText(p.getNom());
        labelDescription.setText(p.getDescription());
        labelBudget.setText(String.format("%.2f DT", p.getBudget()));
        labelStatut.setText(p.getStatut().toUpperCase());
        appliquerStyleStatut(p.getStatut());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        labelDateDebut.setText(p.getDateDebut() != null ? sdf.format(p.getDateDebut()) : "--/--/----");
        labelDateFin.setText(p.getDateFin() != null ? sdf.format(p.getDateFin()) : "--/--/----");
    }

    private void appliquerStyleStatut(String statut) {
        String color = switch (statut.toLowerCase()) {
            case "terminé"  -> "#48bb78";
            case "en cours" -> "#38b2ac";
            case "annulé"   -> "#f56565";
            default         -> "#00bcd4";
        };
        labelStatut.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5; -fx-font-weight: 900;");
    }
}