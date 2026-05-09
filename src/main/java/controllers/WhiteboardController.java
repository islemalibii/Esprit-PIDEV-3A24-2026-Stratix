package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import models.Employe;
import models.Tache;
import services.EmployeeService;
import services.SERVICETache;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WhiteboardController {

    @FXML private VBox columnAFaire;
    @FXML private VBox columnEnCours;
    @FXML private VBox columnTerminee;

    @FXML private Label lblCountAFaire;
    @FXML private Label lblCountEnCours;
    @FXML private Label lblCountTerminee;

    private SERVICETache tacheService;
    private EmployeeService employeService;

    private Tache selectedTache;
    private ContextMenu contextMenu;

    // Pour le drag and drop
    private VBox dragPlaceholder;
    private boolean isDragging = false;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation WhiteboardController avec Drag & Drop ===");

        tacheService = new SERVICETache();
        employeService = new EmployeeService();

        // Créer un placeholder pour le drag
        dragPlaceholder = new VBox();
        dragPlaceholder.setPrefHeight(10);
        dragPlaceholder.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");
        dragPlaceholder.setVisible(false);
        dragPlaceholder.setManaged(false);

        // Vérifier que les composants ne sont pas null
        System.out.println("   columnAFaire: " + (columnAFaire != null ? "✅" : "❌"));
        System.out.println("   columnEnCours: " + (columnEnCours != null ? "✅" : "❌"));
        System.out.println("   columnTerminee: " + (columnTerminee != null ? "✅" : "❌"));

        // Vider les colonnes
        columnAFaire.getChildren().clear();
        columnEnCours.getChildren().clear();
        columnTerminee.getChildren().clear();

        // ⭐ CONFIGURER LE DRAG AND DROP ⭐
        setupDragAndDrop();

        // Charger les tâches
        chargerTaches();

        // Créer le menu contextuel
        contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("✏️ Modifier");
        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        MenuItem moveAFaire = new MenuItem("📌 Déplacer vers À FAIRE");
        MenuItem moveEnCours = new MenuItem("⚡ Déplacer vers EN COURS");
        MenuItem moveTerminee = new MenuItem("✅ Déplacer vers TERMINÉE");

        editItem.setOnAction(e -> editSelectedTache());
        deleteItem.setOnAction(e -> deleteSelectedTache());
        moveAFaire.setOnAction(e -> moveTacheTo("A_FAIRE"));
        moveEnCours.setOnAction(e -> moveTacheTo("EN_COURS"));
        moveTerminee.setOnAction(e -> moveTacheTo("TERMINEE"));

        contextMenu.getItems().addAll(editItem, deleteItem,
                new SeparatorMenuItem(),
                moveAFaire, moveEnCours, moveTerminee);
    }

    /**
     * ⭐ CONFIGURATION DU DRAG AND DROP ⭐
     */
    private void setupDragAndDrop() {
        // Configurer chaque colonne
        setupColumn(columnAFaire, "A_FAIRE");
        setupColumn(columnEnCours, "EN_COURS");
        setupColumn(columnTerminee, "TERMINEE");
    }

    /**
     * Configure une colonne pour le drag and drop
     */
    private void setupColumn(VBox column, String targetStatut) {
        // Quand on passe sur la colonne pendant un drag
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);

                // Calculer la position pour le placeholder
                double mouseY = event.getY();
                int insertIndex = 0;

                // Trouver où insérer le placeholder
                for (int i = 0; i < column.getChildren().size(); i++) {
                    if (column.getChildren().get(i) instanceof VBox) {
                        VBox child = (VBox) column.getChildren().get(i);
                        double childY = child.getLayoutY();
                        double childHeight = child.getHeight();

                        if (mouseY > childY && mouseY < childY + childHeight) {
                            insertIndex = i + 1;
                        }
                    }
                }

                // Retirer le placeholder s'il existe déjà
                if (column.getChildren().contains(dragPlaceholder)) {
                    column.getChildren().remove(dragPlaceholder);
                }

                // Ajouter le placeholder à la bonne position
                if (insertIndex < column.getChildren().size()) {
                    column.getChildren().add(insertIndex, dragPlaceholder);
                } else {
                    column.getChildren().add(dragPlaceholder);
                }

                dragPlaceholder.setVisible(true);
                dragPlaceholder.setManaged(true);
            }
            event.consume();
        });

        // Quand on quitte la colonne
        column.setOnDragExited(event -> {
            if (!isDragging) {
                dragPlaceholder.setVisible(false);
                dragPlaceholder.setManaged(false);
                if (column.getChildren().contains(dragPlaceholder)) {
                    column.getChildren().remove(dragPlaceholder);
                }
            }
            event.consume();
        });

        // ⭐ QUAND ON DÉPOSE DANS LA COLONNE - CHANGEMENT DE STATUT ET COULEUR ⭐
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                try {
                    int taskId = Integer.parseInt(db.getString());
                    Tache taskToMove = findTacheById(taskId);

                    if (taskToMove != null && !taskToMove.getStatut().equals(targetStatut)) {
                        // Afficher le changement
                        System.out.println("🔄 Déplacement de la tâche: " + taskToMove.getTitre());
                        System.out.println("   Ancien statut: " + taskToMove.getStatut() + " (" + getStatusColor(taskToMove.getStatut()) + ")");
                        System.out.println("   Nouveau statut: " + targetStatut + " (" + getStatusColor(targetStatut) + ")");

                        // Changer le statut dans la base de données
                        taskToMove.setStatut(targetStatut);
                        tacheService.updateTache(taskToMove);

                        // ⭐ RECHARGER LES CARTES POUR METTRE À JOUR LES COULEURS ⭐
                        chargerTaches();
                        success = true;

                        System.out.println("✅ Tâche déplacée avec succès - Nouvelle couleur: " + getStatusColor(targetStatut));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            // Cacher le placeholder
            dragPlaceholder.setVisible(false);
            dragPlaceholder.setManaged(false);
            if (column.getChildren().contains(dragPlaceholder)) {
                column.getChildren().remove(dragPlaceholder);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void chargerTaches() {
        columnAFaire.getChildren().clear();
        columnEnCours.getChildren().clear();
        columnTerminee.getChildren().clear();

        int countAFaire = 0;
        int countEnCours = 0;
        int countTerminee = 0;

        List<Tache> toutesTaches = tacheService.getAllTaches();
        System.out.println("   Nombre de tâches chargées: " + toutesTaches.size());

        for (Tache t : toutesTaches) {
            VBox card = createTaskCard(t);

            switch(t.getStatut()) {
                case "A_FAIRE":
                    columnAFaire.getChildren().add(card);
                    countAFaire++;
                    break;
                case "EN_COURS":
                    columnEnCours.getChildren().add(card);
                    countEnCours++;
                    break;
                case "TERMINEE":
                    columnTerminee.getChildren().add(card);
                    countTerminee++;
                    break;
                default:
                    System.out.println("   Statut inconnu: " + t.getStatut());
            }
        }

        lblCountAFaire.setText(String.valueOf(countAFaire));
        lblCountEnCours.setText(String.valueOf(countEnCours));
        lblCountTerminee.setText(String.valueOf(countTerminee));

        System.out.println("   À faire: " + countAFaire + " (🔴), En cours: " + countEnCours + " (🟠), Terminées: " + countTerminee + " (🟢)");
    }

    /**
     * ⭐ RETOURNE LA COULEUR DE FOND SELON LE STATUT ⭐
     * 🔴 ROUGE = À FAIRE
     * 🟠 ORANGE = EN COURS
     * 🟢 VERT = TERMINÉE
     */
    private String getStatusColor(String statut) {
        switch(statut) {
            case "A_FAIRE":
                return "#ffebee"; // Rouge très clair
            case "EN_COURS":
                return "#fff3e0"; // Orange très clair
            case "TERMINEE":
                return "#e8f5e8"; // Vert très clair
            default:
                return "white";
        }
    }

    /**
     * ⭐ RETOURNE LA COULEUR DE BORDURE SELON LE STATUT ⭐
     */
    private String getStatusBorderColor(String statut) {
        switch(statut) {
            case "A_FAIRE":
                return "#ef4444"; // Rouge
            case "EN_COURS":
                return "#f59e0b"; // Orange
            case "TERMINEE":
                return "#10b981"; // Vert
            default:
                return "#e0e0e0";
        }
    }

    /**
     * ⭐ RETOURNE LA COULEUR DU TEXTE SELON LE STATUT ⭐
     */
    private String getTextColor(String statut) {
        switch(statut) {
            case "A_FAIRE":
                return "#b91c1c"; // Rouge foncé
            case "EN_COURS":
                return "#b45309"; // Orange foncé
            case "TERMINEE":
                return "#065f46"; // Vert foncé
            default:
                return "#1e293b"; // Gris foncé
        }
    }

    private VBox createTaskCard(Tache t) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(250);

        // ⭐ COULEUR DE FOND SELON LE STATUT ⭐
        String backgroundColor = getStatusColor(t.getStatut());
        String borderColor = getStatusBorderColor(t.getStatut());

        card.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 8;");

        // ⭐ RENDRE LA CARTE DRAGGABLE ⭐
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();

            // Stocker l'ID de la tâche
            content.putString(String.valueOf(t.getId()));
            db.setContent(content);

            // Rendre la carte translucide pendant le drag
            card.setOpacity(0.5);

            // Prendre un snapshot de la carte pour l'affichage pendant le drag
            db.setDragView(card.snapshot(null, null));

            isDragging = true;

            event.consume();
        });

        card.setOnDragDone(event -> {
            // Remettre l'opacité normale
            card.setOpacity(1.0);
            isDragging = false;

            // Cacher le placeholder
            dragPlaceholder.setVisible(false);
            dragPlaceholder.setManaged(false);

            event.consume();
        });

        // Effet hover avec ombre
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        hoverShadow.setRadius(8);
        hoverShadow.setOffsetY(2);

        card.setOnMouseEntered(e -> {
            if (!isDragging) {
                card.setEffect(hoverShadow);
            }
        });

        card.setOnMouseExited(e -> {
            if (!isDragging) {
                card.setEffect(null);
            }
        });

        // ⭐ DÉFINIR LA COULEUR DU TEXTE SELON LE STATUT ⭐
        String textColor = getTextColor(t.getStatut());
        String secondaryTextColor = "#334155"; // Gris foncé pour les textes secondaires

        // Titre avec couleur foncée
        Label lblTitre = new Label(t.getTitre());
        lblTitre.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTitre.setWrapText(true);
        lblTitre.setStyle("-fx-text-fill: " + textColor + ";"); // Couleur selon le statut

        // Description avec couleur foncée
        Label lblDescription = new Label(t.getDescription() != null ? t.getDescription() : "");
        lblDescription.setFont(Font.font("Arial", 12));
        lblDescription.setWrapText(true);
        lblDescription.setStyle("-fx-text-fill: " + secondaryTextColor + ";"); // Gris foncé
        if (t.getDescription() != null && t.getDescription().length() > 50) {
            lblDescription.setText(t.getDescription().substring(0, 50) + "...");
        }

        // Informations employé
        Employe emp = employeService.getEmployeById(t.getEmployeId());
        String empInfo;
        if (emp != null) {
            empInfo = emp.getUsername() + " (ID: " + t.getEmployeId() + ")";
        } else {
            empInfo = "Employé " + t.getEmployeId();
        }

        HBox empBox = new HBox(5);
        empBox.setAlignment(Pos.CENTER_LEFT);
        Label lblEmpIcon = new Label("👤");

        // Employé avec couleur foncée
        Label lblEmp = new Label(empInfo);
        lblEmp.setFont(Font.font("Arial", 11));
        lblEmp.setStyle("-fx-text-fill: " + secondaryTextColor + ";"); // Gris foncé
        empBox.getChildren().addAll(lblEmpIcon, lblEmp);

        // Deadline
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label lblDateIcon = new Label("📅");

        // Date avec couleur foncée
        Label lblDate = new Label(t.getDeadline().toLocalDate().format(formatter));
        lblDate.setFont(Font.font("Arial", 11));
        lblDate.setStyle("-fx-text-fill: " + secondaryTextColor + ";"); // Gris foncé
        dateBox.getChildren().addAll(lblDateIcon, lblDate);

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e5e7eb;");

        // Boutons d'action
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            selectedTache = t;
            editSelectedTache();
        });

        Button btnDelete = new Button("🗑️");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            selectedTache = t;
            deleteSelectedTache();
        });

        actionBox.getChildren().addAll(btnEdit, btnDelete);

        // Assembler la carte
        card.getChildren().addAll(lblTitre, lblDescription, empBox, dateBox, separator, actionBox);

        // Rendre la carte cliquable pour le menu contextuel
        card.setOnContextMenuRequested(e -> {
            selectedTache = t;
            contextMenu.show(card, e.getScreenX(), e.getScreenY());
        });

        return card;
    }

    private Tache findTacheById(int id) {
        return tacheService.getAllTaches().stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void editSelectedTache() {
        if (selectedTache != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Modifier");
            alert.setHeaderText("Modifier la tâche: " + selectedTache.getTitre());
            alert.setContentText("Fonctionnalité de modification à implémenter");
            alert.showAndWait();
        }
    }

    private void deleteSelectedTache() {
        if (selectedTache != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer la tâche");
            confirm.setContentText("Voulez-vous vraiment supprimer \"" + selectedTache.getTitre() + "\" ?");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                tacheService.deleteTache(selectedTache.getId());
                chargerTaches();
                selectedTache = null;
            }
        }
    }

    private void moveTacheTo(String newStatut) {
        if (selectedTache != null) {
            selectedTache.setStatut(newStatut);
            tacheService.updateTache(selectedTache);
            chargerTaches();
            selectedTache = null;
        }
    }

    @FXML
    private void showDashboardFromButton() { loadView("/dashboard-view.fxml"); }

    @FXML
    private void showPlanningFromButton() { loadView("/PlanningListeView.fxml"); }

    @FXML
    private void showTachesFromButton() { loadView("/TacheListeView.fxml"); }

    @FXML
    private void showCalendarFromButton() { loadView("/calendar-view.fxml"); }

    @FXML
    private void showWhiteboardFromButton() { loadView("/WhiteboardView.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}