package controllers;

import javafx.scene.Node;
import services.UtilisateurService;
import services.ChatbotService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import models.*;
import utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardAdminController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label avatarLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private VBox cardsContainer;

    @FXML
    private Button logoutButton;

    @FXML
    private Button btnEvenements;

    @FXML
    private HBox userProfileSection;

    @FXML
    private VBox chatbotContainer;

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private TextField chatInputField;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private Button chatbotButton;
    @FXML
    private BorderPane main;

    private Utilisateur currentUser;
    private UtilisateurService utilisateurService;
    private services.ChatbotService chatbotService;

    private String currentView = "utilisateurs";
    private List<Utilisateur> currentData;
    private Node dashboardHomeView;



    public void initialize() {
        utilisateurService = UtilisateurService.getInstance();
        chatbotService = services.ChatbotService.getInstance();
        dashboardHomeView = main.getCenter();
        loadUtilisateurs();


        // Recherche en temps réel
        searchField.textProperty().addListener((obs, old, newVal) -> handleSearch(null));

        // Message de bienvenue du chatbot
        addBotMessage("👋 Bonjour! Je suis votre assistant. Tapez 'aide' pour voir ce que je peux faire.");
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        welcomeLabel.setText("Bienvenue, " + user.getNom() + " " + user.getPrenom());

        // Mettre à jour l'avatar avec la première lettre du prénom
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            avatarLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
        }
    }

    @FXML
    void showUtilisateurs(ActionEvent event) {
        main.setCenter(dashboardHomeView);
        currentView = "utilisateurs";
        titleLabel.setText("Gestion des Utilisateurs");
        loadUtilisateurs();
    }

    @FXML
    void showEmployes(ActionEvent event) {
        main.setCenter(dashboardHomeView);
        currentView = "employes";
        titleLabel.setText("Gestion des Employés");
        loadEmployes();
    }

    @FXML
    void showAdmins(ActionEvent event) {
        main.setCenter(dashboardHomeView);
        currentView = "admins";
        titleLabel.setText("Gestion des Admins");
        loadAdmins();
    }

    @FXML
    void showCEOs(ActionEvent event) {
        main.setCenter(dashboardHomeView);
        currentView = "ceos";
        titleLabel.setText("Gestion des CEOs");
        loadCEOs();
    }

    @FXML
    void showResponsables(ActionEvent event) {
        main.setCenter(dashboardHomeView);
        currentView = "responsables";
        titleLabel.setText("Gestion des Responsables");
        loadResponsables();
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        showAddDialog();
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            displayCards(currentData);
            return;
        }

        List<Utilisateur> filteredData = currentData.stream()
                .filter(user -> user.getNom().toLowerCase().contains(searchText) ||
                        user.getPrenom().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        displayCards(filteredData);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // Supprimer la session
        SessionManager.getInstance().logout();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleProfileClick() {
        if (currentUser == null) return;

        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Profil Utilisateur");
        dialog.setHeaderText(null);

        // Container principal
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(25);
        content.setPadding(new javafx.geometry.Insets(30));
        content.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #F7FAFC;");

        // Avatar circulaire centré
        javafx.scene.layout.StackPane avatarContainer = new javafx.scene.layout.StackPane();
        javafx.scene.shape.Circle avatarCircle = new javafx.scene.shape.Circle(60);
        avatarCircle.setFill(javafx.scene.paint.Color.web("#4299E1"));
        avatarCircle.setEffect(new javafx.scene.effect.DropShadow(15, javafx.scene.paint.Color.rgb(66, 153, 225, 0.4)));

        javafx.scene.control.Label avatarText = new javafx.scene.control.Label(
                currentUser.getNom().substring(0, 1).toUpperCase() + currentUser.getPrenom().substring(0, 1).toUpperCase()
        );
        avatarText.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
        avatarContainer.getChildren().addAll(avatarCircle, avatarText);

        // Nom complet
        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(currentUser.getNom() + " " + currentUser.getPrenom());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        // Badge rôle
        javafx.scene.control.Label roleLabel = new javafx.scene.control.Label(getRoleDisplayName(currentUser.getRole()));
        roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: #4299E1; " +
                "-fx-background-radius: 20px; -fx-padding: 8px 20px; -fx-font-weight: bold;");

        // Card Email
        javafx.scene.layout.HBox emailCard = createInfoCard("📧", "Email:", currentUser.getEmail());

        // Card Téléphone
        javafx.scene.layout.HBox phoneCard = createInfoCard("📞", "Téléphone:", currentUser.getTel());

        // Container pour CIN et Statut (côte à côte)
        javafx.scene.layout.HBox bottomRow = new javafx.scene.layout.HBox(15);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER);

        // Card CIN
        javafx.scene.layout.VBox cinCard = new javafx.scene.layout.VBox(8);
        cinCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cinCard.setStyle("-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-padding: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        cinCard.setPrefWidth(250);

        javafx.scene.layout.HBox cinHeader = new javafx.scene.layout.HBox(8);
        cinHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label cinIcon = new javafx.scene.control.Label("🆔");
        cinIcon.setStyle("-fx-font-size: 20px;");
        javafx.scene.control.Label cinTitle = new javafx.scene.control.Label("CIN:");
        cinTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-font-weight: bold;");
        cinHeader.getChildren().addAll(cinIcon, cinTitle);

        javafx.scene.control.Label cinValue = new javafx.scene.control.Label(String.valueOf(currentUser.getCin()));
        cinValue.setStyle("-fx-font-size: 18px; -fx-text-fill: #2D3748; -fx-font-weight: bold; -fx-padding: 5 0 0 28;");

        cinCard.getChildren().addAll(cinHeader, cinValue);

        // Card Statut
        javafx.scene.layout.VBox statutCard = new javafx.scene.layout.VBox(8);
        statutCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        String statutBg = currentUser.isActif() ? "#D1FAE5" : "#FEE2E2";
        String statutColor = currentUser.isActif() ? "#065F46" : "#991B1B";
        statutCard.setStyle("-fx-background-color: " + statutBg + "; -fx-background-radius: 12px; " +
                "-fx-padding: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        statutCard.setPrefWidth(250);

        javafx.scene.layout.HBox statutHeader = new javafx.scene.layout.HBox(8);
        statutHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label statutIcon = new javafx.scene.control.Label("●");
        statutIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: " + statutColor + ";");
        javafx.scene.control.Label statutTitle = new javafx.scene.control.Label("Statut:");
        statutTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + statutColor + "; -fx-font-weight: bold;");
        statutHeader.getChildren().addAll(statutIcon, statutTitle);

        javafx.scene.control.Label statutValue = new javafx.scene.control.Label(currentUser.isActif() ? "ACTIF" : "INACTIF");
        statutValue.setStyle("-fx-font-size: 18px; -fx-text-fill: " + statutColor + "; -fx-font-weight: bold; -fx-padding: 5 0 0 28;");

        statutCard.getChildren().addAll(statutHeader, statutValue);

        bottomRow.getChildren().addAll(cinCard, statutCard);

        // Boutons d'action
        javafx.scene.layout.HBox actionsBox = new javafx.scene.layout.HBox(15);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("✏️ Modifier mes informations");
        btnEdit.setStyle("-fx-background-color: #4299E1; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-padding: 12px 30px; -fx-cursor: hand; -fx-font-size: 14px;");
        btnEdit.setOnAction(e -> {
            dialog.close();
            showEditProfileStage(currentUser);
        });

        javafx.scene.control.Button btnChangePassword = new javafx.scene.control.Button("🔒 Changer mot de passe");
        btnChangePassword.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-padding: 12px 30px; -fx-cursor: hand; -fx-font-size: 14px;");
        btnChangePassword.setOnAction(e -> {
            dialog.close();
            showSimplePasswordDialog(currentUser);
        });

        actionsBox.getChildren().addAll(btnEdit, btnChangePassword);

        // Ajouter tous les éléments
        content.getChildren().addAll(avatarContainer, nameLabel, roleLabel, emailCard, phoneCard, bottomRow, actionsBox);

        dialog.getDialogPane().setContent(content);

        // Bouton Fermer
        javafx.scene.control.ButtonType closeButtonType = new javafx.scene.control.ButtonType("Fermer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        dialog.getDialogPane().setStyle("-fx-background-color: #F7FAFC; -fx-border-radius: 15px; -fx-background-radius: 15px;");
        dialog.getDialogPane().setPrefWidth(600);

        javafx.scene.Node closeButton = dialog.getDialogPane().lookupButton(closeButtonType);
        closeButton.setStyle("-fx-background-color: #4299E1; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-padding: 12px 40px; -fx-cursor: hand; -fx-font-size: 14px;");

        dialog.showAndWait();
    }

    private void addProfileRow(javafx.scene.layout.GridPane grid, int row, String label, String value) {
        javafx.scene.control.Label labelNode = new javafx.scene.control.Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #4A5568; -fx-font-size: 14px;");

        javafx.scene.control.Label valueNode = new javafx.scene.control.Label(value);
        valueNode.setStyle("-fx-text-fill: #2D3748; -fx-font-size: 14px; -fx-background-color: #F7FAFC; -fx-background-radius: 6px; -fx-padding: 8px 12px;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(280);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private String getRoleDisplayName(models.Role role) {
        switch (role) {
            case ADMIN: return "Administrateur";
            case CEO: return "Directeur Général";
            case EMPLOYE: return "Employé";
            case RESPONSABLE_RH: return "Responsable RH";
            case RESPONSABLE_PROJET: return "Responsable Projet";
            case RESPONSABLE_PRODUCTION: return "Responsable Production";
            default: return role.name();
        }
    }

    private javafx.scene.layout.HBox createInfoCard(String icon, String label, String value) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-padding: 18px 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(515);

        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(icon);
        iconLabel.setStyle("-fx-font-size: 22px;");

        javafx.scene.layout.VBox textBox = new javafx.scene.layout.VBox(4);

        javafx.scene.control.Label labelText = new javafx.scene.control.Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-font-weight: bold;");

        javafx.scene.control.Label valueText = new javafx.scene.control.Label(value);
        valueText.setStyle("-fx-font-size: 15px; -fx-text-fill: #2D3748;");

        textBox.getChildren().addAll(labelText, valueText);
        card.getChildren().addAll(iconLabel, textBox);

        return card;
    }

    private void showEditProfileStage(models.Utilisateur user) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Modifier mes informations");
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(15);
        vbox.setPadding(new javafx.geometry.Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Modifier mes informations");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        javafx.scene.control.Label label1 = new javafx.scene.control.Label("Nom:");
        javafx.scene.control.TextField nomField = new javafx.scene.control.TextField(user.getNom());
        nomField.setPrefWidth(350);

        javafx.scene.control.Label label2 = new javafx.scene.control.Label("Prénom:");
        javafx.scene.control.TextField prenomField = new javafx.scene.control.TextField(user.getPrenom());
        prenomField.setPrefWidth(350);

        javafx.scene.control.Label label3 = new javafx.scene.control.Label("Email:");
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField(user.getEmail());
        emailField.setPrefWidth(350);

        javafx.scene.control.Label label4 = new javafx.scene.control.Label("Téléphone:");
        javafx.scene.control.TextField telField = new javafx.scene.control.TextField(user.getTel());
        telField.setPrefWidth(350);

        // Validation
        nomField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]*")) nomField.setText(old);
        });
        prenomField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]*")) prenomField.setText(old);
        });
        telField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[0-9+\\s]*") || newVal.length() > 12) telField.setText(old);
        });

        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnSave = new javafx.scene.control.Button("Enregistrer");
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-size: 14px;");

        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Annuler");
        btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-size: 14px;");
        btnCancel.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(btnSave, btnCancel);

        vbox.getChildren().addAll(titleLabel, label1, nomField, label2, prenomField, label3, emailField, label4, telField, buttonBox);

        btnSave.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() || telField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires");
                return;
            }

            if (!emailField.getText().equals(user.getEmail())) {
                try {
                    services.UtilisateurService utilisateurService = services.UtilisateurService.getInstance();
                    if (utilisateurService.emailExists(emailField.getText())) {
                        showAlert("Erreur", "Cet email est déjà utilisé");
                        return;
                    }
                } catch (Exception ex) {
                    showAlert("Erreur", "Erreur lors de la vérification de l'email");
                    return;
                }
            }

            try {
                user.setNom(nomField.getText().trim());
                user.setPrenom(prenomField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setTel(telField.getText().trim());

                services.UtilisateurService.getInstance().modifier(user);

                stage.close();
                showAlert("Succès", "Vos informations ont été mises à jour!");
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible de mettre à jour: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        javafx.scene.Scene scene = new javafx.scene.Scene(vbox, 450, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Focus après affichage
        javafx.application.Platform.runLater(() -> nomField.requestFocus());
    }

    private void showSimplePasswordDialog(models.Utilisateur user) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Changer mot de passe");
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(15);
        vbox.setPadding(new javafx.geometry.Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Modifier votre mot de passe");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        javafx.scene.control.Label label1 = new javafx.scene.control.Label("Ancien mot de passe:");
        javafx.scene.control.PasswordField oldField = new javafx.scene.control.PasswordField();
        oldField.setPromptText("Entrez l'ancien mot de passe");
        oldField.setPrefWidth(350);

        javafx.scene.control.Label label2 = new javafx.scene.control.Label("Nouveau mot de passe:");
        javafx.scene.control.PasswordField newField = new javafx.scene.control.PasswordField();
        newField.setPromptText("Entrez le nouveau mot de passe");
        newField.setPrefWidth(350);

        javafx.scene.control.Label label3 = new javafx.scene.control.Label("Confirmer le mot de passe:");
        javafx.scene.control.PasswordField confirmField = new javafx.scene.control.PasswordField();
        confirmField.setPromptText("Confirmez le nouveau mot de passe");
        confirmField.setPrefWidth(350);

        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
                "Le mot de passe doit contenir:\n• 8 caractères minimum\n• 1 majuscule\n• 1 minuscule\n• 1 chiffre\n• 1 caractère spécial"
        );
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnChange = new javafx.scene.control.Button("Changer");
        btnChange.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-size: 14px;");

        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Annuler");
        btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-size: 14px;");
        btnCancel.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(btnChange, btnCancel);

        vbox.getChildren().addAll(titleLabel, label1, oldField, label2, newField, label3, confirmField, infoLabel, buttonBox);

        btnChange.setOnAction(e -> {
            String oldPassword = oldField.getText().trim();
            String newPassword = newField.getText().trim();
            String confirmPassword = confirmField.getText().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires");
                return;
            }

            String hashedOldPassword = utils.PasswordValidator.hashPassword(oldPassword);
            if (!user.getPassword().equals(hashedOldPassword)) {
                showAlert("Erreur", "L'ancien mot de passe est incorrect");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas");
                return;
            }

            utils.PasswordValidator.ValidationResult validation = utils.PasswordValidator.validatePassword(newPassword);
            if (!validation.isValid()) {
                showAlert("Erreur", validation.getMessage());
                return;
            }

            try {
                String hashedNewPassword = utils.PasswordValidator.hashPassword(newPassword);
                services.UtilisateurService.getInstance().updatePassword(user.getEmail(), hashedNewPassword);
                user.setPassword(hashedNewPassword);
                stage.close();
                showAlert("Succès", "Votre mot de passe a été changé avec succès!");
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible de changer le mot de passe: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        javafx.scene.Scene scene = new javafx.scene.Scene(vbox, 450, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Focus après affichage
        javafx.application.Platform.runLater(() -> oldField.requestFocus());
    }


    private void loadUtilisateurs() {
        try {
            currentData = utilisateurService.getAll();
            displayCards(currentData);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs");
            e.printStackTrace();
        }
    }

    private void loadEmployes() {
        try {
            currentData = utilisateurService.getByRole(Role.EMPLOYE);
            displayCards(currentData);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les employés");
            e.printStackTrace();
        }
    }

    private void loadAdmins() {
        try {
            currentData = utilisateurService.getByRole(Role.ADMIN);
            displayCards(currentData);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les administrateurs");
            e.printStackTrace();
        }
    }

    private void loadCEOs() {
        try {
            currentData = utilisateurService.getByRole(Role.CEO);
            displayCards(currentData);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les CEOs");
            e.printStackTrace();
        }
    }

    private void loadResponsables() {
        try {
            List<Utilisateur> allResponsables = new java.util.ArrayList<>();
            allResponsables.addAll(utilisateurService.getByRole(Role.RESPONSABLE_RH));
            allResponsables.addAll(utilisateurService.getByRole(Role.RESPONSABLE_PROJET));
            allResponsables.addAll(utilisateurService.getByRole(Role.RESPONSABLE_PRODUCTION));
            currentData = allResponsables;
            displayCards(currentData);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les responsables");
            e.printStackTrace();
        }
    }

    private void displayCards(List<Utilisateur> users) {
        cardsContainer.getChildren().clear();

        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucun utilisateur trouvé");
            emptyLabel.getStyleClass().add("empty-label");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        // Créer une grille avec 3 colonnes
        int columns = 3;
        int row = 0;
        HBox currentRow = null;

        for (int i = 0; i < users.size(); i++) {
            if (i % columns == 0) {
                currentRow = new HBox(20);
                currentRow.setAlignment(Pos.TOP_LEFT);
                cardsContainer.getChildren().add(currentRow);
            }

            VBox card = createUserCardGrid(users.get(i));
            HBox.setHgrow(card, Priority.ALWAYS);
            currentRow.getChildren().add(card);
        }
    }

    private VBox createUserCardGrid(Utilisateur user) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("user-card-grid");
        card.setPrefWidth(300);
        card.setMaxWidth(350);

        // Avatar/Icon section
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(80, 80);
        avatarPane.getStyleClass().add("avatar-large");

        Label avatarLabel = new Label(user.getNom().substring(0, 1).toUpperCase());
        avatarLabel.setFont(Font.font("System Bold", 32));
        avatarLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        avatarPane.getChildren().add(avatarLabel);

        // Name
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setFont(Font.font("System Bold", 16));
        nameLabel.getStyleClass().add("card-name-label");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setAlignment(Pos.CENTER);

        // Role badge
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setPadding(new Insets(6, 16, 6, 16));
        roleLabel.getStyleClass().addAll("role-badge", getRoleBadgeClass(user.getRole()));

        // Statut badge
        Label statutLabel = new Label(user.isActif() ? "✓ Actif" : "✗ Inactif");
        statutLabel.setPadding(new Insets(4, 12, 4, 12));
        statutLabel.setStyle(user.isActif()
                ? "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
                : "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
        );

        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER);
        badgesBox.getChildren().addAll(roleLabel, statutLabel);

        // Details
        VBox detailsBox = new VBox(6);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPrefWidth(Double.MAX_VALUE);

        Label emailLabel = new Label("📧 " + user.getEmail());
        emailLabel.getStyleClass().add("card-detail-label-small");
        emailLabel.setMaxWidth(Double.MAX_VALUE);

        Label telLabel = new Label("📞 " + user.getTel());
        telLabel.getStyleClass().add("card-detail-label-small");

        detailsBox.getChildren().addAll(emailLabel, telLabel);

        // Si employé, afficher poste et salaire
        if (user.isEmploye() && user.getPoste() != null) {
            Label posteLabel = new Label("💼 " + user.getPoste());
            posteLabel.getStyleClass().add("card-detail-label-small");
            detailsBox.getChildren().add(posteLabel);

            if (user.getSalaire() > 0) {
                Label salaireLabel = new Label("💰 " + user.getSalaire() + " €");
                salaireLabel.getStyleClass().add("card-detail-label-small");
                detailsBox.getChildren().add(salaireLabel);
            }
        }

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPrefWidth(Double.MAX_VALUE);

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().addAll("button-warning", "card-action-button-small");
        btnModifier.setOnAction(e -> handleModifier(user));
        HBox.setHgrow(btnModifier, Priority.ALWAYS);
        btnModifier.setMaxWidth(Double.MAX_VALUE);

        Button btnToggleStatut = new Button(user.isActif() ? "Désactiver" : "Activer");
        btnToggleStatut.getStyleClass().addAll(
                user.isActif() ? "button-danger" : "button-success",
                "card-action-button-small"
        );
        btnToggleStatut.setOnAction(e -> handleToggleStatut(user));
        HBox.setHgrow(btnToggleStatut, Priority.ALWAYS);
        btnToggleStatut.setMaxWidth(Double.MAX_VALUE);

        actionsBox.getChildren().addAll(btnModifier, btnToggleStatut);

        card.getChildren().addAll(avatarPane, nameLabel, badgesBox, detailsBox, actionsBox);

        return card;
    }

    private HBox createUserCard(Utilisateur user) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("user-card");
        card.setPrefHeight(100);

        // Info section
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setFont(Font.font("System Bold", 16));
        nameLabel.getStyleClass().add("card-name-label");

        Label emailLabel = new Label("📧 " + user.getEmail());
        emailLabel.getStyleClass().add("card-detail-label");

        Label telLabel = new Label("📞 " + user.getTel());
        telLabel.getStyleClass().add("card-detail-label");

        HBox detailsBox = new HBox(20);
        detailsBox.getChildren().addAll(emailLabel, telLabel);

        infoBox.getChildren().addAll(nameLabel, detailsBox);

        // Role badge
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setPadding(new Insets(6, 16, 6, 16));
        roleLabel.getStyleClass().addAll("role-badge", getRoleBadgeClass(user.getRole()));

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().addAll("button-warning", "card-action-button");
        btnModifier.setOnAction(e -> handleModifier(user));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().addAll("button-danger", "card-action-button");
        btnSupprimer.setOnAction(e -> handleSupprimer(user));

        actionsBox.getChildren().addAll(btnModifier, btnSupprimer);

        // Assemble card
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.TOP_RIGHT);
        rightBox.getChildren().add(roleLabel);

        card.getChildren().addAll(infoBox, rightBox, actionsBox);

        return card;
    }

    private String getRoleBadgeClass(Role role) {
        switch (role) {
            case ADMIN:
                return "role-badge-admin";
            case CEO:
                return "role-badge-ceo";
            case EMPLOYE:
                return "role-badge-employe";
            case RESPONSABLE_RH:
                return "role-badge-rh";
            case RESPONSABLE_PROJET:
                return "role-badge-projet";
            case RESPONSABLE_PRODUCTION:
                return "role-badge-production";
            default:
                return "role-badge-default";
        }
    }

    private void handleModifier(Utilisateur user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Utilisateur");
        dialog.setHeaderText("Modifier les informations de " + user.getNom() + " " + user.getPrenom());

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createUserFormGrid(user);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                String oldPassword = user.getPassword(); // Sauvegarder l'ancien mot de passe
                updateUserFromForm(user, grid);

                // Si le mot de passe a été modifié, le hasher
                if (!user.getPassword().equals(oldPassword)) {
                    String hashedPassword = utils.PasswordValidator.hashPassword(user.getPassword());
                    user.setPassword(hashedPassword);
                }

                utilisateurService.modifier(user);
                refreshCurrentView();
                showAlert("Succès", "Utilisateur modifié avec succès");
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de modifier l'utilisateur");
                e.printStackTrace();
            }
        }
    }

    private void handleSupprimer(Utilisateur user) {
        // Désactiver au lieu de supprimer
        handleToggleStatut(user);
    }

    private void handleToggleStatut(Utilisateur user) {
        try {
            if (user.isActif()) {
                // Confirmation avant désactivation
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmation");
                confirmation.setHeaderText("Désactiver l'utilisateur");
                confirmation.setContentText("Êtes-vous sûr de vouloir désactiver " +
                        user.getNom() + " " + user.getPrenom() + " ?\n\n" +
                        "L'utilisateur ne pourra plus se connecter.");

                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    utilisateurService.desactiver(user.getId());
                    refreshCurrentView();
                    showAlert("Succès", "Utilisateur désactivé avec succès");
                }
            } else {
                // Activer directement sans confirmation
                utilisateurService.activer(user.getId());
                refreshCurrentView();
                showAlert("Succès", "Utilisateur activé avec succès");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de modifier le statut de l'utilisateur");
            e.printStackTrace();
        }
    }

    private void refreshCurrentView() {
        switch (currentView) {
            case "utilisateurs":
                loadUtilisateurs();
                break;
            case "employes":
                loadEmployes();
                break;
            case "admins":
                loadAdmins();
                break;
            case "ceos":
                loadCEOs();
                break;
            case "responsables":
                loadResponsables();
                break;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Utilisateur");
        dialog.setHeaderText("Créer un nouvel utilisateur");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createUserFormGrid(null);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                Utilisateur newUser = createUserFromForm(grid);

                // DEBUG: Afficher les informations de l'utilisateur créé
                System.out.println("=== DEBUG AJOUT UTILISATEUR ===");
                System.out.println("Nom: " + newUser.getNom());
                System.out.println("Email: " + newUser.getEmail());
                System.out.println("Rôle récupéré: " + newUser.getRole());
                System.out.println("Mot de passe (avant hash): " + newUser.getPassword());

                // Vérifier l'unicité de l'email et du CIN
                if (utilisateurService.emailExists(newUser.getEmail())) {
                    showAlert("Erreur", "Cet email est déjà utilisé");
                    return;
                }

                if (utilisateurService.cinExists(newUser.getCin())) {
                    showAlert("Erreur", "Ce CIN est déjà enregistré");
                    return;
                }

                // Hasher le mot de passe avant d'ajouter
                String hashedPassword = utils.PasswordValidator.hashPassword(newUser.getPassword());
                newUser.setPassword(hashedPassword);

                System.out.println("Mot de passe (après hash): " + newUser.getPassword());
                System.out.println("Rôle final: " + newUser.getRole());
                System.out.println("=== FIN DEBUG ===");

                utilisateurService.ajouter(newUser);
                refreshCurrentView();
                showAlert("Succès", "Utilisateur ajouté avec succès");
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'ajouter l'utilisateur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private GridPane createUserFormGrid(Utilisateur user) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Champs de base
        TextField nomField = new TextField(user != null ? user.getNom() : "");
        TextField prenomField = new TextField(user != null ? user.getPrenom() : "");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        TextField telField = new TextField(user != null ? user.getTel() : "");
        TextField cinField = new TextField(user != null ? String.valueOf(user.getCin()) : "");
        PasswordField passwordField = new PasswordField();
        if (user != null) passwordField.setText(user.getPassword());

        // Contrôles de saisie pour nom (lettres uniquement)
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                nomField.setText(oldValue);
            }
        });

        // Contrôles de saisie pour prénom (lettres uniquement)
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                prenomField.setText(oldValue);
            }
        });

        // Validation email
        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !emailField.getText().isEmpty()) {
                if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    emailField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
                } else {
                    emailField.setStyle("");
                }
            }
        });

        // Contrôle téléphone (chiffres et + uniquement, max 12)
        telField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9+\\s]*")) {
                telField.setText(oldValue);
            }
            if (newValue.length() > 12) {
                telField.setText(oldValue);
            }
        });

        // Contrôle CIN (8 chiffres uniquement)
        cinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cinField.setText(oldValue);
            }
            if (newValue.length() > 8) {
                cinField.setText(oldValue);
            }
        });

        // Validation mot de passe (minimum 6 caractères)
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !passwordField.getText().isEmpty()) {
                if (passwordField.getText().length() < 6) {
                    passwordField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
                } else {
                    passwordField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2;");
                }
            }
        });

        // Role
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(Role.values());
        roleCombo.setValue(user != null ? user.getRole() : Role.EMPLOYE);

        // Champs employé (optionnels)
        Label departmentLabel = new Label("Département:");
        TextField departmentField = new TextField(user != null ? user.getDepartment() : "");

        Label posteLabel = new Label("Poste:");
        TextField posteField = new TextField(user != null ? user.getPoste() : "");

        Label salaireLabel = new Label("Salaire:");
        TextField salaireField = new TextField(user != null && user.getSalaire() > 0 ? String.valueOf(user.getSalaire()) : "");

        Label competencesLabel = new Label("Compétences:");
        TextField competencesField = new TextField(user != null ? user.getCompetences() : "");

        // Contrôle salaire (chiffres et point décimal uniquement)
        salaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                salaireField.setText(oldValue);
            }
        });

        // Ajouter les champs de base
        int row = 0;
        grid.add(new Label("Nom:"), 0, row);
        grid.add(nomField, 1, row++);
        grid.add(new Label("Prénom:"), 0, row);
        grid.add(prenomField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Téléphone:"), 0, row);
        grid.add(telField, 1, row++);
        grid.add(new Label("CIN:"), 0, row);
        grid.add(cinField, 1, row++);
        grid.add(new Label("Mot de passe:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("Rôle:"), 0, row);
        grid.add(roleCombo, 1, row++);

        // Lignes pour les champs employé (initialement cachés si ADMIN/CEO)
        int employeFieldsStartRow = row;
        grid.add(departmentLabel, 0, row);
        grid.add(departmentField, 1, row++);
        grid.add(posteLabel, 0, row);
        grid.add(posteField, 1, row++);
        grid.add(salaireLabel, 0, row);
        grid.add(salaireField, 1, row++);
        grid.add(competencesLabel, 0, row);
        grid.add(competencesField, 1, row++);

        // Fonction pour afficher/cacher les champs employé
        Runnable updateFieldsVisibility = () -> {
            Role selectedRole = roleCombo.getValue();
            boolean isEmployeOrResponsable = selectedRole == Role.EMPLOYE ||
                    selectedRole == Role.RESPONSABLE_RH ||
                    selectedRole == Role.RESPONSABLE_PROJET ||
                    selectedRole == Role.RESPONSABLE_PRODUCTION;

            departmentLabel.setVisible(isEmployeOrResponsable);
            departmentLabel.setManaged(isEmployeOrResponsable);
            departmentField.setVisible(isEmployeOrResponsable);
            departmentField.setManaged(isEmployeOrResponsable);

            posteLabel.setVisible(isEmployeOrResponsable);
            posteLabel.setManaged(isEmployeOrResponsable);
            posteField.setVisible(isEmployeOrResponsable);
            posteField.setManaged(isEmployeOrResponsable);

            salaireLabel.setVisible(isEmployeOrResponsable);
            salaireLabel.setManaged(isEmployeOrResponsable);
            salaireField.setVisible(isEmployeOrResponsable);
            salaireField.setManaged(isEmployeOrResponsable);

            competencesLabel.setVisible(isEmployeOrResponsable);
            competencesLabel.setManaged(isEmployeOrResponsable);
            competencesField.setVisible(isEmployeOrResponsable);
            competencesField.setManaged(isEmployeOrResponsable);
        };

        // Initialiser la visibilité
        updateFieldsVisibility.run();

        // Écouter les changements de rôle
        roleCombo.setOnAction(e -> updateFieldsVisibility.run());

        // Stocker les références pour récupération ultérieure
        nomField.setUserData("nom");
        prenomField.setUserData("prenom");
        emailField.setUserData("email");
        telField.setUserData("tel");
        cinField.setUserData("cin");
        passwordField.setUserData("password");
        roleCombo.setUserData("role");
        departmentField.setUserData("department");
        posteField.setUserData("poste");
        salaireField.setUserData("salaire");
        competencesField.setUserData("competences");

        return grid;
    }

    private Utilisateur createUserFromForm(GridPane grid) {
        String nom = getFieldValue(grid, "nom");
        String prenom = getFieldValue(grid, "prenom");
        String email = getFieldValue(grid, "email");
        String tel = getFieldValue(grid, "tel");
        int cin = Integer.parseInt(getFieldValue(grid, "cin"));
        String password = getFieldValue(grid, "password");
        Role role = getComboValue(grid, "role");

        // Si le rôle est ADMIN ou CEO, créer sans les champs employé
        if (role == Role.ADMIN || role == Role.CEO) {
            return new Utilisateur(nom, prenom, email, tel, cin, password, role);
        } else {
            // Pour EMPLOYE et RESPONSABLES, inclure les champs employé
            String department = getFieldValue(grid, "department");
            String poste = getFieldValue(grid, "poste");
            String salaireStr = getFieldValue(grid, "salaire");
            double salaire = salaireStr.isEmpty() ? 0.0 : Double.parseDouble(salaireStr);
            String competences = getFieldValue(grid, "competences");

            return new Utilisateur(nom, prenom, email, tel, cin, password, role, department, poste, salaire, competences);
        }
    }

    private void updateUserFromForm(Utilisateur user, GridPane grid) {
        user.setNom(getFieldValue(grid, "nom"));
        user.setPrenom(getFieldValue(grid, "prenom"));
        user.setEmail(getFieldValue(grid, "email"));
        user.setTel(getFieldValue(grid, "tel"));
        user.setCin(Integer.parseInt(getFieldValue(grid, "cin")));
        user.setPassword(getFieldValue(grid, "password"));
        user.setRole(getComboValue(grid, "role"));

        // Si le rôle est ADMIN ou CEO, mettre NULL pour les champs employé
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.CEO) {
            user.setDepartment(null);
            user.setPoste(null);
            user.setSalaire(0.0);
            user.setCompetences(null);
        } else {
            // Pour EMPLOYE et RESPONSABLES, récupérer les champs employé
            user.setDepartment(getFieldValue(grid, "department"));
            user.setPoste(getFieldValue(grid, "poste"));
            String salaireStr = getFieldValue(grid, "salaire");
            user.setSalaire(salaireStr.isEmpty() ? 0.0 : Double.parseDouble(salaireStr));
            user.setCompetences(getFieldValue(grid, "competences"));
        }
    }

    private String getFieldValue(GridPane grid, String userData) {
        for (javafx.scene.Node node : grid.getChildren()) {
            if (node.getUserData() != null && node.getUserData().equals(userData)) {
                if (node instanceof TextField) {
                    return ((TextField) node).getText();
                } else if (node instanceof PasswordField) {
                    return ((PasswordField) node).getText();
                }
            }
        }
        return "";
    }

    private Role getComboValue(GridPane grid, String userData) {
        System.out.println("=== DEBUG getComboValue ===");
        System.out.println("Recherche userData: " + userData);

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node.getUserData() != null) {
                System.out.println("Node trouvé avec userData: " + node.getUserData() + " (Type: " + node.getClass().getSimpleName() + ")");

                if (node.getUserData().equals(userData)) {
                    System.out.println("Match trouvé!");
                    if (node instanceof ComboBox) {
                        Role selectedRole = (Role) ((ComboBox<?>) node).getValue();
                        System.out.println("Rôle sélectionné dans ComboBox: " + selectedRole);
                        return selectedRole;
                    }
                }
            }
        }

        System.out.println("ATTENTION: Aucun ComboBox trouvé avec userData '" + userData + "', retour EMPLOYE par défaut");
        return Role.EMPLOYE;
    }

    private void loadModule(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            main.setCenter(root);
        } catch (IOException e) {
            System.err.println("Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleEvenements(ActionEvent event) {
        loadModule("/EventDashboard.fxml");
    }

    @FXML
    private void handleServices(ActionEvent event) {
        loadModule("/service-tab.fxml");
    }

    @FXML
    private void handleCategories(ActionEvent event) {
        loadModule("/categorie-tab.fxml");
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        loadModule("/produit.fxml");
    }

    @FXML
    private void handleRessources(ActionEvent event) {
        loadModule("/ressource.fxml");
    }

    @FXML
    private void handleProjets(ActionEvent event) {
        loadModule("/ListeProjets.fxml");
    }

    @FXML
    private void handlePlanning(ActionEvent event) {
        loadModule("/dashboard-view.fxml");
    }


    private void navigateToModule(String fxmlPath, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Passer l'utilisateur actuel au contrôleur si c'est MainController
            Object controller = loader.getController();
            if (controller instanceof MainController && currentUser != null) {
                ((MainController) controller).initData(currentUser);
            }

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            if (cssPath != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la vue " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== CHATBOT METHODS ==========

    @FXML
    private void toggleChatbot() {
        boolean isVisible = chatbotContainer.isVisible();
        chatbotContainer.setVisible(!isVisible);
        chatbotContainer.setManaged(!isVisible);
    }

    @FXML
    private void closeChatbot() {
        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);
    }

    @FXML
    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (message.isEmpty()) return;

        // Afficher le message de l'utilisateur
        addUserMessage(message);

        // Effacer le champ
        chatInputField.clear();

        // Obtenir et afficher la réponse du bot
        String response = chatbotService.processQuestion(message);
        addBotMessage(response);

        // Scroll vers le bas
        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void addUserMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle("-fx-background-color: #4299E1; -fx-text-fill: white; " +
                "-fx-padding: 10; -fx-background-radius: 10; " +
                "-fx-font-size: 13px;");

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        messageBox.setPadding(new javafx.geometry.Insets(5));

        chatMessagesContainer.getChildren().add(messageBox);
    }

    private void addBotMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle("-fx-background-color: white; -fx-text-fill: #2D3748; " +
                "-fx-padding: 10; -fx-background-radius: 10; " +
                "-fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                "-fx-font-size: 13px;");

        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        messageBox.setPadding(new javafx.geometry.Insets(5));

        chatMessagesContainer.getChildren().add(messageBox);
    }
}