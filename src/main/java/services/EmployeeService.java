package services;

import models.Employe;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;

    /**
     * Récupère tous les employés et responsables
     */
    public List<Employe> getAllEmployes() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = 'employe' OR role = 'EMPLOYE' OR role LIKE 'responsable%'";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractEmployeFromResultSet(rs));
            }
            System.out.println("✅ " + list.size() + " employés chargés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllEmployes: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère un employé par son ID
     */
    public Employe getEmployeById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractEmployeFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getEmployeById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Vérifie si un employé existe
     */
    public boolean employeExiste(int id) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Authentification avec gestion des tentatives et verrouillage
     */
    public Employe authentifier(String username, String password) {
        // Vérifier si le compte est verrouillé
        if (isAccountLocked(username)) {
            System.out.println("🔒 Compte verrouillé: " + username);
            return null;
        }

        String sql = "SELECT * FROM utilisateur WHERE username = ? AND password = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Employe e = extractEmployeFromResultSet(rs);

                // Vérifier si le compte est actif
                if ("inactif".equals(e.getStatut())) {
                    System.out.println("❌ Compte inactif: " + username);
                    return null;
                }

                // Réinitialiser les tentatives échouées
                resetFailedAttempts(e.getId());

                System.out.println("✅ Authentification réussie: " + username + " (" + e.getRole() + ")");
                return e;
            } else {
                // Gérer les tentatives échouées
                handleFailedLogin(username);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur authentification: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gère les tentatives de connexion échouées
     */
    private void handleFailedLogin(String username) {
        String sql = "UPDATE utilisateur SET failed_login_attempts = failed_login_attempts + 1 WHERE username = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();

            // Vérifier si le seuil est atteint
            sql = "SELECT failed_login_attempts FROM utilisateur WHERE username = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                ps2.setString(1, username);
                ResultSet rs = ps2.executeQuery();
                if (rs.next()) {
                    int attempts = rs.getInt("failed_login_attempts");
                    if (attempts >= MAX_LOGIN_ATTEMPTS) {
                        lockAccount(username);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verrouille un compte
     */
    private void lockAccount(String username) {
        String sql = "UPDATE utilisateur SET account_locked = 1, locked_until = ? WHERE username = ?";
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lockUntil.toString());
            ps.setString(2, username);
            ps.executeUpdate();
            System.out.println("🔒 Compte verrouillé: " + username + " jusqu'à " + lockUntil);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Réinitialise les tentatives échouées
     */
    private void resetFailedAttempts(int userId) {
        String sql = "UPDATE utilisateur SET failed_login_attempts = 0 WHERE id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si un compte est verrouillé
     */
    public boolean isAccountLocked(String username) {
        String sql = "SELECT account_locked, locked_until FROM utilisateur WHERE username = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (!rs.getBoolean("account_locked")) {
                    return false;
                }
                String lockedUntil = rs.getString("locked_until");
                if (lockedUntil != null && !lockedUntil.isEmpty()) {
                    try {
                        LocalDateTime lockTime = LocalDateTime.parse(lockedUntil);
                        if (lockTime.isBefore(LocalDateTime.now())) {
                            // Déverrouiller automatiquement
                            unlockAccount(username);
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        return true;
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Déverrouille un compte
     */
    private void unlockAccount(String username) {
        String sql = "UPDATE utilisateur SET account_locked = 0, locked_until = NULL, failed_login_attempts = 0 WHERE username = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();
            System.out.println("🔓 Compte déverrouillé: " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extrait un Employe depuis un ResultSet
     */
    private Employe extractEmployeFromResultSet(ResultSet rs) throws SQLException {
        Employe e = new Employe();
        e.setId(rs.getInt("id"));

        // Vérifier si la colonne username existe
        try { e.setUsername(rs.getString("username")); } catch (SQLException ex) {
            e.setUsername(rs.getString("email")); // Fallback à email
        }

        e.setEmail(rs.getString("email"));
        e.setTel(rs.getString("tel"));
        e.setPassword(rs.getString("password"));
        e.setRole(rs.getString("role"));
        e.setDateAjout(rs.getString("date_ajout"));

        // Champs optionnels
        try { e.setStatut(rs.getString("statut")); } catch (SQLException ex) { e.setStatut("actif"); }
        try { e.setDepartment(rs.getString("department")); } catch (SQLException ex) { e.setDepartment(null); }
        try { e.setPoste(rs.getString("poste")); } catch (SQLException ex) { e.setPoste(null); }
        try { e.setDateEmbauche(rs.getString("date_embauche")); } catch (SQLException ex) { e.setDateEmbauche(null); }
        try { e.setSalaire(rs.getDouble("salaire")); } catch (SQLException ex) { e.setSalaire(0); }
        try { e.setCompetences(rs.getString("competences")); } catch (SQLException ex) { e.setCompetences(null); }

        // Champs de sécurité
        try { e.setFailedLoginAttempts(rs.getInt("failed_login_attempts")); } catch (SQLException ex) { e.setFailedLoginAttempts(0); }
        try { e.setAccountLocked(rs.getBoolean("account_locked")); } catch (SQLException ex) { e.setAccountLocked(false); }
        try { e.setLockedUntil(rs.getString("locked_until")); } catch (SQLException ex) { e.setLockedUntil(null); }
        try { e.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled")); } catch (SQLException ex) { e.setTwoFactorEnabled(false); }
        try { e.setTwoFactorSecret(rs.getString("two_factor_secret")); } catch (SQLException ex) { e.setTwoFactorSecret(null); }

        return e;
    }

    /**
     * Récupère tous les responsables
     */
    public List<Employe> getAllResponsables() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role LIKE 'responsable%' OR role = 'admin' OR role = 'ceo'";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractEmployeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère uniquement les employés
     */
    public List<Employe> getAllEmployesOnly() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = 'employe' OR role = 'EMPLOYE'";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractEmployeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}