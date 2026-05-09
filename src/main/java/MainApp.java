import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controllers.DashboardAdminController;
import models.Role;
import models.Utilisateur;
import services.UtilisateurService;
import utils.SessionManager;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean shouldOpenDashboard = false;
        Utilisateur loggedUser = null;
        
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            
            // Vérifier si l'utilisateur est déjà connecté
            if (sessionManager.isLoggedIn()) {
                int userId = sessionManager.getUserId();
                
                if (userId > 0) {
                    try {
                        // Récupérer l'utilisateur depuis la base de données
                        UtilisateurService utilisateurService = UtilisateurService.getInstance();
                        Utilisateur user = utilisateurService.getById(userId);
                        
                        // Vérifier si l'utilisateur existe et est actif
                        if (user != null && user.isActif()) {
                            shouldOpenDashboard = true;
                            loggedUser = user;
                        } else {
                            // Session invalide ou compte inactif, supprimer
                            sessionManager.logout();
                        }
                    } catch (Exception e) {
                        // Erreur de connexion à la base, supprimer la session
                        System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
                        sessionManager.logout();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de la session: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Ouvrir le dashboard ou la page de login
        if (shouldOpenDashboard && loggedUser != null) {
            openDashboardByRole(primaryStage, loggedUser);
        } else {
            openLogin(primaryStage);
        }
    }
    
    private void openLogin(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        primaryStage.setTitle("Stratix - Gestion Utilisateurs");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    /**
     * Ouvrir le dashboard selon le rôle de l'utilisateur
     */
    private void openDashboardByRole(Stage primaryStage, Utilisateur user) throws Exception {
        switch (user.getRole()) {
            case ADMIN:
                openAdminDashboard(primaryStage, user);
                break;
            case CEO:
            case EMPLOYE:
            case RESPONSABLE_RH:
            case RESPONSABLE_PROJET:
            case RESPONSABLE_PRODUCTION:
                openStandardUserDashboard(primaryStage, user);
                break;
            default:
                openLogin(primaryStage);
                break;
        }
    }
    
    /**
     * Ouvrir le dashboard Admin
     */
    private void openAdminDashboard(Stage primaryStage, Utilisateur user) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_admin.fxml"));
        Parent root = loader.load();
        
        DashboardAdminController controller = loader.getController();
        controller.setCurrentUser(user);
        
        primaryStage.setTitle("Stratix - Dashboard Admin");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    /**
     * Ouvrir le dashboard pour les autres utilisateurs (CEO, Employés, Responsables)
     */
    private void openStandardUserDashboard(Stage primaryStage, Utilisateur user) throws Exception {
        // IMPORTANT: Définir l'utilisateur dans UserRole pour la session
        models.UserRole.getInstance().setUser(user);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/service-view.fxml"));
        Parent root = loader.load();
        
        // Récupérer le contrôleur et initialiser avec l'utilisateur
        Object controller = loader.getController();
        if (controller instanceof controllers.MainController) {
            controllers.MainController mainController = (controllers.MainController) controller;
            mainController.initData(user);
        }
        
        String roleTitle = switch (user.getRole()) {
            case CEO -> "CEO";
            case EMPLOYE -> "Employé";
            case RESPONSABLE_RH -> "Responsable RH";
            case RESPONSABLE_PROJET -> "Responsable Projet";
            case RESPONSABLE_PRODUCTION -> "Responsable Production";
            default -> "Utilisateur";
        };
        
        Scene scene = new Scene(root, 1300, 750);
        
        // Charger le CSS si disponible
        String cssPath = "/css/style.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        }
        
        primaryStage.setTitle("stratiX - " + user.getNom() + " " + user.getPrenom() + " [" + roleTitle + "]");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

