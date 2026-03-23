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
import models.Projet;
import models.Utilisateur;
import models.UserRole;
import services.ProjetService;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class EmployeListeProjetController {

    @FXML private FlowPane flowPaneProjets;
    @FXML private TextField searchField;
    @FXML private Label lblBienvenue;

    private ProjetService projetService = new ProjetService();

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserRole.getInstance().getUser();
        if (currentUser != null) {
            lblBienvenue.setText("Bonjour, " + currentUser.getPrenom());
            rafraichirListe(currentUser);

            // Écouteur pour la recherche en temps réel
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                rechercherProjet();
            });
        }
    }

    public void rafraichirListe(Utilisateur user) {
        String nomComplet = (user.getNom() + " " + user.getPrenom()).toLowerCase().trim();
        List<Projet> mesProjets = projetService.listerTousLesProjets().stream()
                .filter(p -> p.getEquipeMembres() != null && p.getEquipeMembres().toLowerCase().contains(nomComplet))
                .filter(p -> !p.isArchived())
                .collect(Collectors.toList());
        afficherLesCartes(mesProjets);
    }

    private void afficherLesCartes(List<Projet> projets) {
        flowPaneProjets.getChildren().clear();
        if (projets.isEmpty()) {
            Label noData = new Label("Aucun projet trouvé.");
            noData.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            flowPaneProjets.getChildren().add(noData);
        } else {
            for (Projet p : projets) {
                flowPaneProjets.getChildren().add(creerCarteSimple(p));
            }
        }
    }

    private VBox creerCarteSimple(Projet p) {
        VBox card = new VBox(15);
        card.setPrefWidth(320);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label title = new Label(p.getNom());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        Label statusBadge = new Label(p.getStatut().toUpperCase());
        statusBadge.setStyle("-fx-background-color: " + getStatusColor(p.getStatut()) +
                "; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 10px;");

        ProgressBar pb = new ProgressBar(p.getProgression() / 100.0);
        pb.setPrefWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent: #3b82f6;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button btnDetails = new Button("Détails");
        btnDetails.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand;");
        btnDetails.setOnAction(e -> afficherPopupDetails(p));

        Button btnChat = new Button("Chat Projet");
        btnChat.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnChat.setOnAction(e -> ouvrirChatSpecifique(p));

        actions.getChildren().addAll(btnDetails, btnChat);
        card.getChildren().addAll(statusBadge, title, pb, new Separator(), actions);

        return card;
    }

    @FXML
    private void ouvrirChat() {
        Utilisateur user = UserRole.getInstance().getUser();
        if (user == null) return;

        String nom = (user.getNom() + " " + user.getPrenom()).toLowerCase().trim();
        projetService.listerTousLesProjets().stream()
                .filter(p -> p.getEquipeMembres() != null && p.getEquipeMembres().toLowerCase().contains(nom))
                .findFirst()
                .ifPresentOrElse(this::ouvrirChatSpecifique, () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous n'avez aucun projet actif pour discuter.");
                    alert.show();
                });
    }


    private void ouvrirChatSpecifique(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatProjet.fxml"));
            Parent root = loader.load();

            ChatProjetController chatCtrl = loader.getController();

            chatCtrl.initChat(p.getId(), p.getNom());

            Stage stage = new Stage();
            stage.setTitle("Chat d'équipe - " + p.getNom());
            stage.setScene(new Scene(root));

            stage.setOnCloseRequest(e -> {
                chatCtrl.stopChat();
                System.out.println("Déconnexion Pusher pour le projet : " + p.getNom());
            });

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement FXML Chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherPopupDetails(Projet p) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Fiche Projet : " + p.getNom());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: white;");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);

        ajouterLigne(grid, 0, "Description:", p.getDescription());
        ajouterLigne(grid, 1, "Responsable:", p.getChefProjet());
        ajouterLigne(grid, 2, "Début:", p.getDateDebut() != null ? p.getDateDebut().toString() : "N/A");
        ajouterLigne(grid, 3, "Fin:", p.getDateFin() != null ? p.getDateFin().toString() : "N/A");
        ajouterLigne(grid, 4, "Équipe:", p.getEquipe());
        ajouterLigne(grid, 5, "Avancement:", p.getProgression() + "%");

        Button btnFermer = new Button("Fermer");
        btnFermer.setOnAction(e -> stage.close());

        Label head = new Label("DÉTAILS DU PROJET");
        head.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #3b82f6;");

        layout.getChildren().addAll(head, new Separator(), grid, new Separator(), btnFermer);
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 450, 480));
        stage.show();
    }

    private void ajouterLigne(GridPane grid, int row, String label, String valeur) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        Label val = new Label(valeur != null ? valeur : "Non renseigné");
        val.setWrapText(true);
        val.setMaxWidth(250);
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    @FXML
    private void rechercherProjet() {
        Utilisateur currentUser = UserRole.getInstance().getUser();
        if (currentUser == null) return;

        String nomComplet = (currentUser.getNom() + " " + currentUser.getPrenom()).toLowerCase().trim();
        String search = searchField.getText().toLowerCase().trim();

        List<Projet> filtrés = projetService.listerTousLesProjets().stream()
                .filter(p -> p.getEquipeMembres() != null && p.getEquipeMembres().toLowerCase().contains(nomComplet))
                .filter(p -> p.getNom().toLowerCase().contains(search))
                .collect(Collectors.toList());

        afficherLesCartes(filtrés);
    }

    private String getStatusColor(String statut) {
        if (statut == null) return "#94a3b8";
        switch (statut.toLowerCase()) {
            case "en cours": return "#10b981";
            case "planifié": return "#3b82f6";
            case "terminé": return "#6366f1";
            default: return "#94a3b8";
        }
    }

}