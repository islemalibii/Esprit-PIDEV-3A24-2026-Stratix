package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Ressource;
import services.service_ressource;

public class FormulaireRessourceController {

    @FXML
    private TextField idField;
    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private VBox autreTypeContainer; // Conteneur pour le type personnalisé
    @FXML
    private TextField autreTypeField; // Champ pour saisir le type personnalisé
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField fournisseurField;
    @FXML
    private Label titreFormulaire;
    @FXML
    private Button btnValider;

    private service_ressource serviceRessource = new service_ressource();
    private Runnable onRessourceAjoutee;

    // Types de ressources prédéfinis
    private final String[] typesPredifinis = {
            "Matériel informatique",
            "Mobilier",
            "Fourniture de bureau",
            "Équipement",
            "Matière première",
            "Logiciel",
            "Autre"
    };

    @FXML
    public void initialize() {
        System.out.println("Formulaire ressource initialisé");

        // Initialisation de la ComboBox
        typeCombo.setItems(FXCollections.observableArrayList(typesPredifinis));

        // Vérifier que les composants existent (sécurité)
        if (autreTypeContainer != null && autreTypeField != null) {
            // Cacher le champ de type personnalisé au démarrage
            autreTypeContainer.setVisible(false);
            autreTypeContainer.setManaged(false);

            // Ajouter un listener pour gérer la sélection "Autre"
            typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if ("Autre".equals(newVal)) {
                    // Afficher le champ de type personnalisé
                    autreTypeContainer.setVisible(true);
                    autreTypeContainer.setManaged(true);

                    // Mettre le focus sur le champ personnalisé
                    autreTypeField.requestFocus();
                    autreTypeField.clear();
                } else {
                    // Cacher le champ de type personnalisé
                    autreTypeContainer.setVisible(false);
                    autreTypeContainer.setManaged(false);
                }
            });
        } else {
            System.err.println("Attention: les composants autreTypeContainer ou autreTypeField sont null");
        }
    }

    public void setModeAjout() {
        titreFormulaire.setText("Ajouter une ressource");
        btnValider.setText("Ajouter");

        // Vider les champs
        idField.clear();
        nomField.clear();
        typeCombo.getSelectionModel().clearSelection();
        if (autreTypeContainer != null) {
            autreTypeContainer.setVisible(false);
            autreTypeContainer.setManaged(false);
        }
        if (autreTypeField != null) {
            autreTypeField.clear();
        }
        quantiteField.clear();
        fournisseurField.clear();
    }

    public void setModeModification(Ressource r) {
        titreFormulaire.setText("Modifier une ressource");
        btnValider.setText("Modifier");

        idField.setText(String.valueOf(r.getid()));
        nomField.setText(r.getNom());

        // Gérer le type existant
        String typeExistant = r.getType_ressource();
        if (typeExistant != null && !typeExistant.isEmpty()) {
            // Vérifier si c'est un type prédéfini
            boolean estPredifini = false;
            for (String type : typesPredifinis) {
                if (type.equals(typeExistant)) {
                    estPredifini = true;
                    break;
                }
            }

            if (estPredifini) {
                // Type prédéfini
                typeCombo.setValue(typeExistant);
                if (autreTypeContainer != null) {
                    autreTypeContainer.setVisible(false);
                    autreTypeContainer.setManaged(false);
                }
            } else {
                // Type personnalisé
                typeCombo.setValue("Autre");
                if (autreTypeContainer != null) {
                    autreTypeContainer.setVisible(true);
                    autreTypeContainer.setManaged(true);
                }
                if (autreTypeField != null) {
                    autreTypeField.setText(typeExistant);
                }
            }
        }

        quantiteField.setText(String.valueOf(r.getQuatite()));
        fournisseurField.setText(r.getFournisseur());
    }

    public void setOnRessourceAjoutee(Runnable callback) {
        this.onRessourceAjoutee = callback;
    }

    /**
     * Récupère la valeur du type
     */
    private String getTypeValue() {
        String selection = typeCombo.getValue();

        if ("Autre".equals(selection)) {
            // Retourner la valeur du champ personnalisé
            if (autreTypeField != null) {
                String autreType = autreTypeField.getText();
                if (autreType != null && !autreType.trim().isEmpty()) {
                    return autreType.trim();
                }
            }
            return null; // Pas de type saisi
        } else {
            // Retourner le type sélectionné
            return selection;
        }
    }

    @FXML
    private void validerFormulaire() {
        if (!validerChamps()) {
            return;
        }

        try {
            Ressource r = new Ressource();

            if (idField.getText() != null && !idField.getText().isEmpty()) {
                r.setid(Integer.parseInt(idField.getText()));
            }

            r.setNom(nomField.getText().trim());
            r.setType_ressource(getTypeValue());
            r.setQuatite(Integer.parseInt(quantiteField.getText().trim()));
            r.setFournisseur(fournisseurField.getText().trim());

            if (idField.getText() == null || idField.getText().isEmpty()) {
                serviceRessource.add(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Ressource ajoutée avec succès !");
            } else {
                serviceRessource.update(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Ressource modifiée avec succès !");
            }

            if (onRessourceAjoutee != null) {
                onRessourceAjoutee.run();
            }

            fermerFormulaire();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerFormulaire() {
        fermerFormulaire();
    }

    private void fermerFormulaire() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validerChamps() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }

        // Validation du type
        String type = getTypeValue();
        if (type == null || type.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le type est obligatoire");
            return false;
        }

        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite < 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre valide");
            return false;
        }
        if (fournisseurField.getText() == null || fournisseurField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le fournisseur est obligatoire");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}