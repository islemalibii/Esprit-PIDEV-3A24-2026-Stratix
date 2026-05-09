package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.CategorieService;
import models.UserRole;
import services.CategorieServiceService;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutCategorieController implements Initializable {

    @FXML private Label lblTitrePopup;
    @FXML private Label lblError;
    @FXML private TextField txtNom;
    @FXML private TextArea txtDescription;
    @FXML private Button btnAction;

    private CategorieServiceService categorieService;
    private CategorieService categorieAModifier;
    private boolean modeModification = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (UserRole.getInstance().isEmployee()) {
            btnAction.setVisible(false);
            btnAction.setManaged(false);

        }
    }

    public void setCategorieService(CategorieServiceService categorieService) {
        this.categorieService = categorieService;
    }

    public void setCategorieAModifier(CategorieService categorie) {
        this.categorieAModifier = categorie;
        this.modeModification = true;
        lblTitrePopup.setText("MODIFIER CATÉGORIE");
        btnAction.setText("Modifier");
        txtNom.setText(categorie.getNom());
        txtDescription.setText(categorie.getDescription());
    }

    @FXML
    private void handleAjouter() {
        lblError.setText("");
        if (!validerChamps()) return;
        try {
            if (modeModification) {
                categorieAModifier.setNom(txtNom.getText());
                categorieAModifier.setDescription(txtDescription.getText());
                categorieService.modifier(categorieAModifier);
                showAlert("Succès", "Catégorie modifiée!");
            } else {
                CategorieService categorie = new CategorieService();
                categorie.setNom(txtNom.getText());
                categorie.setDescription(txtDescription.getText());
                categorieService.ajouter(categorie);
                showAlert("Succès", "Catégorie ajoutée!");
            }
            fermer();
        } catch (SQLException e) {
            lblError.setText("Erreur SQL: " + e.getMessage());
        }
    }

    private boolean validerChamps() {
        if (txtNom.getText().trim().isEmpty()) {
            lblError.setText("Le nom est obligatoire");
            return false;
        }
        return true;
    }

    @FXML
    private void handleFermer() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}