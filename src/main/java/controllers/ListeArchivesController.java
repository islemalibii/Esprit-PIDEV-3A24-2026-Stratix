
package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Projet;
import services.ProjetService;

import java.util.List;

public class ListeArchivesController {
    @FXML private VBox containerArchives;
    @FXML private Label lblNbArchives;
    private ProjetService projetService;
    @FXML
    public void initialize() {
        projetService = new ProjetService();
        chargerArchives();
    }

    private void chargerArchives() {
        if (containerArchives == null) return;

        containerArchives.getChildren().clear();
        List<Projet> archives = projetService.listerArchives();

        if (lblNbArchives != null) {
            lblNbArchives.setText(archives.size() + " projet(s) archivé(s)");
        }

        if (archives.isEmpty()) {
            Label emptyLabel = new Label("Le coffre-fort est vide.");
            emptyLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px; -fx-padding: 20;");
            containerArchives.getChildren().add(emptyLabel);
            return;
        }

        for (Projet p : archives) {
            containerArchives.getChildren().add(creerCardArchive(p));
        }
    }

    private HBox creerCardArchive(Projet p) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #f7fafc; -fx-padding: 15; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");

        VBox info = new VBox(3);
        Label nom = new Label(p.getNom());
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #4a5568;");

        Label details = new Label("Statut final: " + p.getStatut() + " | Budget: " + p.getBudget() + " DT");
        details.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");

        info.getChildren().addAll(nom, details);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button btnRestaurer = new Button("Désarchiver");
        btnRestaurer.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRestaurer.setOnAction(e -> {
            projetService.desarchiverUnProjet(p.getId());
            chargerArchives(); // On rafraîchit la liste des archives
        });

        Button btnSupp = new Button("Supprimer");
        btnSupp.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-cursor: hand;");
        btnSupp.setOnAction(e -> {
            projetService.supprimerUnProjet(p.getId());
            chargerArchives();
        });

        HBox actions = new HBox(10);
        actions.getChildren().addAll(btnRestaurer, btnSupp);

        card.getChildren().addAll(info, actions);
        return card;
    }

    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) containerArchives.getScene().getWindow();
        stage.close();
    }
}
