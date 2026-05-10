package services;

import models.Phase;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhaseService {

    private final Connection connection;

    public PhaseService() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    public void ajouterPhase(Phase phase) {
        String sql = "INSERT INTO phase (projet_id, nom, objectif, date_debut, date_fin, statut, velocite_estimee, velocite_reelle) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, phase.getProjetId());
            ps.setString(2, phase.getNom());
            ps.setString(3, phase.getObjectif());
            ps.setTimestamp(4, new Timestamp(phase.getDateDebut().getTime()));
            ps.setTimestamp(5, new Timestamp(phase.getDateFin().getTime()));
            ps.setString(6, phase.getStatut());
            if (phase.getVelociteEstimee() != null)
                ps.setDouble(7, phase.getVelociteEstimee());
            else
                ps.setNull(7, Types.DOUBLE);
            if (phase.getVelociteReelle() != null)
                ps.setDouble(8, phase.getVelociteReelle());
            else
                ps.setNull(8, Types.DOUBLE);
            ps.executeUpdate();
            System.out.println("✅ Phase ajoutée: " + phase.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout phase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────
    public List<Phase> listerParProjet(int projetId) {
        List<Phase> phases = new ArrayList<>();
        String sql = "SELECT * FROM phase WHERE projet_id = ? ORDER BY date_debut ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Double ve = rs.getObject("velocite_estimee") != null ? rs.getDouble("velocite_estimee") : null;
                Double vr = rs.getObject("velocite_reelle")  != null ? rs.getDouble("velocite_reelle")  : null;
                phases.add(new Phase(
                        rs.getInt("id"),
                        rs.getInt("projet_id"),
                        rs.getString("nom"),
                        rs.getString("objectif"),
                        rs.getTimestamp("date_debut"),
                        rs.getTimestamp("date_fin"),
                        rs.getString("statut"),
                        ve, vr
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur liste phases: " + e.getMessage());
            e.printStackTrace();
        }
        return phases;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void modifierPhase(Phase phase) {
        String sql = "UPDATE phase SET nom=?, objectif=?, date_debut=?, date_fin=?, statut=?, " +
                "velocite_estimee=?, velocite_reelle=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phase.getNom());
            ps.setString(2, phase.getObjectif());
            ps.setTimestamp(3, new Timestamp(phase.getDateDebut().getTime()));
            ps.setTimestamp(4, new Timestamp(phase.getDateFin().getTime()));
            ps.setString(5, phase.getStatut());
            if (phase.getVelociteEstimee() != null)
                ps.setDouble(6, phase.getVelociteEstimee());
            else
                ps.setNull(6, Types.DOUBLE);
            if (phase.getVelociteReelle() != null)
                ps.setDouble(7, phase.getVelociteReelle());
            else
                ps.setNull(7, Types.DOUBLE);
            ps.setInt(8, phase.getId());
            ps.executeUpdate();
            System.out.println("✅ Phase modifiée: " + phase.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification phase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void supprimerPhase(int id) {
        String sql = "DELETE FROM phase WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Phase supprimée id=" + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression phase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}