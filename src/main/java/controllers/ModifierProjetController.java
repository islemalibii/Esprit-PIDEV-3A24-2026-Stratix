package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Projet;
import services.ProjetService;
import utils.MyDataBase;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModifierProjetController {

    @FXML private TextField txtNom, txtBudget, txtProgression;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateDebut, dateFin;
    @FXML private ChoiceBox<String> comboStatut;

    // Utilisation du Wrapper pour cacher l'ID
    @FXML private ComboBox<UserWrapper> cbResponsable;
    @FXML private ListView<UserWrapper> lvMembres;

    private ProjetService service = new ProjetService();
    private Projet projetEnModification;

    // Classe interne pour l'affichage propre
    private static class UserWrapper {
        int id;
        String nomComplet;
        UserWrapper(int id, String nomComplet) {
            this.id = id;
            this.nomComplet = nomComplet;
        }
        @Override
        public String toString() { return nomComplet; }

        // Utile pour la comparaison lors de la pré-sélection
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UserWrapper) {
                return ((UserWrapper) obj).id == this.id;
            }
            return false;
        }
    }

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("Planifié", "En cours", "Terminé", "Annulé");
        lvMembres.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Configurer l'affichage de la ComboBox
        cbResponsable.setConverter(new StringConverter<UserWrapper>() {
            @Override
            public String toString(UserWrapper user) { return (user == null) ? "" : user.nomComplet; }
            @Override
            public UserWrapper fromString(String string) { return null; }
        });

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        String sql = "SELECT id, nom, prenom FROM utilisateur";
        Connection conn = MyDataBase.getInstance().getCnx();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UserWrapper user = new UserWrapper(
                        rs.getInt("id"),
                        rs.getString("nom") + " " + rs.getString("prenom")
                );
                cbResponsable.getItems().add(user);
                lvMembres.getItems().add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void chargerDonnees(int idProjet) {
        this.projetEnModification = service.chercherProjetParId(idProjet);

        if (projetEnModification != null) {
            txtNom.setText(projetEnModification.getNom());
            txtDescription.setText(projetEnModification.getDescription());
            txtBudget.setText(String.valueOf(projetEnModification.getBudget()));
            txtProgression.setText(String.valueOf(projetEnModification.getProgression()));
            comboStatut.setValue(projetEnModification.getStatut());

            if (projetEnModification.getDateDebut() != null)
                dateDebut.setValue(((java.sql.Date) projetEnModification.getDateDebut()).toLocalDate());
            if (projetEnModification.getDateFin() != null)
                dateFin.setValue(((java.sql.Date) projetEnModification.getDateFin()).toLocalDate());

            // 1. Pré-sélection du Chef (par ID)
            for (UserWrapper user : cbResponsable.getItems()) {
                if (user.id == projetEnModification.getResponsableId()) {
                    cbResponsable.setValue(user);
                    break;
                }
            }

            // 2. Pré-sélection des membres (par Nom car stockés en String dans ton équipe)
            if (projetEnModification.getEquipeMembres() != null) {
                List<String> membresNoms = Arrays.asList(projetEnModification.getEquipeMembres().split(", "));
                for (int i = 0; i < lvMembres.getItems().size(); i++) {
                    if (membresNoms.contains(lvMembres.getItems().get(i).nomComplet)) {
                        lvMembres.getSelectionModel().select(i);
                    }
                }
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (!validerChamps()) return;

        try {
            projetEnModification.setNom(txtNom.getText());
            projetEnModification.setDescription(txtDescription.getText());
            projetEnModification.setBudget(Double.parseDouble(txtBudget.getText()));
            projetEnModification.setProgression(Integer.parseInt(txtProgression.getText()));
            projetEnModification.setStatut(comboStatut.getValue());
            projetEnModification.setDateDebut(java.sql.Date.valueOf(dateDebut.getValue()));
            projetEnModification.setDateFin(java.sql.Date.valueOf(dateFin.getValue()));

            // ID du Chef récupéré proprement
            projetEnModification.setResponsableId(cbResponsable.getValue().id);

            // Noms des membres (sans IDs)
            String nouveauxMembres = lvMembres.getSelectionModel().getSelectedItems()
                    .stream()
                    .map(u -> u.nomComplet)
                    .collect(Collectors.joining(", "));
            projetEnModification.setEquipeMembres(nouveauxMembres);

            service.mettreAJourProjet(projetEnModification);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Projet mis à jour !");
            fermerFenetre();

        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML private void handleAnnuler() { fermerFenetre(); }

    private void fermerFenetre() { ((Stage) txtNom.getScene().getWindow()).close(); }

    private boolean validerChamps() {
        if (txtNom.getText().isEmpty() || cbResponsable.getValue() == null) return false;
        return true;
    }



    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}