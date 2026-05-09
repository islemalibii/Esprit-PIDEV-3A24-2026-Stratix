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
    @FXML private ComboBox<UserWrapper> cbResponsable;
    @FXML private ListView<UserWrapper> lvMembres;

    private ProjetService service = new ProjetService();
    private Projet projetEnModification;

    private static class UserWrapper {
        int id;
        String nomComplet;
        UserWrapper(int id, String nomComplet) {
            this.id = id;
            this.nomComplet = nomComplet;
        }
        @Override public String toString() { return nomComplet; }
        @Override public boolean equals(Object obj) {
            return obj instanceof UserWrapper && ((UserWrapper) obj).id == this.id;
        }
    }

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("Planifié", "En cours", "Terminé", "Annulé");
        lvMembres.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cbResponsable.setConverter(new StringConverter<UserWrapper>() {
            @Override public String toString(UserWrapper u) { return u == null ? "" : u.nomComplet; }
            @Override public UserWrapper fromString(String s) { return null; }
        });
        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        Connection conn = MyDataBase.getInstance().getCnx();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nom, prenom FROM utilisateur")) {
            while (rs.next()) {
                UserWrapper u = new UserWrapper(rs.getInt("id"),
                        rs.getString("nom") + " " + rs.getString("prenom"));
                cbResponsable.getItems().add(u);
                lvMembres.getItems().add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void chargerDonnees(int idProjet) {
        this.projetEnModification = service.chercherProjetParId(idProjet);
        if (projetEnModification == null) return;

        txtNom.setText(projetEnModification.getNom());
        txtDescription.setText(projetEnModification.getDescription());
        txtBudget.setText(String.valueOf(projetEnModification.getBudget()));
        txtProgression.setText(String.valueOf(projetEnModification.getProgression()));
        comboStatut.setValue(projetEnModification.getStatut());

        if (projetEnModification.getDateDebut() != null)
            dateDebut.setValue(((java.sql.Date) projetEnModification.getDateDebut()).toLocalDate());
        if (projetEnModification.getDateFin() != null)
            dateFin.setValue(((java.sql.Date) projetEnModification.getDateFin()).toLocalDate());

        // Pré-sélection responsable
        for (UserWrapper u : cbResponsable.getItems()) {
            if (u.id == projetEnModification.getResponsableId()) {
                cbResponsable.setValue(u);
                break;
            }
        }

        // Pré-sélection membres
        if (projetEnModification.getEquipeMembres() != null) {
            List<String> membresNoms = Arrays.asList(
                    projetEnModification.getEquipeMembres().split(", "));
            for (int i = 0; i < lvMembres.getItems().size(); i++) {
                if (membresNoms.contains(lvMembres.getItems().get(i).nomComplet)) {
                    lvMembres.getSelectionModel().select(i);
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
            projetEnModification.setResponsableId(cbResponsable.getValue().id);

            List<UserWrapper> membresSel = lvMembres.getSelectionModel().getSelectedItems();
            String nouveauxMembres = membresSel.stream()
                    .map(u -> u.nomComplet)
                    .collect(Collectors.joining(", "));
            projetEnModification.setEquipeMembres(nouveauxMembres);

            service.mettreAJourProjet(projetEnModification);

            // ── Synchroniser projet_utilisateur pour Symfony ──
            syncProjetUtilisateur(projetEnModification.getId(), membresSel);

            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Projet mis à jour !");
            fermerFenetre();
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    /**
     * Remplace tous les membres dans projet_utilisateur pour ce projet
     */
    private void syncProjetUtilisateur(int projetId, List<UserWrapper> membres) {
        Connection conn = MyDataBase.getInstance().getCnx();
        try {
            // 1. Supprimer les anciens membres
            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM projet_utilisateur WHERE projet_id = ?");
            del.setInt(1, projetId);
            del.executeUpdate();

            // 2. Insérer les nouveaux
            if (!membres.isEmpty()) {
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT IGNORE INTO projet_utilisateur (projet_id, utilisateur_id) VALUES (?, ?)");
                for (UserWrapper u : membres) {
                    ins.setInt(1, projetId);
                    ins.setInt(2, u.id);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            System.out.println("✅ projet_utilisateur synchronisé pour projet id=" + projetId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sync projet_utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleAnnuler() { fermerFenetre(); }
    private void fermerFenetre() { ((Stage) txtNom.getScene().getWindow()).close(); }
    private boolean validerChamps() {
        return !txtNom.getText().isEmpty() && cbResponsable.getValue() != null;
    }
    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setContentText(msg);
        a.showAndWait();
    }
}