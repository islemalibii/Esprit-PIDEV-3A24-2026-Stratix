package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SimpleDBTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/stratix";
        String user = "root";
        String password = "";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== TEST DE CONNEXION ET INSERTION ===");
            
            // 1. Connexion
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Connexion établie");
            System.out.println("AutoCommit: " + conn.getAutoCommit());
            
            // 2. Générer un email unique
            String testEmail = "simpletest" + System.currentTimeMillis() + "@test.com";
            
            // 3. Insertion
            String insertQuery = "INSERT INTO utilisateur (nom, prenom, email, tel, cin, password, role, statut, date_ajout) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            
            ps = conn.prepareStatement(insertQuery);
            ps.setString(1, "SimpleTest");
            ps.setString(2, "User");
            ps.setString(3, testEmail);
            ps.setString(4, "0612345678");
            ps.setInt(5, 11111111);
            ps.setString(6, "hashedpassword123");
            ps.setString(7, "employe");
            ps.setString(8, "actif");
            
            System.out.println("Exécution INSERT...");
            int rows = ps.executeUpdate();
            System.out.println("✓ Lignes insérées: " + rows);
            ps.close();
            
            // 4. Vérification immédiate
            String selectQuery = "SELECT * FROM utilisateur WHERE email = ?";
            ps = conn.prepareStatement(selectQuery);
            ps.setString(1, testEmail);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                System.out.println("✓ SUCCÈS - Utilisateur trouvé dans la base!");
                System.out.println("  ID: " + rs.getInt("id"));
                System.out.println("  Nom: " + rs.getString("nom"));
                System.out.println("  Email: " + rs.getString("email"));
            } else {
                System.out.println("✗ ÉCHEC - Utilisateur NON trouvé dans la base!");
            }
            
            // 5. Compter tous les utilisateurs
            rs.close();
            ps.close();
            ps = conn.prepareStatement("SELECT COUNT(*) as total FROM utilisateur");
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Total utilisateurs dans la base: " + rs.getInt("total"));
            }
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
