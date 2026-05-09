package services;

import models.Tache;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SERVICETache {

    // ⭐ AJOUT DU SERVICE PROJET
    private ProjetService projetService = new ProjetService();

    // CREATE
    public void addTache(Tache t) {
        String sql = """
            INSERT INTO tache (titre, description, deadline, statut, employe_id, projet_id, priorite)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Connection c = MyDataBase.getInstance().getCnx();
        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setDate(3, t.getDeadline());
            ps.setString(4, t.getStatut());
            ps.setInt(5, t.getEmployeId());
            ps.setInt(6, t.getProjetId());
            ps.setString(7, t.getPriorite());

            ps.executeUpdate();
            System.out.println("✅ Tâche ajoutée: " + t.getTitre());

            // ⭐ MISE À JOUR AUTOMATIQUE DE LA PROGRESSION
            System.out.println("🔔 Appel de mettreAJourProgression pour projet " + t.getProjetId());
            projetService.mettreAJourProgression(t.getProjetId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur addTache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ
    public List<Tache> getAllTaches() {
        List<Tache> list = new ArrayList<>();
        String sql = "SELECT * FROM tache";

        Connection c = MyDataBase.getInstance().getCnx();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Tache t = new Tache();
                t.setId(rs.getInt("id"));
                t.setTitre(rs.getString("titre"));
                t.setDescription(rs.getString("description"));
                t.setDeadline(rs.getDate("deadline"));
                t.setStatut(rs.getString("statut"));
                t.setEmployeId(rs.getInt("employe_id"));
                t.setProjetId(rs.getInt("projet_id"));
                t.setPriorite(rs.getString("priorite"));

                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE
    public void updateTache(Tache t) {
        String sql = """
            UPDATE tache
            SET titre=?, description=?, deadline=?, statut=?, employe_id=?, projet_id=?, priorite=?
            WHERE id=?
        """;

        Connection c = MyDataBase.getInstance().getCnx();
        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setDate(3, t.getDeadline());
            ps.setString(4, t.getStatut());
            ps.setInt(5, t.getEmployeId());
            ps.setInt(6, t.getProjetId());
            ps.setString(7, t.getPriorite());
            ps.setInt(8, t.getId());

            ps.executeUpdate();
            System.out.println("✅ Tâche mise à jour: " + t.getTitre());

            // ⭐ MISE À JOUR AUTOMATIQUE DE LA PROGRESSION
            System.out.println("🔔 Appel de mettreAJourProgression pour projet " + t.getProjetId());
            projetService.mettreAJourProgression(t.getProjetId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur updateTache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteTache(int id) {
        // ⭐ RÉCUPÉRER LE PROJET_ID AVANT SUPPRESSION
        int projetId = -1;
        Tache t = getTacheById(id);
        if (t != null) {
            projetId = t.getProjetId();
            System.out.println("🔍 Tâche trouvée - Projet ID: " + projetId);
        }

        String sql = "DELETE FROM tache WHERE id=?";

        Connection c = MyDataBase.getInstance().getCnx();
        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Tâche supprimée (ID: " + id + ")");

                // ⭐ MISE À JOUR DE LA PROGRESSION SI ON AVAIT LE PROJET_ID
                if (projetId != -1) {
                    System.out.println("🔔 Appel de mettreAJourProgression pour projet " + projetId);
                    projetService.mettreAJourProgression(projetId);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur deleteTache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // GET BY ID
    public Tache getTacheById(int id) {
        String sql = "SELECT * FROM tache WHERE id = ?";

        Connection c = MyDataBase.getInstance().getCnx();
        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tache t = new Tache();
                    t.setId(rs.getInt("id"));
                    t.setTitre(rs.getString("titre"));
                    t.setDescription(rs.getString("description"));
                    t.setDeadline(rs.getDate("deadline"));
                    t.setStatut(rs.getString("statut"));
                    t.setEmployeId(rs.getInt("employe_id"));
                    t.setProjetId(rs.getInt("projet_id"));
                    t.setPriorite(rs.getString("priorite"));
                    return t;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getTacheById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}