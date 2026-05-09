package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import models.Planning;
import models.Utilisateur;
import services.SERVICEPlanning;
import services.UtilisateurService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML private ComboBox<Utilisateur> cmbEmploye;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtHeureDebut;
    @FXML private TextField txtHeureFin;
    @FXML private ComboBox<String> cbTypeShift;

    @FXML private Label lblTotalEmployes;
    @FXML private Label lblEnPoste;
    @FXML private Label lblAbsents;
    @FXML private Label lblMessage;

    private SERVICEPlanning planningService;
    private UtilisateurService utilisateurService;
    private int selectedPlanningId = -1;

    private static final String TYPE_CONGE = "CONGÉ";
    private static final String TYPE_MALADIE = "MALADIE";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== Initialisation PlanningController (Avec Utilisateur) ===");

        planningService = new SERVICEPlanning();
        utilisateurService = UtilisateurService.getInstance();

        // Charger les utilisateurs (employés uniquement - avec rôle EMPLOYE ou RESPONSABLE)
        chargerUtilisateurs();

        // Configuration de l'affichage des utilisateurs - COMME SYMFONY
        cmbEmploye.setCellFactory(param -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setText(null);
                } else {
                    // ✅ Affiche "Prénom Nom" exactement comme Symfony
                    setText(u.getPrenom() + " " + u.getNom());
                }
            }
        });

        cmbEmploye.setButtonCell(new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setText(null);
                } else {
                    // ✅ Affiche "Prénom Nom" dans le bouton aussi
                    setText(u.getPrenom() + " " + u.getNom());
                }
            }
        });

        // Types de shift avec icônes (comme Symfony)
        cbTypeShift.setItems(FXCollections.observableArrayList(
                "☀️ JOUR", "🌆 SOIR", "🌙 NUIT", "🌴 CONGÉ", "🤒 MALADIE", "🎓 FORMATION", "📋 AUTRE"
        ));

        // Charger les statistiques
        chargerStatistiques();

        // Valeurs par défaut
        dpDate.setValue(LocalDate.now());

        // Désactiver les heures si type = CONGÉ ou MALADIE
        cbTypeShift.setOnAction(event -> onTypeShiftChanged());
    }

    private void chargerUtilisateurs() {
        try {
            // Récupérer tous les utilisateurs actifs avec rôle EMPLOYE ou RESPONSABLE
            List<Utilisateur> tousUtilisateurs = utilisateurService.getAllActifs();

            // Filtrer pour garder uniquement les employés et responsables (pas les admins/CEO pour les plannings)
            List<Utilisateur> employes = tousUtilisateurs.stream()
                    .filter(u -> u.isEmploye() || u.isResponsable())
                    .toList();

            cmbEmploye.setItems(FXCollections.observableArrayList(employes));
            System.out.println("✅ " + employes.size() + " employés chargés pour les plannings");

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement employés: " + e.getMessage());
            e.printStackTrace();
            showMessage("❌ Erreur chargement des employés", "error");
        }
    }

    private void chargerStatistiques() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllActifs();
            List<Utilisateur> employes = utilisateurs.stream()
                    .filter(u -> u.isEmploye() || u.isResponsable())
                    .toList();

            lblTotalEmployes.setText(String.valueOf(employes.size()));

            long enPoste = employes.stream()
                    .filter(Utilisateur::isActif)
                    .count();

            lblEnPoste.setText(String.valueOf(enPoste));
            lblAbsents.setText(String.valueOf(employes.size() - enPoste));

        } catch (SQLException e) {
            lblTotalEmployes.setText("0");
            lblEnPoste.setText("0");
            lblAbsents.setText("0");
            System.err.println("Erreur statistiques: " + e.getMessage());
        }
    }

    @FXML
    private void onTypeShiftChanged() {
        String type = cbTypeShift.getValue();
        if (type != null) {
            String cleanType = getTypeWithoutIcon(type);
            if (TYPE_CONGE.equals(cleanType) || TYPE_MALADIE.equals(cleanType)) {
                txtHeureDebut.setDisable(true);
                txtHeureFin.setDisable(true);
                txtHeureDebut.clear();
                txtHeureFin.clear();
                txtHeureDebut.setPromptText("Journée complète");
                txtHeureFin.setPromptText("Journée complète");
            } else {
                txtHeureDebut.setDisable(false);
                txtHeureFin.setDisable(false);
                txtHeureDebut.setPromptText("HH:MM");
                txtHeureFin.setPromptText("HH:MM");
            }
        }
    }

    @FXML
    private void ajouterPlanning() {
        if (!validerChamps()) return;

        try {
            Planning p = new Planning();
            Utilisateur selected = cmbEmploye.getValue();
            p.setEmployeId(selected.getId());
            p.setDate(Date.valueOf(dpDate.getValue()));

            String type = getTypeWithoutIcon(cbTypeShift.getValue());

            if (TYPE_CONGE.equals(type) || TYPE_MALADIE.equals(type)) {
                p.setHeureDebut(Time.valueOf("00:00:00"));
                p.setHeureFin(Time.valueOf("23:59:00"));
            } else {
                p.setHeureDebut(Time.valueOf(txtHeureDebut.getText() + ":00"));
                p.setHeureFin(Time.valueOf(txtHeureFin.getText() + ":00"));
            }

            p.setTypeShift(type);

            planningService.addPlanning(p);
            showMessage("✅ Planning ajouté avec succès !", "success");
            viderFormulaire();
            chargerStatistiques();

        } catch (IllegalArgumentException e) {
            showMessage("❌ Format d'heure incorrect ! Utilisez HH:MM", "error");
        } catch (Exception e) {
            showMessage("❌ Erreur : " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierPlanning() {
        if (selectedPlanningId == -1) {
            showMessage("❌ Aucun planning sélectionné", "error");
            return;
        }

        if (!validerChamps()) return;

        try {
            Planning p = new Planning();
            p.setId(selectedPlanningId);
            Utilisateur selected = cmbEmploye.getValue();
            p.setEmployeId(selected.getId());
            p.setDate(Date.valueOf(dpDate.getValue()));

            String type = getTypeWithoutIcon(cbTypeShift.getValue());

            if (TYPE_CONGE.equals(type) || TYPE_MALADIE.equals(type)) {
                p.setHeureDebut(Time.valueOf("00:00:00"));
                p.setHeureFin(Time.valueOf("23:59:00"));
            } else {
                p.setHeureDebut(Time.valueOf(txtHeureDebut.getText() + ":00"));
                p.setHeureFin(Time.valueOf(txtHeureFin.getText() + ":00"));
            }

            p.setTypeShift(type);

            planningService.updatePlanning(p);
            showMessage("✏️ Planning modifié avec succès !", "success");
            viderFormulaire();
            selectedPlanningId = -1;
            chargerStatistiques();

        } catch (Exception e) {
            showMessage("❌ Erreur : " + e.getMessage(), "error");
        }
    }

    @FXML
    private void supprimerPlanning() {
        if (selectedPlanningId == -1) {
            showMessage("❌ Aucun planning sélectionné", "error");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce planning ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                planningService.deletePlanning(selectedPlanningId);
                showMessage("🗑️ Planning supprimé !", "success");
                viderFormulaire();
                selectedPlanningId = -1;
                chargerStatistiques();
            }
        });
    }

    public void setPlanningToEdit(Planning planning) {
        if (planning == null) return;

        this.selectedPlanningId = planning.getId();

        // Trouver l'utilisateur dans la ComboBox
        cmbEmploye.getItems().stream()
                .filter(u -> u.getId() == planning.getEmployeId())
                .findFirst()
                .ifPresent(cmbEmploye::setValue);

        dpDate.setValue(planning.getDate().toLocalDate());

        String type = planning.getTypeShift();
        String typeWithIcon = switch(type) {
            case "JOUR" -> "☀️ JOUR";
            case "SOIR" -> "🌆 SOIR";
            case "NUIT" -> "🌙 NUIT";
            case "CONGÉ" -> "🌴 CONGÉ";
            case "MALADIE" -> "🤒 MALADIE";
            case "FORMATION" -> "🎓 FORMATION";
            default -> "📋 AUTRE";
        };
        cbTypeShift.setValue(typeWithIcon);

        if (planning.getHeureDebut() != null && planning.getHeureFin() != null) {
            String debut = planning.getHeureDebut().toString();
            String fin = planning.getHeureFin().toString();
            txtHeureDebut.setText(debut.substring(0, 5));
            txtHeureFin.setText(fin.substring(0, 5));
        }

        onTypeShiftChanged(); // Pour gérer la désactivation des heures si nécessaire
    }

    @FXML
    private void demandeConge() {
        cbTypeShift.setValue("🌴 CONGÉ");
        onTypeShiftChanged();
    }

    @FXML
    private void demandeMaladie() {
        cbTypeShift.setValue("🤒 MALADIE");
        onTypeShiftChanged();
    }

    @FXML
    private void demandeFormation() {
        cbTypeShift.setValue("🎓 FORMATION");
        onTypeShiftChanged();
    }

    @FXML
    private void openPlanningListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/PlanningListeView.fxml"));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validerChamps() {
        if (cmbEmploye.getValue() == null) {
            showMessage("❌ Sélectionnez un employé !", "error");
            return false;
        }
        if (dpDate.getValue() == null) {
            showMessage("❌ La date est requise !", "error");
            return false;
        }
        if (cbTypeShift.getValue() == null) {
            showMessage("❌ Le type est requis !", "error");
            return false;
        }

        String type = getTypeWithoutIcon(cbTypeShift.getValue());
        if (!TYPE_CONGE.equals(type) && !TYPE_MALADIE.equals(type)) {
            if (txtHeureDebut.getText().trim().isEmpty() || txtHeureFin.getText().trim().isEmpty()) {
                showMessage("❌ Les heures sont requises !", "error");
                return false;
            }
            if (!txtHeureDebut.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$") ||
                    !txtHeureFin.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showMessage("❌ Format HH:MM requis", "error");
                return false;
            }
        }
        return true;
    }

    private String getTypeWithoutIcon(String typeWithIcon) {
        if (typeWithIcon == null) return null;
        int idx = typeWithIcon.indexOf(' ');
        return (idx >= 0) ? typeWithIcon.substring(idx + 1).trim() : typeWithIcon.trim();
    }

    private void viderFormulaire() {
        cmbEmploye.setValue(null);
        dpDate.setValue(LocalDate.now());
        txtHeureDebut.clear();
        txtHeureFin.clear();
        txtHeureDebut.setDisable(false);
        txtHeureFin.setDisable(false);
        cbTypeShift.setValue(null);
        selectedPlanningId = -1;
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if (type.equals("success")) {
            lblMessage.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
        lblMessage.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblMessage.setVisible(false));
        pause.play();
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
}