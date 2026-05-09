package services;

import models.Role;
import models.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {
    private static UtilisateurService instance;
    private Connection connection;

    private UtilisateurService() {
        initConnection();
    }

    private void initConnection() {
        connection = MyDataBase.getInstance().getCnx();
        if (connection == null) {
            System.err.println("ATTENTION: Connexion à la base de données non disponible");
        }
    }

    // Vérifier et rétablir la connexion si nécessaire
    private void checkConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔄 Reconnexion à la base de données...");
                initConnection();
                if (connection == null || connection.isClosed()) {
                    throw new SQLException("Connexion à la base de données non disponible");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification connexion: " + e.getMessage());
            initConnection();
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connexion à la base de données non disponible", e);
            }
        }
    }

    public static UtilisateurService getInstance() {
        if (instance == null) {
            instance = new UtilisateurService();
        }
        return instance;
    }

    // Ajouter un utilisateur
    public void ajouter(Utilisateur utilisateur) throws SQLException {
        checkConnection();

        String query = "INSERT INTO utilisateur (nom, prenom, email, tel, cin, password, role, statut, date_ajout, " +
                "department, poste, date_embauche, competences, salaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setString(4, utilisateur.getTel());
        ps.setInt(5, utilisateur.getCin());
        ps.setString(6, utilisateur.getPassword());
        ps.setString(7, utilisateur.getRole().name().toLowerCase());
        ps.setString(8, utilisateur.getStatut() != null ? utilisateur.getStatut() : "actif");
        ps.setDate(9, Date.valueOf(utilisateur.getDateAjout() != null ? utilisateur.getDateAjout() : LocalDate.now()));

        ps.setString(10, utilisateur.getDepartment());
        ps.setString(11, utilisateur.getPoste());
        ps.setDate(12, utilisateur.getDateEmbauche() != null ? Date.valueOf(utilisateur.getDateEmbauche()) : null);
        ps.setString(13, utilisateur.getCompetences());

        if (utilisateur.getSalaire() == 0.0) {
            ps.setNull(14, Types.DECIMAL);
        } else {
            ps.setDouble(14, utilisateur.getSalaire());
        }

        ps.executeUpdate();
    }

    // Récupérer tous les utilisateurs
    public List<Utilisateur> getAll() throws SQLException {
        checkConnection();

        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            utilisateurs.add(mapResultSetToUtilisateur(rs));
        }

        return utilisateurs;
    }

    // Récupérer par rôle
    public List<Utilisateur> getByRole(Role role) throws SQLException {
        checkConnection();

        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, role.name().toLowerCase());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            utilisateurs.add(mapResultSetToUtilisateur(rs));
        }

        return utilisateurs;
    }

    // Récupérer par ID
    public Utilisateur getById(int id) throws SQLException {
        checkConnection();

        String query = "SELECT * FROM utilisateur WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToUtilisateur(rs);
        }
        return null;
    }

    // Récupérer par email (pour login)
    public Utilisateur getByEmail(String email) throws SQLException {
        checkConnection();

        String query = "SELECT * FROM utilisateur WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToUtilisateur(rs);
        }
        return null;
    }

    // Modifier un utilisateur
    public void modifier(Utilisateur utilisateur) throws SQLException {
        checkConnection();

        String query = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, tel = ?, cin = ?, " +
                "password = ?, role = ?, department = ?, poste = ?, competences = ?, salaire = ? " +
                "WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, utilisateur.getNom());
        ps.setString(2, utilisateur.getPrenom());
        ps.setString(3, utilisateur.getEmail());
        ps.setString(4, utilisateur.getTel());
        ps.setInt(5, utilisateur.getCin());
        ps.setString(6, utilisateur.getPassword());
        ps.setString(7, utilisateur.getRole().name().toLowerCase());
        ps.setString(8, utilisateur.getDepartment());
        ps.setString(9, utilisateur.getPoste());
        ps.setString(10, utilisateur.getCompetences());

        if (utilisateur.getSalaire() == 0.0) {
            ps.setNull(11, Types.DECIMAL);
        } else {
            ps.setDouble(11, utilisateur.getSalaire());
        }
        ps.setInt(12, utilisateur.getId());

        ps.executeUpdate();
    }

    // Supprimer un utilisateur
    public void supprimer(int id) throws SQLException {
        checkConnection();

        String query = "DELETE FROM utilisateur WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Mettre à jour le mot de passe
    public void updatePassword(String email, String hashedPassword) throws SQLException {
        checkConnection();

        String query = "UPDATE utilisateur SET password = ? WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, hashedPassword);
        ps.setString(2, email);
        ps.executeUpdate();
    }

    // Vérifier si un email existe déjà
    public boolean emailExists(String email) throws SQLException {
        checkConnection();

        String query = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // Vérifier si un email existe déjà (en excluant un utilisateur spécifique)
    public boolean emailExistsExcludingUser(String email, int userId) throws SQLException {
        checkConnection();

        String query = "SELECT COUNT(*) FROM utilisateur WHERE email = ? AND id != ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, email);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // Vérifier si un CIN existe déjà
    public boolean cinExists(int cin) throws SQLException {
        checkConnection();

        String query = "SELECT COUNT(*) FROM utilisateur WHERE cin = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, cin);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // Vérifier si un CIN existe déjà (en excluant un utilisateur spécifique)
    public boolean cinExistsExcludingUser(int cin, int userId) throws SQLException {
        checkConnection();

        String query = "SELECT COUNT(*) FROM utilisateur WHERE cin = ? AND id != ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, cin);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // Mapper ResultSet vers Utilisateur
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setTel(rs.getString("tel"));
        utilisateur.setCin(rs.getInt("cin"));
        utilisateur.setPassword(rs.getString("password"));

        String roleStr = rs.getString("role");
        utilisateur.setRole(Role.valueOf(roleStr.toUpperCase()));

        try {
            utilisateur.setStatut(rs.getString("statut"));
        } catch (SQLException e) {
            utilisateur.setStatut("actif");
        }

        Date dateAjout = rs.getDate("date_ajout");
        if (dateAjout != null) {
            utilisateur.setDateAjout(dateAjout.toLocalDate());
        }

        utilisateur.setDepartment(rs.getString("department"));
        utilisateur.setPoste(rs.getString("poste"));
        utilisateur.setCompetences(rs.getString("competences"));
        utilisateur.setSalaire(rs.getDouble("salaire"));

        Date dateEmbauche = rs.getDate("date_embauche");
        if (dateEmbauche != null) {
            utilisateur.setDateEmbauche(dateEmbauche.toLocalDate());
        }

        try {
            utilisateur.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
            utilisateur.setTwoFactorSecret(rs.getString("two_factor_secret"));
        } catch (SQLException e) {
            utilisateur.setTwoFactorEnabled(false);
        }

        return utilisateur;
    }

    // Désactiver un utilisateur
    public void desactiver(int id) throws SQLException {
        checkConnection();

        String query = "UPDATE utilisateur SET statut = 'inactif' WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Activer un utilisateur
    public void activer(int id) throws SQLException {
        checkConnection();

        String query = "UPDATE utilisateur SET statut = 'actif' WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Récupérer uniquement les utilisateurs actifs
    public List<Utilisateur> getAllActifs() throws SQLException {
        checkConnection();

        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE statut = 'actif'";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            utilisateurs.add(mapResultSetToUtilisateur(rs));
        }

        return utilisateurs;
    }

    // Récupérer uniquement les employés (pour les plannings)
    public List<Utilisateur> getAllEmployes() throws SQLException {
        checkConnection();

        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role IN ('EMPLOYE', 'RESPONSABLE_RH', 'RESPONSABLE_PROJET', 'RESPONSABLE_PRODUCTION') AND statut = 'actif'";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            utilisateurs.add(mapResultSetToUtilisateur(rs));
        }

        return utilisateurs;
    }
}
