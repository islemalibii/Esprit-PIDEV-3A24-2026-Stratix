package services;

import interfaces.service;
import models.produit;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class service_produit implements service<produit> {

    @Override
    public void add(produit p) {
        // AJOUT du champ details dans la requête
        String req = "INSERT INTO produit(nom, description, categorie, prix, stock_actuel, stock_min, date_creation, ressources_necessaires, image_path, date_fabrication, date_peremption, date_garantie, details) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCategorie());
            ps.setDouble(4, p.getPrix());
            ps.setInt(5, p.getStock_actuel());
            ps.setInt(6, p.getStock_min());
            ps.setDate(7, Date.valueOf(p.getDate_creation()));
            ps.setString(8, p.getRessources_necessaires());

            // Gestion de l'image_path
            if (p.getImage_path() != null && !p.getImage_path().isEmpty()) {
                ps.setString(9, p.getImage_path());
            } else {
                ps.setNull(9, java.sql.Types.VARCHAR);
            }

            // Dates (peuvent être null)
            if (p.getDate_fabrication() != null && !p.getDate_fabrication().isEmpty()) {
                ps.setDate(10, Date.valueOf(p.getDate_fabrication()));
            } else {
                ps.setNull(10, java.sql.Types.DATE);
            }

            if (p.getDate_peremption() != null && !p.getDate_peremption().isEmpty()) {
                ps.setDate(11, Date.valueOf(p.getDate_peremption()));
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }

            if (p.getDate_garantie() != null && !p.getDate_garantie().isEmpty()) {
                ps.setDate(12, Date.valueOf(p.getDate_garantie()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            // Gestion des détails (NOUVEAU)
            if (p.getDetails() != null && !p.getDetails().isEmpty()) {
                ps.setString(13, p.getDetails());
            } else {
                ps.setNull(13, java.sql.Types.VARCHAR);
            }

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
                System.out.println("Produit ajouté avec succès ! ID: " + p.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur add produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<produit> getAll() {
        List<produit> list = new ArrayList<>();
        // AJOUT du champ details dans la requête SELECT
        String req = "SELECT id, nom, description, categorie, prix, stock_actuel, stock_min, date_creation, ressources_necessaires, image_path, date_fabrication, date_peremption, date_garantie, details FROM produit";

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                produit p = new produit();

                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setCategorie(rs.getString("categorie"));
                p.setPrix(rs.getDouble("prix"));
                p.setStock_actuel(rs.getInt("stock_actuel"));
                p.setStock_min(rs.getInt("stock_min"));

                Date dateSql = rs.getDate("date_creation");
                if (dateSql != null) {
                    p.setDate_creation(dateSql.toString());
                }

                p.setRessources_necessaires(rs.getString("ressources_necessaires"));

                // Image
                try {
                    p.setImage_path(rs.getString("image_path"));
                } catch (SQLException e) {
                    p.setImage_path(null);
                }

                // Dates
                Date dateFab = rs.getDate("date_fabrication");
                if (dateFab != null) {
                    p.setDate_fabrication(dateFab.toString());
                }

                Date datePer = rs.getDate("date_peremption");
                if (datePer != null) {
                    p.setDate_peremption(datePer.toString());
                }

                Date dateGar = rs.getDate("date_garantie");
                if (dateGar != null) {
                    p.setDate_garantie(dateGar.toString());
                }

                // Détails (NOUVEAU)
                try {
                    p.setDetails(rs.getString("details"));
                } catch (SQLException e) {
                    p.setDetails(null);
                }

                list.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll produit : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void update(produit p) {
        // AJOUT du champ details dans la requête UPDATE
        String req = "UPDATE produit SET nom=?, description=?, categorie=?, prix=?, stock_actuel=?, stock_min=?, date_creation=?, ressources_necessaires=?, image_path=?, date_fabrication=?, date_peremption=?, date_garantie=?, details=? WHERE id=?";

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCategorie());
            ps.setDouble(4, p.getPrix());
            ps.setInt(5, p.getStock_actuel());
            ps.setInt(6, p.getStock_min());
            ps.setDate(7, Date.valueOf(p.getDate_creation()));
            ps.setString(8, p.getRessources_necessaires());

            // Image
            if (p.getImage_path() != null && !p.getImage_path().isEmpty()) {
                ps.setString(9, p.getImage_path());
            } else {
                ps.setNull(9, java.sql.Types.VARCHAR);
            }

            // Dates
            if (p.getDate_fabrication() != null && !p.getDate_fabrication().isEmpty()) {
                ps.setDate(10, Date.valueOf(p.getDate_fabrication()));
            } else {
                ps.setNull(10, java.sql.Types.DATE);
            }

            if (p.getDate_peremption() != null && !p.getDate_peremption().isEmpty()) {
                ps.setDate(11, Date.valueOf(p.getDate_peremption()));
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }

            if (p.getDate_garantie() != null && !p.getDate_garantie().isEmpty()) {
                ps.setDate(12, Date.valueOf(p.getDate_garantie()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            // Détails (NOUVEAU)
            if (p.getDetails() != null && !p.getDetails().isEmpty()) {
                ps.setString(13, p.getDetails());
            } else {
                ps.setNull(13, java.sql.Types.VARCHAR);
            }

            ps.setInt(14, p.getId());

            int rows = ps.executeUpdate();

            if(rows > 0)
                System.out.println("Produit modifié ! ID: " + p.getId());
            else
                System.out.println("Produit introuvable !");

        } catch (SQLException e) {
            System.out.println("Erreur update produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(produit p) {
        String req = "DELETE FROM produit WHERE id=?";

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(req);

            ps.setInt(1, p.getId());

            int rows = ps.executeUpdate();

            if(rows > 0)
                System.out.println("Produit supprimé ! ID: " + p.getId());
            else
                System.out.println("Produit introuvable !");

        } catch (SQLException e) {
            System.out.println("Erreur delete produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM produit";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.executeUpdate();
            System.out.println("Table produit vidée avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }
}