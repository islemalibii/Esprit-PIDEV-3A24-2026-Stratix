package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Projet;
import services.ProjetService;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AjoutProjetController {

    @FXML private TextField tfNom, tfBudget;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDateDebut, dpDateFin;
    @FXML private ChoiceBox<String> cbStatut;
    @FXML private ComboBox<UserWrapper> cbResponsable;
    @FXML private ListView<UserWrapper> lvMembres;

    private ProjetService projetService = new ProjetService();

    static class UserWrapper {
        int id;
        String nomComplet;
        UserWrapper(int id, String nomComplet) {
            this.id = id;
            this.nomComplet = nomComplet;
        }
        @Override public String toString() { return nomComplet; }
    }

    @FXML
    public void initialize() {
        cbStatut.getItems().add("Planifié");
        cbStatut.setValue("Planifié");
        cbStatut.setDisable(true);

        lvMembres.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cbResponsable.setConverter(new StringConverter<UserWrapper>() {
            @Override public String toString(UserWrapper u) { return u == null ? "" : u.nomComplet; }
            @Override public UserWrapper fromString(String s) { return null; }
        });

        bloquerDatesPassees(dpDateDebut);
        bloquerDatesPassees(dpDateFin);
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

    @FXML
    private void ajouterProjet() {
        try {
            if (tfNom.getText().isEmpty() || cbResponsable.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Champs obligatoires manquants.");
                return;
            }

            int idChef = cbResponsable.getValue().id;
            List<UserWrapper> membresSel = lvMembres.getSelectionModel().getSelectedItems();
            String nomsMembres = membresSel.stream()
                    .map(u -> u.nomComplet)
                    .collect(Collectors.joining(", "));

            Projet p = new Projet(
                    0, tfNom.getText(), taDescription.getText(),
                    java.sql.Date.valueOf(dpDateDebut.getValue()),
                    java.sql.Date.valueOf(dpDateFin.getValue()),
                    Double.parseDouble(tfBudget.getText()),
                    cbStatut.getValue(), 0, false, idChef, nomsMembres
            );

            projetService.ajouterProjet(p);

            // ── Récupérer l'ID du projet qui vient d'être inséré ──
            int projetId = getDernierId();
            if (projetId > 0) {
                // ── Synchroniser projet_utilisateur pour Symfony ──
                syncProjetUtilisateur(projetId, membresSel);
            }

            showAlert(Alert.AlertType.INFORMATION, "Projet ajouté !");
            annuler();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
        }
    }

    /**
     * Récupère l'ID du dernier projet inséré
     */
    private int getDernierId() {
        Connection conn = MyDataBase.getInstance().getCnx();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID() as id")) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * Insère les membres dans projet_utilisateur (table de jointure Symfony)
     */
    private void syncProjetUtilisateur(int projetId, List<UserWrapper> membres) {
        if (membres.isEmpty()) return;
        Connection conn = MyDataBase.getInstance().getCnx();
        String sql = "INSERT IGNORE INTO projet_utilisateur (projet_id, utilisateur_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (UserWrapper u : membres) {
                ps.setInt(1, projetId);
                ps.setInt(2, u.id);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("✅ projet_utilisateur synchronisé pour projet id=" + projetId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur sync projet_utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bloquerDatesPassees(DatePicker picker) {
        picker.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    @FXML private void annuler() { ((Stage) tfNom.getScene().getWindow()).close(); }
    private void showAlert(Alert.AlertType type, String msg) { new Alert(type, msg).showAndWait(); }
}