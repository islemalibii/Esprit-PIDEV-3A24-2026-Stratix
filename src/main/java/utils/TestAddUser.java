package utils;

import models.Role;
import models.Utilisateur;
import services.UtilisateurService;

import java.sql.SQLException;

public class TestAddUser {
    public static void main(String[] args) {
        try {
            UtilisateurService service = UtilisateurService.getInstance();
            
            // Créer un utilisateur de test
            Utilisateur testUser = new Utilisateur(
                "Test",
                "User",
                "test" + System.currentTimeMillis() + "@example.com",
                "0612345678",
                12345678,
                PasswordValidator.hashPassword("Test123!"),
                Role.EMPLOYE,
                "IT",
                "Testeur",
                2500.0,
                "Java, Testing"
            );
            
            testUser.setStatut("actif");
            
            System.out.println("Ajout de l'utilisateur...");
            service.ajouter(testUser);
            System.out.println("Utilisateur ajouté avec succès!");
            
            // Vérifier dans la base
            System.out.println("\nVérification dans la base...");
            var users = service.getAll();
            System.out.println("Nombre total d'utilisateurs: " + users.size());
            
            boolean found = users.stream()
                .anyMatch(u -> u.getEmail().equals(testUser.getEmail()));
            
            if (found) {
                System.out.println("✓ L'utilisateur a été trouvé dans la base!");
            } else {
                System.out.println("✗ L'utilisateur n'a PAS été trouvé dans la base!");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
