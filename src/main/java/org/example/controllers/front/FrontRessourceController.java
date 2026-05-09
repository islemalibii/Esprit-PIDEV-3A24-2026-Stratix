package org.example.controllers.front;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Ressource;
import services.service_ressource;

import javafx.geometry.Insets;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FrontRessourceController implements Initializable {

    @FXML
    private ListView<Ressource> listViewRessources;
    @FXML
    private Label statsTotal;
    @FXML
    private Label statsQuantiteTotale;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnClearSearch;

    private service_ressource serviceRessource = new service_ressource();
    private ObservableList<Ressource> ressourcesList = FXCollections.observableArrayList();
    private FilteredList<Ressource> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation du Front Office Ressources...");

        // Débogage des chemins
        debugResourcePaths();

        configurerAffichageRessources();
        chargerRessources();
        configurerRecherche();
        mettreAJourStatistiques();
    }

    /**
     * Méthode de débogage pour trouver les chemins des fichiers FXML
     */
    private void debugResourcePaths() {
        System.out.println("=== DÉBOGAGE DES CHEMINS FRONT OFFICE (RESSOURCES) ===");
        System.out.println("Resource /fxml/front/frontProduits.fxml: " +
                getClass().getResource("/fxml/front/frontProduits.fxml"));
        System.out.println("Resource /front/frontProduits.fxml: " +
                getClass().getResource("/front/frontProduits.fxml"));
        System.out.println("Resource /fxml/frontProduits.fxml: " +
                getClass().getResource("/fxml/frontProduits.fxml"));
        System.out.println("Resource /frontProduits.fxml: " +
                getClass().getResource("/frontProduits.fxml"));
        System.out.println("====================================================");
    }

    private void configurerRecherche() {
        filteredData = new FilteredList<>(ressourcesList, p -> true);

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
            return (ressource.getNom() != null && ressource.getNom().toLowerCase().contains(lowerCaseFilter)) ||
                    (ressource.getType_ressource() != null && ressource.getType_ressource().toLowerCase().contains(lowerCaseFilter)) ||
                    (ressource.getFournisseur() != null && ressource.getFournisseur().toLowerCase().contains(lowerCaseFilter));
        });
        mettreAJourStatistiques();
    }

    private void configurerAffichageRessources() {
        listViewRessources.setCellFactory(param -> new ListCell<Ressource>() {
            private final GridPane gridPane = new GridPane();
            private final VBox contentBox = new VBox(5);
            private final HBox cellBox = new HBox(15);

            private final Label nomValeur = new Label();
            private final Label typeValeur = new Label();
            private final Label fournisseurValeur = new Label();
            private final Label quantiteValeur = new Label();
            private final Label disponibiliteLabel = new Label();

            {
                nomValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
                typeValeur.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px;");
                fournisseurValeur.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                quantiteValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                disponibiliteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                disponibiliteLabel.setMaxWidth(Double.MAX_VALUE);
                disponibiliteLabel.setAlignment(javafx.geometry.Pos.CENTER);

                gridPane.setHgap(10);
                gridPane.setVgap(8);
                gridPane.setPadding(new Insets(5));

                int row = 0;
                gridPane.add(nomValeur, 0, row, 2, 1);
                row++;
                gridPane.add(new Label("Type: "), 0, row);
                gridPane.add(typeValeur, 1, row);
                row++;
                gridPane.add(new Label("Fournisseur: "), 0, row);
                gridPane.add(fournisseurValeur, 1, row);
                row++;
                gridPane.add(new Label("Quantité: "), 0, row);
                gridPane.add(quantiteValeur, 1, row);

                contentBox.getChildren().addAll(gridPane, disponibiliteLabel);
                contentBox.setSpacing(10);
                cellBox.getChildren().add(contentBox);
                cellBox.setStyle("-fx-padding: 15; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-background-color: white; -fx-background-radius: 8;");
                cellBox.setSpacing(20);
            }

            @Override
            protected void updateItem(Ressource ressource, boolean empty) {
                super.updateItem(ressource, empty);
                if (empty || ressource == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nomValeur.setText(ressource.getNom() != null ? ressource.getNom() : "Non spécifié");
                    typeValeur.setText(ressource.getType_ressource() != null ? ressource.getType_ressource() : "Non spécifié");
                    fournisseurValeur.setText(ressource.getFournisseur() != null ? ressource.getFournisseur() : "Non spécifié");

                    int quantite = ressource.getQuatite();
                    quantiteValeur.setText(String.valueOf(quantite));

                    // Statut de disponibilité
                    String disponibilite;
                    String style;
                    String quantiteStyle;

                    if (quantite <= 0) {
                        disponibilite = "⚠ RUPTURE DE STOCK";
                        style = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 5; -fx-background-radius: 5;";
                        quantiteStyle = "-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 16px;";
                    } else if (quantite <= 5) {
                        disponibilite = "⚠ Stock faible";
                        style = "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-padding: 5; -fx-background-radius: 5;";
                        quantiteStyle = "-fx-text-fill: #ef6c00; -fx-font-weight: bold; -fx-font-size: 16px;";
                    } else {
                        disponibilite = "✓ En stock";
                        style = "-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-padding: 5; -fx-background-radius: 5;";
                        quantiteStyle = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;";
                    }

                    disponibiliteLabel.setText(disponibilite);
                    disponibiliteLabel.setStyle(style);
                    quantiteValeur.setStyle(quantiteStyle);

                    setGraphic(cellBox);
                }
            }
        });
    }

    private void chargerRessources() {
        try {
            List<Ressource> ressources = serviceRessource.getAll();
            ressourcesList.clear();
            ressourcesList.addAll(ressources);
            System.out.println(ressources.size() + " ressources chargées");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ressources");
        }
    }

    private void mettreAJourStatistiques() {
        int total = ressourcesList.size();
        int quantiteTotale = ressourcesList.stream()
                .mapToInt(Ressource::getQuatite)
                .sum();

        statsTotal.setText("Total ressources: " + total);
        statsQuantiteTotale.setText("Quantité totale: " + quantiteTotale);
    }

    /**
     * Navigation vers la page des produits (version corrigée)
     */
    @FXML
    private void voirProduits() {
        try {
            // Essayer différents chemins possibles
            URL fxmlUrl = null;
            String[] chemins = {
                    "/front/frontProduits.fxml",
                    "/fxml/front/frontProduits.fxml",
                    "/frontProduits.fxml",
                    "/fxml/frontProduits.fxml"
            };

            for (String chemin : chemins) {
                fxmlUrl = getClass().getResource(chemin);
                if (fxmlUrl != null) {
                    System.out.println("Fichier produits trouvé: " + chemin);
                    break;
                }
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier frontProduits.fxml introuvable!\n" +
                                "Vérifiez qu'il est dans src/main/resources/front/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) listViewRessources.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Front Office - Produits");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retour vers l'administration
     */
    @FXML
    private void retournerAdministration() {
        try {
            // Essayer différents chemins possibles
            URL fxmlUrl = null;
            String[] chemins = {
                    "/produit.fxml",
                    "/fxml/produit.fxml",
                    "/fxml/produits.fxml",
                    "/produits.fxml"
            };

            for (String chemin : chemins) {
                fxmlUrl = getClass().getResource(chemin);
                if (fxmlUrl != null) {
                    System.out.println("Fichier administration trouvé: " + chemin);
                    break;
                }
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier produit.fxml introuvable!\n" +
                                "Vérifiez qu'il est dans src/main/resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) listViewRessources.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion - Administration");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner à l'administration: " + e.getMessage());
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