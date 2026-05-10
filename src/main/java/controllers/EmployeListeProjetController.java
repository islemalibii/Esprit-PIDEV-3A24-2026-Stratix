package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Projet;
import models.Utilisateur;
import models.UserRole;
import services.ProjetService;
import utils.MyDataBase;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeListeProjetController {

    @FXML private FlowPane flowPaneProjets;
    @FXML private TextField searchField;
    @FXML private Label lblBienvenue;

    private final ProjetService projetService = new ProjetService();

    @FXML
    public void initialize() {
        Utilisateur currentUser = UserRole.getInstance().getUser();
        if (currentUser != null) {
            lblBienvenue.setText("Bonjour, " + currentUser.getPrenom());
            rafraichirListe(currentUser);
            searchField.textProperty().addListener((obs, o, n) -> rechercherProjet());
        }
    }

    public void rafraichirListe(Utilisateur user) {
        List<Projet> mesProjets = findProjetsForUser(user);
        afficherLesCartes(mesProjets);
    }

    /**
     * Cherche les projets de l'employé dans DEUX sources :
     * 1. projet_utilisateur (projets créés depuis Symfony)
     * 2. equipe_membres texte (projets créés depuis Java)
     */
    private List<Projet> findProjetsForUser(Utilisateur user) {
        List<Projet> tous = projetService.listerTousLesProjets();
        List<Integer> idsViaJointure = getProjetIdsViaJointure(user.getId());
        String nomComplet = (user.getNom() + " " + user.getPrenom()).toLowerCase().trim();
        String prenomNom  = (user.getPrenom() + " " + user.getNom()).toLowerCase().trim();

        return tous.stream()
                .filter(p -> !p.isArchived())
                .filter(p ->
                        // Source 1 : table projet_utilisateur (Symfony)
                        idsViaJointure.contains(p.getId())
                                ||
                                // Source 2 : colonne equipe_membres texte (Java)
                                (p.getEquipeMembres() != null && (
                                        p.getEquipeMembres().toLowerCase().contains(nomComplet) ||
                                                p.getEquipeMembres().toLowerCase().contains(prenomNom)
                                ))
                )
                .collect(Collectors.toList());
    }

    /**
     * Récupère les IDs des projets où l'utilisateur est dans projet_utilisateur
     */
    private List<Integer> getProjetIdsViaJointure(int userId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT projet_id FROM projet_utilisateur WHERE utilisateur_id = ?";
        try (PreparedStatement ps = MyDataBase.getInstance().getCnx().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("projet_id"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur projet_utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return ids;
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

        Button btnDetails = new Button("Voir Détails");
        btnDetails.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        btnDetails.setOnAction(e -> voirDetails(p));

        Button btnChat = new Button("💬 Chat");
        btnChat.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        btnChat.setOnAction(e -> ouvrirChatSpecifique(p));

        actions.getChildren().addAll(btnDetails, btnChat);
        card.getChildren().addAll(statusBadge, title, pb, new Separator(), actions);
        return card;
    }

    private void voirDetails(Projet p) {
        if (MainController.staticContentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailsProjet.fxml"));
            Parent root = loader.load();
            DetailsProjetController ctrl = loader.getController();
            ctrl.setReadOnly(true);
            ctrl.setProjet(p);
            MainController.staticContentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement détails projet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirChat() {
        Utilisateur user = UserRole.getInstance().getUser();
        if (user == null) return;
        findProjetsForUser(user).stream()
                .findFirst()
                .ifPresentOrElse(this::ouvrirChatSpecifique, () ->
                        new Alert(Alert.AlertType.INFORMATION,
                                "Vous n'avez aucun projet actif pour discuter.").show());
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
            stage.setOnCloseRequest(e -> chatCtrl.stopChat());
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement FXML Chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void rechercherProjet() {
        Utilisateur currentUser = UserRole.getInstance().getUser();
        if (currentUser == null) return;
        String search = searchField.getText().toLowerCase().trim();
        List<Projet> filtrés = findProjetsForUser(currentUser).stream()
                .filter(p -> p.getNom().toLowerCase().contains(search))
                .collect(Collectors.toList());
        afficherLesCartes(filtrés);
    }

    private String getStatusColor(String statut) {
        if (statut == null) return "#94a3b8";
        return switch (statut.toLowerCase()) {
            case "en cours" -> "#10b981";
            case "planifié" -> "#3b82f6";
            case "terminé"  -> "#6366f1";
            default         -> "#94a3b8";
        };
    }
}