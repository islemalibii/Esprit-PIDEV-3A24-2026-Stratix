package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.produit;
import services.service_produit;
import service.export.ExportPDFService;
import service.export.ExportExcelService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProduitController {

    @FXML
    private ListView<produit> listViewProduits;
    @FXML
    private Label statsTotal;
    @FXML
    private Label statsStockFaible;
    @FXML
    private Label statsValeurStock;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnClearSearch;

    private service_produit serviceProduit = new service_produit();
    private ObservableList<produit> produitsList = FXCollections.observableArrayList();
    private FilteredList<produit> filteredData;
    private SortedList<produit> sortedData;
    private boolean triAscendant = true;

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur principal avec ListView...");

        debugPaths();
        configurerAffichageProduits();
        chargerProduits();
        configurerRechercheEtTri();
        mettreAJourStatistiques();
    }

    private void debugPaths() {
        System.out.println("=== DÉBOGAGE DES CHEMINS ===");
        System.out.println("Resource /fxml/Ressource.fxml: " + getClass().getResource("/fxml/Ressource.fxml"));
        System.out.println("Resource /fxml/detailsProduit.fxml: " + getClass().getResource("/fxml/detailsProduit.fxml"));
        System.out.println("Resource /detailsProduit.fxml: " + getClass().getResource("/detailsProduit.fxml"));
        System.out.println("==============================");
    }

    private void configurerRechercheEtTri() {
        // Créer la liste filtrée
        filteredData = new FilteredList<>(produitsList, p -> true);

        // Créer la liste triée à partir de la liste filtrée
        sortedData = new SortedList<>(filteredData);

        // Appliquer le tri initial (par défaut, tri par nom)
        sortedData.setComparator(Comparator.comparing(produit::getNom));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerProduits(newValue);
        });

        btnSearch.setOnAction(e -> filtrerProduits(searchField.getText()));
        btnClearSearch.setOnAction(e -> {
            searchField.clear();
            filtrerProduits("");
        });

        // Lier la ListView à la liste triée
        listViewProduits.setItems(sortedData);
    }

    private void filtrerProduits(String texteRecherche) {
        filteredData.setPredicate(produit -> {
            if (texteRecherche == null || texteRecherche.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = texteRecherche.toLowerCase();

            if (produit.getNom() != null && produit.getNom().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (produit.getCategorie() != null && produit.getCategorie().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });

        mettreAJourStatistiquesFiltrees();
    }

    @FXML
    private void trierParPrix() {
        triAscendant = !triAscendant;

        if (triAscendant) {
            sortedData.setComparator(Comparator.comparing(produit::getPrix));
            showAlert(Alert.AlertType.INFORMATION, "Tri", "Produits triés par prix (croissant)");
        } else {
            sortedData.setComparator(Comparator.comparing(produit::getPrix).reversed());
            showAlert(Alert.AlertType.INFORMATION, "Tri", "Produits triés par prix (décroissant)");
        }
    }

    @FXML
    private void trierParNom() {
        sortedData.setComparator(Comparator.comparing(produit::getNom));
        showAlert(Alert.AlertType.INFORMATION, "Tri", "Produits triés par nom");
        triAscendant = true; // Réinitialiser l'état du tri par prix
    }

    @FXML
    private void trierParStock() {
        sortedData.setComparator(Comparator.comparing(produit::getStock_actuel));
        showAlert(Alert.AlertType.INFORMATION, "Tri", "Produits triés par stock");
        triAscendant = true; // Réinitialiser l'état du tri par prix
    }

    private void mettreAJourStatistiquesFiltrees() {
        int total = filteredData.size();
        int stockFaible = 0;
        int produitsPerimes = 0;
        int produitsBientotPerimes = 0;
        double valeurStock = 0;

        for (produit p : filteredData) {
            if (p.getStock_actuel() <= p.getStock_min()) stockFaible++;
            if (p.estPerime()) produitsPerimes++;
            if (p.estBientotPerime(30)) produitsBientotPerimes++;
            valeurStock += p.getPrix() * p.getStock_actuel();
        }

        if (searchField.getText() != null && !searchField.getText().isEmpty()) {
            statsTotal.setText("Résultats: " + total + " (sur " + produitsList.size() + ")");
        } else {
            statsTotal.setText("Total produits: " + total);
        }

        statsStockFaible.setText("Stock faible: " + stockFaible);
        statsValeurStock.setText(String.format("Valeur stock: %.2f DT | Périmés: %d | Bientôt: %d",
                valeurStock, produitsPerimes, produitsBientotPerimes));
    }

    private void configurerAffichageProduits() {
        listViewProduits.setCellFactory(param -> new ListCell<produit>() {
            private final ImageView imageView = new ImageView();
            private final GridPane gridPane = new GridPane();
            private final VBox contentBox = new VBox(8);
            private final HBox cellBox = new HBox(15);
            private final HBox buttonBox = new HBox(15);
            private final HBox headerBox = new HBox(10);

            private final Label nomValeur = new Label();
            private final Label descriptionValeur = new Label();
            private final Label categorieValeur = new Label();
            private final Label prixValeur = new Label();
            private final Label stockValeur = new Label();
            private final Label dateCreationValeur = new Label();
            private final Label dateFabricationValeur = new Label();
            private final Label datePeremptionValeur = new Label();
            private final Label dateGarantieValeur = new Label();
            private final Label ressourcesValeur = new Label();
            private final Label statutLabel = new Label();

            private final Button btnDetails = new Button("🔍 Détails");
            private final Button btnModifier = new Button("✏ Modifier");
            private final Button btnSupprimer = new Button("🗑 Retirer");

            {
                // Image
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

                // Styles
                String valeurStyle = "-fx-text-fill: #2c3e50; -fx-font-size: 12px;";
                nomValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
                descriptionValeur.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");
                categorieValeur.setStyle(valeurStyle);
                prixValeur.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                stockValeur.setStyle(valeurStyle);
                dateCreationValeur.setStyle(valeurStyle);
                dateFabricationValeur.setStyle(valeurStyle);
                datePeremptionValeur.setStyle(valeurStyle);
                dateGarantieValeur.setStyle(valeurStyle);
                ressourcesValeur.setStyle(valeurStyle);

                // Style des boutons
                btnDetails.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 15; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                );

                btnModifier.setStyle(
                        "-fx-background-color: #f39c12; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 15; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                );

                btnSupprimer.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 15; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                );

                // Actions des boutons
                btnDetails.setOnAction(event -> {
                    produit selected = getItem();
                    if (selected != null) ouvrirDetailsProduit(selected);
                });

                btnModifier.setOnAction(event -> {
                    produit selected = getItem();
                    if (selected != null) ouvrirModifierProduit(selected);
                });

                btnSupprimer.setOnAction(event -> {
                    produit selected = getItem();
                    if (selected != null) supprimerProduit(selected);
                });

                descriptionValeur.setWrapText(false);
                descriptionValeur.setMaxWidth(300);

                // Configuration du GridPane
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(25);
                col1.setHalignment(javafx.geometry.HPos.RIGHT);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(75);
                col2.setHalignment(javafx.geometry.HPos.LEFT);

                gridPane.getColumnConstraints().addAll(col1, col2);

                gridPane.setHgap(8);
                gridPane.setVgap(3);
                gridPane.setPadding(new Insets(5));
                gridPane.setMaxWidth(Double.MAX_VALUE);

                // Ligne d'en-tête
                headerBox.getChildren().addAll(nomValeur, prixValeur);
                headerBox.setAlignment(Pos.CENTER_LEFT);

                int row = 0;
                gridPane.add(headerBox, 0, row, 2, 1);
                row++;

                if (descriptionValeur.getText() != null && !descriptionValeur.getText().isEmpty()) {
                    gridPane.add(descriptionValeur, 0, row, 2, 1);
                    row++;
                }

                ajouterLigne(gridPane, "Catégorie:", categorieValeur, row++);
                ajouterLigne(gridPane, "Stock:", stockValeur, row++);
                ajouterLigne(gridPane, "Création:", dateCreationValeur, row++);
                ajouterLigne(gridPane, "Fabrication:", dateFabricationValeur, row++);
                ajouterLigne(gridPane, "Péremption:", datePeremptionValeur, row++);
                ajouterLigne(gridPane, "Garantie:", dateGarantieValeur, row++);
                ajouterLigne(gridPane, "Ressources:", ressourcesValeur, row++);

                // Boutons
                buttonBox.getChildren().addAll(btnDetails, btnModifier, btnSupprimer);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setSpacing(15);

                // Statut
                statutLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                statutLabel.setMaxWidth(Double.MAX_VALUE);
                statutLabel.setAlignment(Pos.CENTER);

                contentBox.getChildren().addAll(gridPane, statutLabel, buttonBox);
                contentBox.setSpacing(10);
                contentBox.setMaxWidth(Double.MAX_VALUE);

                cellBox.getChildren().addAll(imageView, contentBox);
                cellBox.setStyle("-fx-padding: 15; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-background-color: white;");
                cellBox.setSpacing(15);
                HBox.setHgrow(contentBox, Priority.ALWAYS);
            }

            private void ajouterLigne(GridPane grid, String label, Label valeur, int row) {
                Label lbl = new Label(label);
                lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                grid.add(lbl, 0, row);
                grid.add(valeur, 1, row);
            }

            @Override
            protected void updateItem(produit produit, boolean empty) {
                super.updateItem(produit, empty);
                if (empty || produit == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Valeurs
                    nomValeur.setText(produit.getNom());
                    descriptionValeur.setText(produit.getDescription() != null ?
                            (produit.getDescription().length() > 60 ? produit.getDescription().substring(0, 57) + "..." : produit.getDescription()) : "");
                    categorieValeur.setText(produit.getCategorie() != null ? produit.getCategorie() : "-");
                    prixValeur.setText(String.format("%.0f DT", produit.getPrix()));
                    stockValeur.setText(produit.getStock_actuel() + " (min " + produit.getStock_min() + ")");
                    dateCreationValeur.setText(produit.getDate_creation() != null ? produit.getDate_creation() : "-");
                    dateFabricationValeur.setText(produit.getDate_fabrication() != null ? produit.getDate_fabrication() : "-");
                    datePeremptionValeur.setText(produit.getDate_peremption() != null ? produit.getDate_peremption() : "-");
                    dateGarantieValeur.setText(produit.getDate_garantie() != null ? produit.getDate_garantie() : "-");
                    ressourcesValeur.setText(produit.getRessources_necessaires() != null ?
                            (produit.getRessources_necessaires().length() > 40 ? produit.getRessources_necessaires().substring(0, 37) + "..." : produit.getRessources_necessaires()) : "-");

                    // Statut
                    String statut;
                    String couleur;
                    String fond;

                    if (produit.estPerime()) {
                        statut = "⚠ PRODUIT PÉRIMÉ ⚠";
                        couleur = "#c62828";
                        fond = "#ffebee";
                    } else if (produit.estBientotPerime(30)) {
                        statut = "⚠ Bientôt périmé";
                        couleur = "#ef6c00";
                        fond = "#fff3e0";
                    } else if (produit.garantieExpiree()) {
                        statut = "⚠ Garantie expirée";
                        couleur = "#c62828";
                        fond = "#ffebee";
                    } else if (produit.getStock_actuel() <= produit.getStock_min()) {
                        statut = "⚠ Stock faible";
                        couleur = "#ef6c00";
                        fond = "#fff3e0";
                    } else {
                        statut = "✓ Disponible";
                        couleur = "#2e7d32";
                        fond = "#e8f5e8";
                    }

                    statutLabel.setText(statut);
                    statutLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold; -fx-font-size: 12px; " +
                            "-fx-background-color: " + fond + "; -fx-padding: 5; -fx-background-radius: 5;");

                    chargerImageProduit(produit, imageView);
                    ajouterTooltipImage(imageView, produit);
                    setGraphic(cellBox);
                }
            }
        });
    }

    private void ouvrirDetailsProduit(produit selected) {
        try {
            URL fxmlUrl = getClass().getResource("/detailsProduit.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier detailsProduit.fxml introuvable!\n" +
                                "Vérifiez qu'il est dans src/main/resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            DetailsProduitController controller = loader.getController();
            controller.setProduit(selected);

            Stage stage = new Stage();
            stage.setTitle("Détails du produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerImageProduit(produit produit, ImageView imageView) {
        String imagePath = produit.getImage_path();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    imageView.setImage(image);
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
            imageView.setStyle("-fx-background-color: #f0f0f0;");
        }
    }

    private void ajouterTooltipImage(ImageView imageView, produit produit) {
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(new ImageView());
        tooltip.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-padding: 5;");

        imageView.setOnMouseEntered(event -> {
            if (produit.getImage_path() != null && !produit.getImage_path().isEmpty()) {
                try {
                    File imageFile = new File(produit.getImage_path());
                    if (imageFile.exists()) {
                        Image fullImage = new Image(imageFile.toURI().toString(), 200, 200, true, true);
                        ImageView fullImageView = new ImageView(fullImage);
                        fullImageView.setPreserveRatio(true);
                        tooltip.setGraphic(fullImageView);
                        tooltip.show(imageView, event.getScreenX() + 10, event.getScreenY() + 10);
                    }
                } catch (Exception e) {}
            }
        });
        imageView.setOnMouseExited(event -> tooltip.hide());
    }

    private void chargerProduits() {
        try {
            List<produit> produits = serviceProduit.getAll();
            produitsList.clear();
            produitsList.addAll(produits);
            System.out.println(produits.size() + " produits chargés");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    private void mettreAJourStatistiques() {
        int total = produitsList.size();
        int stockFaible = 0;
        int produitsPerimes = 0;
        int produitsBientotPerimes = 0;
        double valeurStock = 0;

        for (produit p : produitsList) {
            if (p.getStock_actuel() <= p.getStock_min()) stockFaible++;
            if (p.estPerime()) produitsPerimes++;
            if (p.estBientotPerime(30)) produitsBientotPerimes++;
            valeurStock += p.getPrix() * p.getStock_actuel();
        }

        statsTotal.setText("Total produits: " + total);
        statsStockFaible.setText("Stock faible: " + stockFaible);
        statsValeurStock.setText(String.format("Valeur stock: %.2f DT | Périmés: %d | Bientôt: %d",
                valeurStock, produitsPerimes, produitsBientotPerimes));
    }

    @FXML
    private void ouvrirAjoutProduit() {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/ajouterProduit.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("/ajouterProduit.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier ajouterProduit.fxml introuvable!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            FormulaireProduitController controller = loader.getController();
            controller.setModeAjout();
            controller.setOnProduitAjoute(this::rafraichirListe);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirModifierProduit(produit selected) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/ajouterProduit.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("/ajouterProduit.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            FormulaireProduitController controller = loader.getController();
            controller.setModeModification(selected);
            controller.setOnProduitAjoute(this::rafraichirListe);

            Stage stage = new Stage();
            stage.setTitle("Modifier un produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void supprimerProduit(produit selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Voulez-vous vraiment supprimer le produit \"" + selected.getNom() + "\" ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (selected.getImage_path() != null && !selected.getImage_path().isEmpty()) {
                    File imageFile = new File(selected.getImage_path());
                    if (imageFile.exists()) imageFile.delete();
                }
                serviceProduit.delete(selected);
                rafraichirListe();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le produit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void ouvrirModifierProduit() {
        produit selected = listViewProduits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit à modifier");
            return;
        }
        ouvrirModifierProduit(selected);
    }

    @FXML
    private void supprimerProduit() {
        produit selected = listViewProduits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit à supprimer");
            return;
        }
        supprimerProduit(selected);
    }

    @FXML
    private void rafraichirListe() {
        chargerProduits();
        filtrerProduits(searchField.getText());
        mettreAJourStatistiques();
    }

    @FXML
    private void naviguerRessources() {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/Ressource.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("/Ressource.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier Ressource.fxml introuvable!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) listViewProduits.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des ressources");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer: " + e.getMessage());
        }
    }

    @FXML private void naviguerEmployes() { System.out.println("Navigation vers Employés"); }
    @FXML private void naviguerProjets() { System.out.println("Navigation vers Projets"); }
    @FXML private void naviguerTaches() { System.out.println("Navigation vers Tâches"); }
    @FXML private void naviguerProduits() { System.out.println("Déjà sur Produits"); }
    @FXML private void naviguerPrevoyance() { System.out.println("Navigation vers Prévoyance"); }
    @FXML private void naviguerAnalyse() { System.out.println("Navigation vers Analyse"); }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void exporterProduitsExcel() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );
            fileChooser.setInitialFileName("produits.xlsx");

            File file = fileChooser.showSaveDialog(listViewProduits.getScene().getWindow());

            if (file != null) {
                ExportExcelService.exporterProduitsVersExcel(produitsList, file.getAbsolutePath());
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
    private void exporterProduitsPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("produits.pdf");

            File file = fileChooser.showSaveDialog(listViewProduits.getScene().getWindow());

            if (file != null) {
                ExportPDFService.exporterProduitsVersPDF(produitsList, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Export PDF réussi !\nFichier : " + file.getName());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'export PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}