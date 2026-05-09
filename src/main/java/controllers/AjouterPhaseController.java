package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Phase;
import services.PhaseService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AjouterPhaseController {

    @FXML private Label labelTitre;
    @FXML private TextField fieldNom;
    @FXML private TextArea fieldObjectif;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextField fieldVelociteEstimee;
    @FXML private TextField fieldVelociteReelle;
    @FXML private Label labelErreur;

    private final PhaseService phaseService = new PhaseService();
    private Phase phaseEnEdition = null;
    private int projetId;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList("Planifié", "En cours", "Terminé", "Annulé"));
        comboStatut.setValue("Planifié");
    }

    public void setProjetId(int projetId) {
        this.projetId = projetId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setPhase(Phase phase) {
        this.phaseEnEdition = phase;
        labelTitre.setText("✏️ Modifier la phase");
        fieldNom.setText(phase.getNom());
        fieldObjectif.setText(phase.getObjectif() != null ? phase.getObjectif() : "");
        comboStatut.setValue(phase.getStatut());

        if (phase.getDateDebut() != null)
            dateDebut.setValue(phase.getDateDebut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if (phase.getDateFin() != null)
            dateFin.setValue(phase.getDateFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        if (phase.getVelociteEstimee() != null)
            fieldVelociteEstimee.setText(String.valueOf(phase.getVelociteEstimee()));
        if (phase.getVelociteReelle() != null)
            fieldVelociteReelle.setText(String.valueOf(phase.getVelociteReelle()));
    }

    @FXML
    private void handleSauvegarder() {
        if (!valider()) return;

        Phase phase = (phaseEnEdition != null) ? phaseEnEdition : new Phase();
        phase.setProjetId(projetId);
        phase.setNom(fieldNom.getText().trim());
        phase.setObjectif(fieldObjectif.getText().trim());
        phase.setStatut(comboStatut.getValue());
        phase.setDateDebut(toDate(dateDebut.getValue()));
        phase.setDateFin(toDate(dateFin.getValue()));

        // Vélocités optionnelles
        phase.setVelociteEstimee(parseDouble(fieldVelociteEstimee.getText()));
        phase.setVelociteReelle(parseDouble(fieldVelociteReelle.getText()));

        if (phaseEnEdition != null)
            phaseService.modifierPhase(phase);
        else
            phaseService.ajouterPhase(phase);

        if (onSuccess != null) onSuccess.run();
        fermer();
    }

    @FXML
    private void handleAnnuler() {
        fermer();
    }

    private boolean valider() {
        labelErreur.setText("");
        if (fieldNom.getText().trim().isEmpty()) {
            labelErreur.setText("⚠ Le nom de la phase est obligatoire.");
            return false;
        }
        if (dateDebut.getValue() == null) {
            labelErreur.setText("⚠ La date de début est obligatoire.");
            return false;
        }
        if (dateFin.getValue() == null) {
            labelErreur.setText("⚠ La date de fin est obligatoire.");
            return false;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            labelErreur.setText("⚠ La date de fin doit être après la date de début.");
            return false;
        }
        // Validation vélocités si remplies
        if (!fieldVelociteEstimee.getText().trim().isEmpty() && parseDouble(fieldVelociteEstimee.getText()) == null) {
            labelErreur.setText("⚠ Vélocité estimée invalide (nombre attendu).");
            return false;
        }
        if (!fieldVelociteReelle.getText().trim().isEmpty() && parseDouble(fieldVelociteReelle.getText()) == null) {
            labelErreur.setText("⚠ Vélocité réelle invalide (nombre attendu).");
            return false;
        }
        return true;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Double parseDouble(String text) {
        try {
            return (text == null || text.trim().isEmpty()) ? null : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void fermer() {
        ((Stage) fieldNom.getScene().getWindow()).close();
    }
}