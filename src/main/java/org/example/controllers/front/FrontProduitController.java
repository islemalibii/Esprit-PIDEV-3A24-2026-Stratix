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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.produit;
import services.service_produit;

import javafx.geometry.Insets;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FrontProduitController implements Initializable {

    @FXML
    private ListView<produit> listViewProduits;
    @FXML
    private Label statsTotal;
    @FXML
    private Label statsStockDisponible;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnClearSearch;

    private service_produit serviceProduit = new service_produit();
    private ObservableList<produit> produitsList = FXCollections.observableArrayList();
    private FilteredList<produit> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation du Front Office Produits...");

        // Débogage des chemins
        debugResourcePaths();

        configurerAffichageProduits();
        chargerProduits();
        configurerRecherche();
        mettreAJourStatistiques();
    }

    /**
     * Méthode de débogage pour trouver les chemins des fichiers FXML
     */
    private void debugResourcePaths() {
        System.out.println("=== DÉBOGAGE DES CHEMINS FRONT OFFICE ===");
        System.out.println("Resource /fxml/front/frontRessources.fxml: " +
                getClass().getResource("/fxml/front/frontRessources.fxml"));
        System.out.println("Resource /front/frontRessources.fxml: " +
                getClass().getResource("/front/frontRessources.fxml"));
        System.out.println("Resource /fxml/frontRessources.fxml: " +
                getClass().getResource("/fxml/frontRessources.fxml"));
        System.out.println("Resource /frontRessources.fxml: " +
                getClass().getResource("/frontRessources.fxml"));
        System.out.println("Resource /produit.fxml: " +
                getClass().getResource("/produit.fxml"));
        System.out.println("Resource /fxml/produit.fxml: " +
                getClass().getResource("/fxml/produit.fxml"));
        System.out.println("==========================================");
    }

    private void configurerRecherche() {
        filteredData = new FilteredList<>(produitsList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerProduits(newValue);
        });

        btnSearch.setOnAction(e -> filtrerProduits(searchField.getText()));
        btnClearSearch.setOnAction(e -> {
            searchField.clear();
            filtrerProduits("");
        });

        listViewProduits.setItems(filteredData);
    }

    private void filtrerProduits(String texteRecherche) {
        filteredData.setPredicate(produit -> {
            if (texteRecherche == null || texteRecherche.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = texteRecherche.toLowerCase();
            return (produit.getNom() != null && produit.getNom().toLowerCase().contains(lowerCaseFilter)) ||
                    (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(lowerCaseFilter)) ||
                    (produit.getCategorie() != null && produit.getCategorie().toLowerCase().contains(lowerCaseFilter));
        });
        mettreAJourStatistiques();
    }

    private void configurerAffichageProduits() {
        listViewProduits.setCellFactory(param -> new ListCell<produit>() {
            private final ImageView imageView = new ImageView();
            private final GridPane gridPane = new GridPane();
            private final VBox contentBox = new VBox(5);
            private final HBox cellBox = new HBox(15);

            private final Label nomValeur = new Label();
            private final Label descriptionValeur = new Label();
            private final Label categorieValeur = new Label();
            private final Label prixValeur = new Label();
            private final Label stockValeur = new Label();
            private final Label disponibiliteLabel = new Label();

            {
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

                nomValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
                descriptionValeur.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");
                categorieValeur.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
                prixValeur.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;");

                disponibiliteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                disponibiliteLabel.setMaxWidth(Double.MAX_VALUE);
                disponibiliteLabel.setAlignment(javafx.geometry.Pos.CENTER);

                descriptionValeur.setWrapText(true);
                descriptionValeur.setMaxWidth(300);

                gridPane.setHgap(10);
                gridPane.setVgap(5);
                gridPane.setPadding(new Insets(5));

                int row = 0;
                gridPane.add(nomValeur, 0, row, 2, 1);
                row++;
                gridPane.add(descriptionValeur, 0, row, 2, 1);
                row++;
                gridPane.add(new Label("Catégorie: "), 0, row);
                gridPane.add(categorieValeur, 1, row);
                row++;
                gridPane.add(new Label("Prix: "), 0, row);
                gridPane.add(prixValeur, 1, row);
                row++;
                gridPane.add(new Label("Stock: "), 0, row);
                gridPane.add(stockValeur, 1, row);

                contentBox.getChildren().addAll(gridPane, disponibiliteLabel);
                contentBox.setSpacing(10);
                cellBox.getChildren().addAll(imageView, contentBox);
                cellBox.setStyle("-fx-padding: 15; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-background-color: white;");
                cellBox.setSpacing(20);
            }

            @Override
            protected void updateItem(produit produit, boolean empty) {
                super.updateItem(produit, empty);
                if (empty || produit == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nomValeur.setText(produit.getNom());
                    descriptionValeur.setText(produit.getDescription() != null ? produit.getDescription() : "");
                    categorieValeur.setText(produit.getCategorie() != null ? produit.getCategorie() : "Non spécifiée");
                    prixValeur.setText(String.format("%.2f DT", produit.getPrix()));
                    stockValeur.setText(String.valueOf(produit.getStock_actuel()));

                    // Statut de disponibilité
                    String disponibilite;
                    String style;
                    if (produit.getStock_actuel() <= 0) {
                        disponibilite = "⚠ RUPTURE DE STOCK";
                        style = "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 5; -fx-background-radius: 5;";
                    } else if (produit.getStock_actuel() <= produit.getStock_min()) {
                        disponibilite = "⚠ Stock faible";
                        style = "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-padding: 5; -fx-background-radius: 5;";
                    } else {
                        disponibilite = "✓ En stock";
                        style = "-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-padding: 5; -fx-background-radius: 5;";
                    }
                    disponibiliteLabel.setText(disponibilite);
                    disponibiliteLabel.setStyle(style);

                    chargerImageProduit(produit, imageView);
                    setGraphic(cellBox);
                }
            }
        });
    }

    private void chargerImageProduit(produit produit, ImageView imageView) {
        String imagePath = produit.getImage_path();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString(), true));
                } else {
                    setDefaultImage(imageView);
                }
            } catch (Exception e) {
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-product.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }

    private void chargerProduits() {
        try {
            List<produit> produits = serviceProduit.getAll();
            produitsList.clear();
            produitsList.addAll(produits);
            System.out.println(produits.size() + " produits chargés");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les produits");
        }
    }

    private void mettreAJourStatistiques() {
        int total = produitsList.size();
        int stockDisponible = produitsList.stream()
                .filter(p -> p.getStock_actuel() > 0)
                .mapToInt(p -> 1)
                .sum();

        statsTotal.setText("Total produits: " + total);
        statsStockDisponible.setText("Produits disponibles: " + stockDisponible);
    }

    /**
     * Navigation vers la page des ressources (version améliorée)
     */
    @FXML
    private void voirRessources() {
        try {
            // Essayer différents chemins possibles
            URL fxmlUrl = null;
            String[] chemins = {
                    "/fxml/front/frontRessources.fxml",
                    "/front/frontRessources.fxml",
                    "/fxml/frontRessources.fxml",
                    "/frontRessources.fxml"
            };

            for (String chemin : chemins) {
                fxmlUrl = getClass().getResource(chemin);
                if (fxmlUrl != null) {
                    System.out.println("Fichier ressources trouvé: " + chemin);
                    break;
                }
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier frontRessources.fxml introuvable!\n" +
                                "Vérifiez qu'il est dans src/main/resources/fxml/front/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) listViewProduits.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Front Office - Ressources");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page des ressources: " + e.getMessage());
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
                                "Vérifiez qu'il est dans src/main/resources/fxml/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) listViewProduits.getScene().getWindow();
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