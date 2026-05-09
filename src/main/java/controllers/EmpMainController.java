package controllers;

import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class EmpMainController {

    @FXML private StackPane mainPane;
    // @FXML private Label lblEmployeNom;  ← SUPPRIMÉ

    private Parent mesTachesView;
    private Parent monPlanningView;

    @FXML
    public void initialize() {
        // PLUS DE RÉFÉRENCE À lblEmployeNom
        // Employe user = SessionManager.getCurrentUser();
        // if (user != null) {
        //     lblEmployeNom.setText(user.getUsername() + " (" + user.getRole() + ")");
        // }

        try {
            System.out.println("Chargement des vues employé...");

            mesTachesView = FXMLLoader.load(getClass().getResource("/EmpTacheView.fxml"));
            monPlanningView = FXMLLoader.load(getClass().getResource("/EmpPlanningView.fxml"));

            showMesTaches();
            System.out.println("✅ Vues employé chargées");
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement vues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showMesTaches() {
        if (mesTachesView != null) {
            mainPane.getChildren().clear();
            mainPane.getChildren().add(mesTachesView);
            System.out.println("🔄 Affichage des tâches");
        }
    }

    @FXML
    private void showMonPlanning() {
        if (monPlanningView != null) {
            mainPane.getChildren().clear();
            mainPane.getChildren().add(monPlanningView);
            System.out.println("🔄 Affichage du planning");
        }
    }

    @FXML
    private void logout() {
        try {
            // ✅ CORRIGÉ: utiliser getInstance() pour appeler logout()
            SessionManager.getInstance().logout();

            Parent loginView = FXMLLoader.load(getClass().getResource("/PagePrincipaleView.fxml"));
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.setMaximized(true);
            stage.show();

            System.out.println("🔓 Déconnexion réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}