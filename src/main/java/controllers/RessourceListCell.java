package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import models.Ressource;

public class RessourceListCell extends ListCell<Ressource> {

    private final GridPane gridPane = new GridPane();
    private final VBox contentBox = new VBox(8);
    private final HBox cellBox = new HBox(15);
    private final HBox buttonBox = new HBox(15);
    private final HBox headerBox = new HBox(10);

    private final Label nomValeur = new Label();
    private final Label typeValeur = new Label();
    private final Label fournisseurValeur = new Label();
    private final Label quantiteValeur = new Label();      // Pour la valeur
    private final Label statutLabel = new Label();

    private final Button btnModifier = new Button("✏ Modifier");
    private final Button btnSupprimer = new Button("🗑 Retirer");

    public RessourceListCell() {
        // Styles
        String valeurStyle = "-fx-text-fill: #2c3e50; -fx-font-size: 12px;";
        nomValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        typeValeur.setStyle(valeurStyle);
        fournisseurValeur.setStyle(valeurStyle);
        quantiteValeur.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Style des boutons
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
        headerBox.getChildren().addAll(nomValeur);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        int row = 0;
        gridPane.add(headerBox, 0, row, 2, 1);
        row++;

        ajouterLigne(gridPane, "Type:", typeValeur, row++);
        ajouterLigne(gridPane, "Fournisseur:", fournisseurValeur, row++);
        ajouterLigne(gridPane, "Stock:", quantiteValeur, row++); // MODIFICATION ICI

        // Boutons
        buttonBox.getChildren().addAll(btnModifier, btnSupprimer);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setSpacing(15);

        // Statut
        statutLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        statutLabel.setMaxWidth(Double.MAX_VALUE);
        statutLabel.setAlignment(Pos.CENTER);

        contentBox.getChildren().addAll(gridPane, statutLabel, buttonBox);
        contentBox.setSpacing(10);
        contentBox.setMaxWidth(Double.MAX_VALUE);

        cellBox.getChildren().add(contentBox);
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

    public Button getBtnModifier() {
        return btnModifier;
    }

    public Button getBtnSupprimer() {
        return btnSupprimer;
    }

    @Override
    protected void updateItem(Ressource ressource, boolean empty) {
        super.updateItem(ressource, empty);

        if (empty || ressource == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Remplir les valeurs
            nomValeur.setText(ressource.getNom() != null ? ressource.getNom() : "-");
            typeValeur.setText(ressource.getType_ressource() != null ? ressource.getType_ressource() : "-");
            fournisseurValeur.setText(ressource.getFournisseur() != null ? ressource.getFournisseur() : "-");

            int quantite = ressource.getQuatite();
            quantiteValeur.setText(String.valueOf(quantite));

            // Statut avec couleur
            String statut;
            String couleur;
            String fond;

            if (quantite <= 0) {
                statut = "⚠ RUPTURE DE STOCK";
                couleur = "#c62828";
                fond = "#ffebee";
            } else if (quantite <= 5) {
                statut = "⚠ Stock faible";
                couleur = "#ef6c00";
                fond = "#fff3e0";
            } else {
                statut = "✓ Disponible";
                couleur = "#2e7d32";
                fond = "#e8f5e8";
            }

            statutLabel.setText(statut);
            statutLabel.setStyle(String.format(
                    "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12px; " +
                            "-fx-background-color: %s; -fx-padding: 5; -fx-background-radius: 5;",
                    couleur, fond
            ));

            // Couleur de la quantité
            if (quantite <= 5) {
                quantiteValeur.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                quantiteValeur.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
            }

            setGraphic(cellBox);
        }
    }
}