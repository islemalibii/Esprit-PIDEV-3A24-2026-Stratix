package services;
import interfaces.ProjServices;
import models.Projet;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjetService implements ProjServices {

    private final Connection connection;

    public ProjetService() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouterProjet(Projet p) {
        String sql = "INSERT INTO projet (nom, description, date_debut, date_fin, budget, statut, responsable_id, equipe_membres, progression, is_archived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDate(3, new java.sql.Date(p.getDateDebut().getTime()));
            ps.setDate(4, new java.sql.Date(p.getDateFin().getTime()));
            ps.setDouble(5, p.getBudget());
            ps.setString(6, p.getStatut());
            ps.setInt(7, p.getResponsableId());
            ps.setString(8, p.getEquipeMembres());
            ps.setInt(9, 0); // progression initiale
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Projet> listerTousLesProjets() {
        return recupererParEtat(0);
    }

    @Override
    public List<Projet> listerArchives() {
        return recupererParEtat(1);
    }

    private List<Projet> recupererParEtat(int etat) {
        List<Projet> projets = new ArrayList<>();

        String sql = "SELECT p.*, u.nom AS nom_chef " +
                "FROM projet p " +
                "LEFT JOIN utilisateur u ON p.responsable_id = u.id " +
                "WHERE p.is_archived = ? ORDER BY p.id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, etat);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Projet p = new Projet(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin"),
                        rs.getDouble("budget"),
                        rs.getString("statut"),
                        rs.getInt("progression"),
                        rs.getBoolean("is_archived"),
                        rs.getInt("responsable_id"),
                        nettoyerNomsEquipe(rs.getString("equipe_membres"))
                );

                p.setNomResponsable(rs.getString("nom_chef"));
                projets.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projets;
    }

    @Override
    public Projet chercherProjetParId(int idProjet) {
        String sql = "SELECT p.*, u.nom AS nom_chef FROM projet p " +
                "LEFT JOIN utilisateur u ON p.responsable_id = u.id WHERE p.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idProjet);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Projet p = new Projet(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin"),
                        rs.getDouble("budget"),
                        rs.getString("statut"),
                        rs.getInt("progression"),
                        rs.getBoolean("is_archived"),
                        rs.getInt("responsable_id"),
                        nettoyerNomsEquipe(rs.getString("equipe_membres"))
                );

                p.setNomResponsable(rs.getString("nom_chef"));
                return p;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



    @Override
    public void mettreAJourProjet(Projet p) {
        String sql = "UPDATE projet SET nom=?, description=?, date_debut=?, date_fin=?, budget=?, statut=?, responsable_id=?, equipe_membres=?, progression=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDate(3, new java.sql.Date(p.getDateDebut().getTime()));
            ps.setDate(4, new java.sql.Date(p.getDateFin().getTime()));
            ps.setDouble(5, p.getBudget());
            ps.setString(6, p.getStatut());
            ps.setInt(7, p.getResponsableId());
            ps.setString(8, p.getEquipeMembres());
            ps.setInt(9, p.getProgression());
            ps.setInt(10, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void archiverUnProjet(int id) {
        changerEtatArchive(id, 1);
    }

    @Override
    public void desarchiverUnProjet(int id) {
        changerEtatArchive(id, 0);
    }

    private void changerEtatArchive(int id, int etat) {
        String sql = "UPDATE projet SET is_archived = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, etat);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerUnProjet(int id) {
        String sql = "DELETE FROM projet WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Projet> rechercherProjets(String query, String statut) {
        return listerTousLesProjets().stream()
                .filter(p -> statut.equals("Tous les projets") || p.getStatut().equals(statut))
                .filter(p -> p.getNom().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    private String nettoyerNomsEquipe(String bruts) {
        if (bruts == null || bruts.isEmpty()) return "Aucun membre";
        return bruts.replaceAll("\\d+\\s*-\\s*", "");
    }




    public List<Projet> getAllProjets() {
        List<Projet> list = new ArrayList<>();
        String sql = "SELECT * FROM projet WHERE is_archived = 0 ORDER BY id DESC";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractProjetFromResultSet(rs));
            }
            System.out.println("✅ " + list.size() + " projets chargés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllProjets: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Projet getProjetById(int id) {
        String sql = "SELECT * FROM projet WHERE id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractProjetFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getProjetById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void mettreAJourProgression(int projetId) {
        System.out.println("\n🔄 ===== MISE À JOUR PROGRESSION =====");
        System.out.println("🔄 Projet ID: " + projetId);

        Projet projet = getProjetById(projetId);
        if (projet == null) {
            System.out.println("❌ ERREUR: Projet ID " + projetId + " n'existe pas!");
            return;
        }
        System.out.println("✅ Projet trouvé: " + projet.getNom());

        String sqlTaches = "SELECT id, titre, statut FROM tache WHERE projet_id = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sqlTaches)) {

            ps.setInt(1, projetId);
            ResultSet rs = ps.executeQuery();

            System.out.println("📋 Tâches du projet:");
            boolean hasTasks = false;
            while (rs.next()) {
                hasTasks = true;
                System.out.println("   - ID: " + rs.getInt("id") +
                        " | Titre: " + rs.getString("titre") +
                        " | Statut: '" + rs.getString("statut") + "'");
            }
            if (!hasTasks) {
                System.out.println("   ⚠️ Aucune tâche trouvée pour ce projet");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur affichage tâches: " + e.getMessage());
            e.printStackTrace();
        }

        String sqlCount = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN statut = 'TERMINEE' THEN 1 ELSE 0 END) as terminees " +
                "FROM tache WHERE projet_id = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sqlCount)) {

            ps.setInt(1, projetId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                int terminees = rs.getInt("terminees");

                System.out.println("📊 RÉSULTATS DU CALCUL:");
                System.out.println("   - Total tâches: " + total);
                System.out.println("   - Tâches terminées (TERMINEE): " + terminees);

                int progression = 0;
                if (total > 0) {
                    progression = (terminees * 100) / total;
                }

                System.out.println("   📈 Progression calculée: " + progression + "%");

                String sqlUpdate = "UPDATE projet SET progression = ? WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                    psUpdate.setInt(1, progression);
                    psUpdate.setInt(2, projetId);
                    int rowsAffected = psUpdate.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("✅ SUCCÈS: Progression mise à jour à " + progression + "%");
                    } else {
                        System.out.println("⚠️ Aucune ligne mise à jour - Vérifie l'ID du projet");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour progression: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🔄 ===== FIN MISE À JOUR =====\n");
    }

    private Projet extractProjetFromResultSet(ResultSet rs) throws SQLException {
        Projet p = new Projet();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setDateDebut(rs.getDate("date_debut"));
        p.setDateFin(rs.getDate("date_fin"));
        p.setBudget(rs.getDouble("budget"));
        p.setStatut(rs.getString("statut"));
        p.setProgression(rs.getInt("progression"));
        p.setArchived(rs.getBoolean("is_archived"));

        try { p.setResponsableId(rs.getInt("responsable_id")); } catch (SQLException e) { p.setResponsableId(0); }
        try { p.setEquipeMembres(rs.getString("equipe_membres")); } catch (SQLException e) { p.setEquipeMembres(""); }

        return p;
    }
}