package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.produit;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DetailsProduitController implements Initializable {

    @FXML
    private Label titreLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Label nomLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label categorieLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label stockLabel;
    @FXML
    private Label dateCreationLabel;
    @FXML
    private Label dateFabricationLabel;
    @FXML
    private Label datePeremptionLabel;
    @FXML
    private Label dateGarantieLabel;
    @FXML
    private Label ressourcesLabel;
    @FXML
    private TextArea detailsLabel;  // ← Changé de Label à TextArea
    @FXML
    private Label statutLabel;

    private produit produit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation
    }

    public void setProduit(produit produit) {
        this.produit = produit;
        afficherDetails();
    }

    private void afficherDetails() {
        if (produit == null) return;

        titreLabel.setText("Détails du produit : " + produit.getNom());
        nomLabel.setText(produit.getNom() != null ? produit.getNom() : "-");
        descriptionLabel.setText(produit.getDescription() != null ? produit.getDescription() : "-");
        categorieLabel.setText(produit.getCategorie() != null ? produit.getCategorie() : "-");
        prixLabel.setText(String.format("%.2f DT", produit.getPrix()));
        stockLabel.setText(produit.getStock_actuel() + " (min: " + produit.getStock_min() + ")");
        dateCreationLabel.setText(produit.getDate_creation() != null ? produit.getDate_creation() : "-");
        dateFabricationLabel.setText(produit.getDate_fabrication() != null ? produit.getDate_fabrication() : "-");
        datePeremptionLabel.setText(produit.getDate_peremption() != null ? produit.getDate_peremption() : "-");
        dateGarantieLabel.setText(produit.getDate_garantie() != null ? produit.getDate_garantie() : "-");
        ressourcesLabel.setText(produit.getRessources_necessaires() != null ? produit.getRessources_necessaires() : "-");
        detailsLabel.setText(produit.getDetails() != null ? produit.getDetails() : "Aucun détail supplémentaire");

        // Statut
        String statut;
        String style;
        if (produit.estPerime()) {
            statut = "⚠ PRODUIT PÉRIMÉ ⚠";
            style = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 5; -fx-background-radius: 5;";
        } else if (produit.estBientotPerime(30)) {
            statut = "⚠ Bientôt périmé";
            style = "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-padding: 5; -fx-background-radius: 5;";
        } else if (produit.garantieExpiree()) {
            statut = "⚠ Garantie expirée";
            style = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 5; -fx-background-radius: 5;";
        } else if (produit.getStock_actuel() <= produit.getStock_min()) {
            statut = "⚠ Stock faible";
            style = "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-padding: 5; -fx-background-radius: 5;";
        } else {
            statut = "✓ Produit en bon état";
            style = "-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-padding: 5; -fx-background-radius: 5;";
        }
        statutLabel.setText(statut);
        statutLabel.setStyle(style);

        // Image
        chargerImage();
    }

    private void chargerImage() {
        String imagePath = produit.getImage_path();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), 150, 150, true, true);
                    imageView.setImage(image);
                } else {
                    setDefaultImage();
                }
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-product.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
}