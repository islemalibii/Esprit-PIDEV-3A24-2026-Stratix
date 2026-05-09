package services;

import models.DashboardStats;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatsService {

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        Connection conn = null;

        try {
            conn = MyDataBase.getInstance().getCnx();

            // 1. Total des tâches
            String sqlTaches = "SELECT COUNT(*) FROM tache";
            PreparedStatement ps = conn.prepareStatement(sqlTaches);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) stats.setTotalTaches(rs.getInt(1));

            // 2. Tâches en cours
            sqlTaches = "SELECT COUNT(*) FROM tache WHERE statut = 'EN_COURS'";
            ps = conn.prepareStatement(sqlTaches);
            rs = ps.executeQuery();
            if (rs.next()) stats.setTachesEnCours(rs.getInt(1));

            // 3. Tâches terminées
            sqlTaches = "SELECT COUNT(*) FROM tache WHERE statut = 'TERMINEE'";
            ps = conn.prepareStatement(sqlTaches);
            rs = ps.executeQuery();
            if (rs.next()) stats.setTachesTerminees(rs.getInt(1));

            // 4. Tâches à faire
            sqlTaches = "SELECT COUNT(*) FROM tache WHERE statut = 'A_FAIRE'";
            ps = conn.prepareStatement(sqlTaches);
            rs = ps.executeQuery();
            if (rs.next()) stats.setTachesAFaire(rs.getInt(1));

            // 5. Tâches en retard (deadline dépassée et pas terminée)
            sqlTaches = "SELECT COUNT(*) FROM tache WHERE deadline < CURDATE() AND statut != 'TERMINEE'";
            ps = conn.prepareStatement(sqlTaches);
            rs = ps.executeQuery();
            if (rs.next()) stats.setTachesEnRetard(rs.getInt(1));

            // 6. Total employés
            String sqlEmployes = "SELECT COUNT(*) FROM utilisateur WHERE role = 'employe' OR role = 'EMPLOYE' OR role LIKE 'responsable%'";
            ps = conn.prepareStatement(sqlEmployes);
            rs = ps.executeQuery();
            if (rs.next()) stats.setTotalEmployes(rs.getInt(1));

            // 7. Employés en poste aujourd'hui
            String sqlEnPoste = "SELECT COUNT(DISTINCT employe_id) FROM planning WHERE date = CURDATE()";
            ps = conn.prepareStatement(sqlEnPoste);
            rs = ps.executeQuery();
            if (rs.next()) stats.setEmployesEnPoste(rs.getInt(1));

            // 8. Employés absents
            int total = stats.getTotalEmployes();
            int enPoste = stats.getEmployesEnPoste();
            stats.setEmployesAbsents(total - enPoste);

        } catch (Exception e) {
            System.err.println("❌ Erreur StatsService: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }
}