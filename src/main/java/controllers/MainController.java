package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import models.Role;
import models.UserRole;
import models.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnCategories;
    @FXML private Button btnResources;
    @FXML private Button btnProjet;
    @FXML private Button btnTaches;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblUserAvatar;
    @FXML private Label welcomeLabel;
    @FXML private Label avatarLabel;
    @FXML private javafx.scene.layout.HBox userProfileSection;
    public static StackPane staticContentArea;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        staticContentArea = contentArea;
        System.out.println("✅ MainController initialisé");
    }

    public void initData(Utilisateur user) {
        updateUserInfo(user);
        applyRoleBasedVisibility(user);

        if (user.getRole() == Role.EMPLOYE) {
            loadView("/EventEmployeeDashboard.fxml");
        } else {
            loadView("/EventDashboard.fxml");
        }
    }


    private void updateUserInfo(Utilisateur user) {
        if (lblUserName == null) return;

        lblUserName.setText(user.getNom() + " " + user.getPrenom());

        // Mettre à jour le message de bienvenue
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + user.getPrenom());
        }

        // Mettre à jour l'avatar avec la première lettre du prénom (header en haut)
        if (avatarLabel != null && user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            avatarLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
        }

        // Mettre à jour l'avatar dans la section utilisateur (bas)
        if (lblUserAvatar != null && user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            lblUserAvatar.setText(user.getPrenom().substring(0, 1).toUpperCase());
        }

        if (user.getRole() == Role.EMPLOYE) {
            lblUserRole.setText("Session Employé");
            if (lblUserAvatar != null) lblUserAvatar.setText("👤");
        } else {
            lblUserRole.setText(user.getRole().name());
            if (lblUserAvatar != null) lblUserAvatar.setText("👑");
        }
    }

    private void applyRoleBasedVisibility(Utilisateur user) {
        if (user.getRole() == Role.EMPLOYE) {
            Button[] toHide = {btnCategories};
            for (Button btn : toHide) {
                if (btn != null) {
                    btn.setVisible(false);
                    btn.setManaged(false);
                }
            }
        }
    }


    @FXML
    private void showServices(ActionEvent event) {
        loadView("/service-tab.fxml");
    }

    @FXML
    private void showCategories(ActionEvent event) {
        loadView("/categorie-tab.fxml");
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        Utilisateur user = UserRole.getInstance().getUser();
        String fxml = (user != null && user.getRole() == Role.EMPLOYE) ?
                "/EventEmployeeDashboard.fxml" : "/EventDashboard.fxml";
        loadView(fxml);
    }

    @FXML
    private void showProjet(ActionEvent event) {
        Utilisateur user = UserRole.getInstance().getUser();
        String fxml = (user != null && user.getRole() == Role.EMPLOYE) ?
                "/EmployeListeProjets.fxml" : "/ListeProjets.fxml";
        loadView(fxml);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            System.out.println("✅ Vue chargée: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showResources() {
        Utilisateur user = UserRole.getInstance().getUser();
        String fxml = (user != null && user.getRole() == Role.EMPLOYE) ?
                "/FrontRessources.fxml" : "/ressource.fxml";
        loadView(fxml);
    }

    @FXML
    private void showProduits() {
        Utilisateur user = UserRole.getInstance().getUser();
        String fxml = (user != null && user.getRole() == Role.EMPLOYE) ?
                "/FrontProduits.fxml" : "/produit.fxml";
        loadView(fxml);
    }

    @FXML
    private void showPlanning() {
        Utilisateur user = UserRole.getInstance().getUser();
        String fxml = (user != null && user.getRole() == Role.EMPLOYE) ?
                "/EmpTacheView.fxml" : "/dashboard-view.fxml";
        loadView(fxml);
    }

    // ⭐ MÉTHODES STATIQUES POUR DASHBOARD ⭐
    public static void showTachesFromDashboard() {
        if (staticContentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/TacheView.fxml"));
                Parent view = loader.load();
                staticContentArea.getChildren().setAll(view);
                System.out.println("✅ Navigation vers Tâches depuis Dashboard");
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement Tâches: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void showPlanningFromDashboard() {
        if (staticContentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/PlanningView.fxml"));
                Parent view = loader.load();
                staticContentArea.getChildren().setAll(view);
                System.out.println("✅ Navigation vers Planning depuis Dashboard");
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement Planning: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void showCalendarFromDashboard() {
        if (staticContentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/calendar-view.fxml"));
                Parent view = loader.load();
                staticContentArea.getChildren().setAll(view);
                System.out.println("✅ Navigation vers Calendrier depuis Dashboard");
            } catch (IOException e) {
                System.err.println("❌ Erreur chargement Calendrier: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLogout() {
        utils.SessionManager.getInstance().logout();

        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root, 1000, 700);

            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("stratiX - Accueil");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleProfileClick() {
        Utilisateur user = UserRole.getInstance().getUser();
        if (user == null) return;

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
                user.getNom().substring(0, 1).toUpperCase() + user.getPrenom().substring(0, 1).toUpperCase()
        );
        avatarText.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
        avatarContainer.getChildren().addAll(avatarCircle, avatarText);

        // Nom complet
        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        // Badge rôle
        javafx.scene.control.Label roleLabel = new javafx.scene.control.Label(getRoleDisplayName(user.getRole()));
        roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: #4299E1; " +
                "-fx-background-radius: 20px; -fx-padding: 8px 20px; -fx-font-weight: bold;");

        // Card Email
        javafx.scene.layout.HBox emailCard = createInfoCard("📧", "Email:", user.getEmail());

        // Card Téléphone
        javafx.scene.layout.HBox phoneCard = createInfoCard("📞", "Téléphone:", user.getTel());

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

        javafx.scene.control.Label cinValue = new javafx.scene.control.Label(String.valueOf(user.getCin()));
        cinValue.setStyle("-fx-font-size: 18px; -fx-text-fill: #2D3748; -fx-font-weight: bold; -fx-padding: 5 0 0 28;");

        cinCard.getChildren().addAll(cinHeader, cinValue);

        // Card Statut
        javafx.scene.layout.VBox statutCard = new javafx.scene.layout.VBox(8);
        statutCard.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        String statutBg = user.isActif() ? "#D1FAE5" : "#FEE2E2";
        String statutColor = user.isActif() ? "#065F46" : "#991B1B";
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

        javafx.scene.control.Label statutValue = new javafx.scene.control.Label(user.isActif() ? "ACTIF" : "INACTIF");
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
            showEditProfileStage(user);
        });

        javafx.scene.control.Button btnChangePassword = new javafx.scene.control.Button("🔒 Changer mot de passe");
        btnChangePassword.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-padding: 12px 30px; -fx-cursor: hand; -fx-font-size: 14px;");
        btnChangePassword.setOnAction(e -> {
            dialog.close();
            showSimplePasswordDialog(user);
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

    private String getRoleDisplayName(Role role) {
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

    private void showEditProfileStage(Utilisateur user) {
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
                updateUserInfo(user);

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

    private void showSimplePasswordDialog(Utilisateur user) {
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

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}