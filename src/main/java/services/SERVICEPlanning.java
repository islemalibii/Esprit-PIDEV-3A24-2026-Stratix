package services;

import models.Planning;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SERVICEPlanning {


    public void addPlanning(Planning p) {
        String sql = """
            INSERT INTO planning (employe_id, date, heure_debut, heure_fin, type_shift)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, p.getEmployeId());
            ps.setDate(2, p.getDate());
            ps.setTime(3, p.getHeureDebut());
            ps.setTime(4, p.getHeureFin());
            ps.setString(5, p.getTypeShift());

            ps.executeUpdate();
            System.out.println("✅ Planning ajouté (ID employé: " + p.getEmployeId() + ")");

        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout planning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Planning> getAllPlannings() {
        List<Planning> list = new ArrayList<>();
        String sql = "SELECT * FROM planning ORDER BY date DESC, heure_debut ASC";

        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Planning p = new Planning();
                p.setId(rs.getInt("id"));
                p.setEmployeId(rs.getInt("employe_id"));
                p.setDate(rs.getDate("date"));
                p.setHeureDebut(rs.getTime("heure_debut"));
                p.setHeureFin(rs.getTime("heure_fin"));
                p.setTypeShift(rs.getString("type_shift"));

                list.add(p);
            }

            System.out.println("✅ " + list.size() + " plannings chargés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement plannings: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // Récupérer un planning par ID
    public Planning getPlanningById(int id) {
        String sql = "SELECT * FROM planning WHERE id = ?";

        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Planning p = new Planning();
                p.setId(rs.getInt("id"));
                p.setEmployeId(rs.getInt("employe_id"));
                p.setDate(rs.getDate("date"));
                p.setHeureDebut(rs.getTime("heure_debut"));
                p.setHeureFin(rs.getTime("heure_fin"));
                p.setTypeShift(rs.getString("type_shift"));
                return p;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération planning ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlanning(Planning p) {
        String sql = """
            UPDATE planning
            SET employe_id=?, date=?, heure_debut=?, heure_fin=?, type_shift=?
            WHERE id=?
        """;

        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, p.getEmployeId());
            ps.setDate(2, p.getDate());
            ps.setTime(3, p.getHeureDebut());
            ps.setTime(4, p.getHeureFin());
            ps.setString(5, p.getTypeShift());
            ps.setInt(6, p.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Planning modifié (ID: " + p.getId() + ")");
            } else {
                System.out.println("⚠️ Aucun planning trouvé avec ID: " + p.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur modification planning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deletePlanning(int id) {
        String sql = "DELETE FROM planning WHERE id=?";

        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Planning supprimé (ID: " + id + ")");
            } else {
                System.out.println("⚠️ Aucun planning trouvé avec ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression planning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Compter le nombre total d'utilisateurs avec rôle employeeeé
    public int compterTotalEmployes() {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = 'employe' OR role = 'EMPLOYE' OR role LIKE 'responsable%'";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage employés: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Compter les employés en poste aujourd'hui
    public int compterEnPoste() {
        String sql = "SELECT COUNT(DISTINCT employe_id) FROM planning WHERE date = CURDATE()";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage en poste: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Compter les employés absents aujourd'hui
    public int compterAbsents() {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE (role = 'employe' OR role = 'EMPLOYE' OR role LIKE 'responsable%') " +
                "AND id NOT IN (SELECT DISTINCT employe_id FROM planning WHERE date = CURDATE())";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage absents: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Pour obtenir les statistiques du jour spécifique
    public int compterEnPosteParDate(LocalDate date) {
        String sql = "SELECT COUNT(DISTINCT employe_id) FROM planning WHERE date = ?";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage en poste par date: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int compterAbsentsParDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE (role = 'employe' OR role = 'EMPLOYE' OR role LIKE 'responsable%') " +
                "AND id NOT IN (SELECT DISTINCT employe_id FROM planning WHERE date = ?)";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage absents par date: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Compter par type de shift
    public int compterParShift(String shift) {
        String sql = "SELECT COUNT(*) FROM planning WHERE type_shift = ?";
        try (Connection c = MyDataBase.getInstance().getCnx();  // ✅ CORRIGÉ
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, shift);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage shift " + shift + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}//