package controllers;

import models.Role;
import models.UserRole;
import services.AuthenticationService;
import services.CaptchaService;
import services.EmailService;
import services.UtilisateurService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Utilisateur;
import utils.PasswordValidator;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LoginController {

    // ← Changer false en true pour réactiver le 2FA
    private static final boolean ENABLE_2FA = false;

    @FXML
    private Button googleLoginButton;

    @FXML
    private StackPane captchaContainer;

    @FXML
    private TextField captchaField;

    @FXML
    private Button refreshCaptchaButton;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label countdownLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    private UtilisateurService utilisateurService;
    private AuthenticationService authService;
    private EmailService emailService;
    private Timeline countdownTimeline;

    public void initialize() {
        utilisateurService = UtilisateurService.getInstance();
        authService = AuthenticationService.getInstance();
        emailService = EmailService.getInstance();

        // Générer le CAPTCHA initial
        refreshCaptcha();

        // Contrôle de saisie pour l'email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        // Validation email au focus perdu
        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !emailField.getText().isEmpty()) {
                if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    emailField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
                } else {
                    emailField.setStyle("");
                }
            }
        });

        // Masquer l'erreur quand on tape le mot de passe
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
    }

    @FXML
    void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String captchaInput = captchaField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier le CAPTCHA
        CaptchaService captchaService = CaptchaService.getInstance();
        if (!captchaService.verifyCaptcha(captchaInput)) {
            showError("Code CAPTCHA incorrect. Veuillez réessayer.");
            refreshCaptcha();
            return;
        }

        try {
            System.out.println("=== DEBUG LOGIN ===");
            System.out.println("Email saisi: " + email);
            System.out.println("Mot de passe saisi: " + password);

            // Vérifier si le compte est verrouillé
            AuthenticationService.LockStatus lockStatus = authService.isAccountLocked(email);
            if (lockStatus.isLocked()) {
                showLockoutCountdown(lockStatus);
                return;
            }

            // Hasher le mot de passe pour comparaison
            String hashedPassword = PasswordValidator.hashPassword(password);
            System.out.println("Mot de passe hashé (SHA-256): " + hashedPassword);

            // Vérifier les credentials
            List<Utilisateur> utilisateurs = utilisateurService.getAll();
            System.out.println("Nombre d'utilisateurs dans la base: " + utilisateurs.size());

            // Chercher l'utilisateur par email
            Utilisateur userByEmail = utilisateurs.stream()
                    .filter(u -> u.getEmail().equals(email))
                    .findFirst()
                    .orElse(null);

            if (userByEmail != null) {
                System.out.println("Utilisateur trouvé: " + userByEmail.getNom() + " " + userByEmail.getPrenom());
                System.out.println("Mot de passe dans la base: " + userByEmail.getPassword());
                System.out.println("Longueur mot de passe base: " + userByEmail.getPassword().length());
                System.out.println("Mots de passe identiques? " + userByEmail.getPassword().equals(hashedPassword));
            } else {
                System.out.println("AUCUN utilisateur trouvé avec cet email!");
            }

            Utilisateur user = utilisateurs.stream()
                    .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(hashedPassword))
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                // Vérifier si le compte est actif
                if (user.isInactif()) {
                    showError("Votre compte a été désactivé. Contactez l'administrateur.");
                    return;
                }

                // Réinitialiser les tentatives échouées
                authService.resetFailedAttempts(email);

                // 2FA - changer ENABLE_2FA en true/false pour activer/désactiver
                if (ENABLE_2FA) {
                    show2FADialog(user);
                } else {
                    SessionManager.getInstance().saveSession(user.getId(), user.getEmail(), user.getRole().name());
                    redirectToRoleDashboard(user);
                }
            } else {
                // Enregistrer tentative échouée
                authService.recordFailedLogin(email);
                System.out.println("Échec de connexion - mot de passe incorrect");
                showError("Email ou mot de passe incorrect");
            }
            System.out.println("=== FIN DEBUG LOGIN ===");
        } catch (Exception e) {
            showError("Erreur de connexion");
            e.printStackTrace();
        }
    }

    /**
     * Afficher le dialog 2FA
     */
    private void show2FADialog(Utilisateur user) {
        try {
            // Générer un nouveau code 2FA
            String code = authService.generate2FACode(user.getId());

            // Envoyer le code par email
            boolean emailSent = emailService.send2FACode(user.getEmail(), code);

            if (emailSent) {
                // Email envoyé avec succès
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Code 2FA envoyé");
                infoAlert.setHeaderText("Code de vérification envoyé");
                infoAlert.setContentText("Un code de vérification a été envoyé à votre adresse email.\n\nVérifiez votre boîte de réception.");
                infoAlert.showAndWait();
            } else {
                // Si l'envoi échoue, afficher le code à l'écran (fallback)
                Alert codeAlert = new Alert(Alert.AlertType.INFORMATION);
                codeAlert.setTitle("Code 2FA");
                codeAlert.setHeaderText("Impossible d'envoyer l'email");
                codeAlert.setContentText("Votre code de vérification: " + code + "\n\n(Configurez votre serveur SMTP dans EmailService.java pour activer l'envoi d'emails)");
                codeAlert.showAndWait();
            }

            // Dialog pour entrer le code
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Authentification à deux facteurs");
            dialog.setHeaderText("Entrez le code de vérification");
            dialog.setContentText("Code:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(enteredCode -> {
                try {
                    if (authService.verify2FACode(user.getId(), enteredCode)) {
                        // Code correct - Connexion réussie
                        SessionManager.getInstance().saveSession(user.getId(), user.getEmail(), user.getRole().name());
                        redirectToRoleDashboard(user);
                    } else {
                        showError("Code de vérification incorrect");
                    }
                } catch (SQLException e) {
                    showError("Erreur lors de la vérification du code");
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            showError("Erreur lors de la génération du code 2FA");
            e.printStackTrace();
        }
    }

    /**
     * Rediriger vers le dashboard selon le rôle
     */
    private void redirectToRoleDashboard(Utilisateur user) {
        UserRole.getInstance().setUser(user);
        if (user.getRole() == Role.ADMIN) {
            openDashboard(user);
        } else {
            openStandardUserDashboard(user);
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Entrez votre email:");

        // Appliquer le CSS
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            try {
                // Vérifier si l'email existe
                Utilisateur user = utilisateurService.getByEmail(email);
                if (user != null) {
                    // Générer un code de vérification (6 chiffres)
                    String code = PasswordValidator.generate2FACode();

                    // Envoyer l'email
                    boolean emailSent = emailService.sendPasswordResetEmail(email, code);

                    if (emailSent) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Email envoyé");
                        successAlert.setHeaderText("Code de vérification envoyé");
                        successAlert.setContentText("Un code de vérification a été envoyé à votre adresse email.\n\nVérifiez votre boîte de réception.");
                        successAlert.showAndWait();

                        // Ouvrir le dialog de réinitialisation avec le code et l'email
                        showResetPasswordDialog(email, code);
                    } else {
                        // Si l'envoi échoue, afficher le code à l'écran (fallback)
                        Alert codeAlert = new Alert(Alert.AlertType.INFORMATION);
                        codeAlert.setTitle("Code de vérification");
                        codeAlert.setHeaderText("Impossible d'envoyer l'email");
                        codeAlert.setContentText("Votre code de vérification: " + code + "\n\n(Configurez votre serveur SMTP dans EmailService.java pour activer l'envoi d'emails)");
                        codeAlert.showAndWait();

                        showResetPasswordDialog(email, code);
                    }
                } else {
                    showError("Email non trouvé");
                }
            } catch (SQLException e) {
                showError("Erreur lors de la génération du code");
                e.printStackTrace();
            }
        });
    }

    /**
     * Dialog pour réinitialiser le mot de passe
     */
    private void showResetPasswordDialog(String email, String expectedCode) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Entrez le code reçu par email et votre nouveau mot de passe");

        // Appliquer le CSS
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        ButtonType resetButtonType = new ButtonType("Réinitialiser", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeField = new TextField();
        codeField.setPromptText("Code de vérification");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer mot de passe");

        // Label pour afficher les exigences du mot de passe
        Label requirementsLabel = new Label(
                "Le mot de passe doit contenir:\n" +
                        "• Au moins 8 caractères\n" +
                        "• Une lettre majuscule\n" +
                        "• Une lettre minuscule\n" +
                        "• Un chiffre\n" +
                        "• Un caractère spécial (@#$%^&+=!)"
        );
        requirementsLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        // Label pour l'indicateur de force
        Label strengthLabel = new Label();
        strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Validation en temps réel
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            PasswordValidator.ValidationResult validation = PasswordValidator.validatePassword(newValue);
            if (newValue.isEmpty()) {
                strengthLabel.setText("");
                newPasswordField.setStyle("");
            } else if (validation.isValid()) {
                strengthLabel.setText("✓ Mot de passe fort");
                strengthLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-font-weight: bold;");
                newPasswordField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2;");
            } else {
                strengthLabel.setText("✗ Mot de passe faible");
                strengthLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-font-weight: bold;");
                newPasswordField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
            }
        });

        // Validation de confirmation
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                confirmPasswordField.setStyle("");
            } else if (newValue.equals(newPasswordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2;");
            } else {
                confirmPasswordField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
            }
        });

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Nouveau mot de passe:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(strengthLabel, 1, 2);
        grid.add(requirementsLabel, 0, 3, 2, 1);
        grid.add(new Label("Confirmer:"), 0, 4);
        grid.add(confirmPasswordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == resetButtonType) {
            String enteredCode = codeField.getText().trim();
            String newPassword = newPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            // Validation
            if (enteredCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError("Veuillez remplir tous les champs");
                return;
            }

            // Vérifier le code
            if (!enteredCode.equals(expectedCode)) {
                showError("Code de vérification incorrect");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("Les mots de passe ne correspondent pas");
                return;
            }

            // Valider la force du mot de passe
            PasswordValidator.ValidationResult validation = PasswordValidator.validatePassword(newPassword);
            if (!validation.isValid()) {
                showError(validation.getMessage());
                return;
            }

            try {
                // Hasher le nouveau mot de passe
                String hashedPassword = PasswordValidator.hashPassword(newPassword);

                // Mettre à jour le mot de passe dans la base de données
                utilisateurService.updatePassword(email, hashedPassword);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("Mot de passe réinitialisé avec succès!\n\nVous pouvez maintenant vous connecter avec votre nouveau mot de passe.");
                success.showAndWait();
            } catch (Exception e) {
                showError("Erreur lors de la réinitialisation");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleSignup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/signup.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDashboard(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_admin.fxml"));
            Parent root = loader.load();

            DashboardAdminController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openStandardUserDashboard(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/service-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();

            mainController.initData(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1300, 750);

            String path = "/css/style.css";
            if (getClass().getResource(path) != null) {
                scene.getStylesheets().add(getClass().getResource(path).toExternalForm());
            } else {
                System.err.println("ERREUR : Fichier CSS introuvable à : " + path);
            }

            stage.setScene(scene);
            stage.setTitle("stratiX - " + user.getNom() + " " + user.getPrenom() + " [" + user.getRole() + "]");
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du dashboard standard : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEventPage(String fxmlPath, Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Afficher un compteur à rebours pour le verrouillage
     */
    private void showLockoutCountdown(AuthenticationService.LockStatus lockStatus) {
        // Désactiver les champs de saisie
        emailField.setDisable(true);
        passwordField.setDisable(true);
        loginButton.setDisable(true);

        // Masquer l'erreur
        errorLabel.setVisible(false);

        // Afficher le compteur
        countdownLabel.setVisible(true);

        // Arrêter le timeline précédent s'il existe
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        // Calculer l'affichage initial
        long initialSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                lockStatus.getUnlockTime()
        ).getSeconds();

        long minutes = initialSeconds / 60;
        long seconds = initialSeconds % 60;
        countdownLabel.setText(String.format("⏱️ Compte verrouillé. Réessayez dans %d:%02d", minutes, seconds));

        // Timeline pour mettre à jour le compteur chaque seconde
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    long currentSeconds = java.time.Duration.between(
                            LocalDateTime.now(),
                            lockStatus.getUnlockTime()
                    ).getSeconds();

                    if (currentSeconds <= 0) {
                        // Déverrouillage
                        countdownTimeline.stop();
                        countdownLabel.setVisible(false);
                        emailField.setDisable(false);
                        passwordField.setDisable(false);
                        loginButton.setDisable(false);
                        showError("Vous pouvez maintenant réessayer de vous connecter");
                    } else {
                        long mins = currentSeconds / 60;
                        long secs = currentSeconds % 60;
                        countdownLabel.setText(String.format("⏱️ Compte verrouillé. Réessayez dans %d:%02d", mins, secs));
                    }
                })
        );

        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    @FXML
    void handleRefreshCaptcha() {
        refreshCaptcha();
    }

    private void refreshCaptcha() {
        try {
            CaptchaService captchaService = CaptchaService.getInstance();
            javafx.scene.canvas.Canvas captchaCanvas = captchaService.generateCaptchaCanvas();

            // Effacer le contenu précédent
            captchaContainer.getChildren().clear();

            // Ajouter le nouveau canvas
            captchaContainer.getChildren().add(captchaCanvas);

            // Effacer le champ de saisie
            if (captchaField != null) {
                captchaField.clear();
            }

            System.out.println("CAPTCHA généré: " + captchaService.getCurrentCaptchaText());
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du CAPTCHA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleGoogleLogin() {
        showError("La connexion Google sera disponible prochainement.\nVeuillez configurer les identifiants OAuth dans GoogleAuthService.java");


        try {
            System.out.println("=== CONNEXION GOOGLE ===");
            googleLoginButton.setDisable(true);
            googleLoginButton.setText("Connexion en cours...");

            new Thread(() -> {
                try {
                    services.GoogleAuthService googleAuth = services.GoogleAuthService.getInstance();
                    services.GoogleAuthService.GoogleUserInfo googleUser = googleAuth.authenticateWithGoogle();

                    javafx.application.Platform.runLater(() -> {
                        try {
                            Utilisateur user = utilisateurService.getByEmail(googleUser.getEmail());

                            if (user == null) {
                                user = new Utilisateur(
                                        googleUser.getLastName() != null ? googleUser.getLastName() : "Google",
                                        googleUser.getFirstName() != null ? googleUser.getFirstName() : "User",
                                        googleUser.getEmail(),
                                        "",
                                        0,
                                        utils.PasswordValidator.hashPassword(java.util.UUID.randomUUID().toString()),
                                        models.Role.EMPLOYE
                                );
                                user.setStatut("actif");
                                utilisateurService.ajouter(user);
                                user = utilisateurService.getByEmail(googleUser.getEmail());
                            }

                            if (user != null) {
                                if (user.isInactif()) {
                                    showError("Votre compte a été désactivé. Contactez l'administrateur.");
                                    return;
                                }
                                show2FADialog(user);
                            } else {
                                showError("Erreur lors de la création du compte");
                            }

                        } catch (Exception e) {
                            showError("Erreur lors de la connexion Google");
                            e.printStackTrace();
                        } finally {
                            googleLoginButton.setDisable(false);
                            googleLoginButton.setText("Se connecter avec Google");
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Erreur d'authentification Google");
                        googleLoginButton.setDisable(false);
                        googleLoginButton.setText("Se connecter avec Google");
                    });
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur lors de la connexion Google");
            googleLoginButton.setDisable(false);
            googleLoginButton.setText("Se connecter avec Google");
            e.printStackTrace();
        }

    }
}