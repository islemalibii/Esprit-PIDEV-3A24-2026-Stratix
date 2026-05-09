package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Ressource;
import services.service_ressource;
import service.export.ExportExcelService;
import service.export.ExportPDFService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class RessourceController implements Initializable {

    @FXML
    private ListView<Ressource> listViewRessources;
    @FXML
    private Label statsTotal;
    @FXML
    private Label statsQuantiteTotale;
    @FXML
    private Label statsTypes;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnClearSearch;
    @FXML
    private Button btnAjoutRessource; // Nouveau bouton pour ajouter

    private service_ressource serviceRessource = new service_ressource();
    private ObservableList<Ressource> observableList = FXCollections.observableArrayList();
    private FilteredList<Ressource> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation du contrôleur des ressources...");

        configurerAffichageRessources();
        chargerDonnees();
        configurerRecherche();
        mettreAJourStatistiques();
    }

    private void configurerAffichageRessources() {
        // Configuration du ListView avec des cellules personnalisées
        listViewRessources.setCellFactory(param -> {
            RessourceListCell cell = new RessourceListCell();

            // Actions des boutons
            cell.getBtnModifier().setOnAction(event -> {
                Ressource selected = cell.getItem();
                if (selected != null) {
                    ouvrirModifierRessource(selected);
                }
            });

            cell.getBtnSupprimer().setOnAction(event -> {
                Ressource selected = cell.getItem();
                if (selected != null) {
                    supprimerRessource(selected);
                }
            });

            return cell;
        });

        // Permettre la sélection
        listViewRessources.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void configurerRecherche() {
        filteredData = new FilteredList<>(observableList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerRessources(newValue);
        });

        btnSearch.setOnAction(e -> filtrerRessources(searchField.getText()));
        btnClearSearch.setOnAction(e -> {
            searchField.clear();
            filtrerRessources("");
        });

        listViewRessources.setItems(filteredData);
    }

    private void filtrerRessources(String texteRecherche) {
        filteredData.setPredicate(ressource -> {
            if (texteRecherche == null || texteRecherche.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = texteRecherche.toLowerCase();

            if (ressource.getNom() != null && ressource.getNom().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (ressource.getType_ressource() != null &&
                    ressource.getType_ressource().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (ressource.getFournisseur() != null &&
                    ressource.getFournisseur().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });

        mettreAJourStatistiquesFiltrees();
    }

    private void mettreAJourStatistiquesFiltrees() {
        int total = filteredData.size();
        int quantiteTotale = filteredData.stream()
                .mapToInt(Ressource::getQuatite)
                .sum();
        long typesUniques = filteredData.stream()
                .map(Ressource::getType_ressource)
                .distinct()
                .count();

        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            statsTotal.setText("Résultats: " + total + " (sur " + observableList.size() + ")");
        } else {
            statsTotal.setText("Total ressources: " + total);
        }

        statsQuantiteTotale.setText("Quantité totale: " + quantiteTotale);
        statsTypes.setText("Types: " + typesUniques);
    }

    private void chargerDonnees() {
        List<Ressource> ressources = serviceRessource.getAll();
        observableList.setAll(ressources);
        if (filteredData != null) {
            filtrerRessources(searchField != null ? searchField.getText() : "");
        }
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = observableList.size();
        int quantiteTotale = observableList.stream()
                .mapToInt(Ressource::getQuatite)
                .sum();
        long typesUniques = observableList.stream()
                .map(Ressource::getType_ressource)
                .distinct()
                .count();

        statsTotal.setText("Total ressources: " + total);
        statsQuantiteTotale.setText("Quantité totale: " + quantiteTotale);
        statsTypes.setText("Types: " + typesUniques);
    }

    @FXML
    private void ouvrirAjoutRessource() {
        try {
            System.out.println("Ouverture du formulaire d'ajout de ressource...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterRessource.fxml"));
            Parent root = loader.load();

            FormulaireRessourceController controller = loader.getController();
            controller.setModeAjout();
            controller.setOnRessourceAjoutee(this::chargerDonnees);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une ressource");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirModifierRessource(Ressource selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterRessource.fxml"));
            Parent root = loader.load();

            FormulaireRessourceController controller = loader.getController();
            controller.setModeModification(selected);
            controller.setOnRessourceAjoutee(this::chargerDonnees);

            Stage stage = new Stage();
            stage.setTitle("Modifier une ressource");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void ouvrirModifierRessource() {
        Ressource selected = listViewRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une ressource à modifier");
            return;
        }
        ouvrirModifierRessource(selected);
    }

    private void supprimerRessource(Ressource selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la ressource");
        confirm.setContentText("Voulez-vous vraiment supprimer " + selected.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceRessource.delete(selected);
            chargerDonnees();
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Ressource supprimée avec succès !");
        }
    }

    @FXML
    private void supprimerRessource() {
        Ressource selected = listViewRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une ressource à supprimer");
            return;
        }
        supprimerRessource(selected);
    }

    @FXML
    private void rafraichirListe() {
        chargerDonnees();
        showAlert(Alert.AlertType.INFORMATION, "Info", "Liste rafraîchie !");
    }

    @FXML
    private void retourMenuPrincipal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/produit.fxml"));
            Stage stage = (Stage) listViewRessources.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Produits");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exporterRessourcesExcel() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );
            fileChooser.setInitialFileName("ressources.xlsx");

            File file = fileChooser.showSaveDialog(listViewRessources.getScene().getWindow());

            if (file != null) {
                ExportExcelService.exporterRessourcesVersExcel(observableList, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Export Excel réussi !\nFichier : " + file.getName());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'export Excel : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void exporterRessourcesPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("ressources.pdf");

            File file = fileChooser.showSaveDialog(listViewRessources.getScene().getWindow());

            if (file != null) {
                ExportPDFService.exporterRessourcesVersPDF(observableList, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Export PDF réussi !\nFichier : " + file.getName());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'export PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}