package services;

import interfaces.Services;
import models.Evenement;
import models.enums.EventStatus;
import models.enums.EventType;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.*;
import java.util.UUID;
import java.io.File;

public class ServiceEvenemnet implements Services<Evenement> {

    Connection cnx ;
    public ServiceEvenemnet() {
        cnx = MyDataBase.getInstance().getCnx();
    }
    //lladmin
    @Override
    public void add(Evenement evenement) {

        String req = "INSERT INTO evenement(type_event, date_event, description, statut, lieu, titre, image_url, latitude, longitude, isArchived, recurrence) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // l values ywalou treated as data mch sql ynajm yexecuta : protection contre l sql injection

        try (PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);) {

            pst.setString(1, evenement.getType_event().name());
            pst.setDate(2, java.sql.Date.valueOf(evenement.getDate_event())); // LocalDate ll SQL Date
            pst.setString(3, evenement.getDescription());
            pst.setString(4, evenement.getStatut().name());
            pst.setString(5, evenement.getLieu());
            pst.setString(6, evenement.getTitre());
            pst.setString(7, evenement.getImageUrl());
            pst.setDouble(8, evenement.getLatitude());
            pst.setDouble(9, evenement.getLongitude());

            pst.setInt(10, 0);          // 0 = false (not archived)
            pst.setString(11, "none");
            pst.executeUpdate();


            System.out.println("Evenement added successfully");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    //lladmin
    @Override
    public void update(Evenement evenement) {

        String req = "update evenement set type_event=? , date_event=? , description=? , statut=? , lieu=? , titre=?, image_url=?, latitude=?, longitude=?, recurrence=? "+
                " where id=?";
        try{
            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, evenement.getType_event().name());
            pst.setDate(2, java.sql.Date.valueOf(evenement.getDate_event())); // LocalDate → DATE
            pst.setString(3, evenement.getDescription());
            pst.setString(4, evenement.getStatut().name());
            pst.setString(5, evenement.getLieu());
            pst.setString(6, evenement.getTitre());
            pst.setString(7, evenement.getImageUrl());
            pst.setDouble(8, evenement.getLatitude());
            pst.setDouble(9, evenement.getLongitude());
            pst.setString(10, "none"); // Keeps recurrence valid
            pst.setInt(11, evenement.getId());


            pst.executeUpdate();
            System.out.println("evenement modifie");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    //lladmin
    public void deleteById(int id) {
        String sql = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //lladmin
    private List<Evenement> getByArchiveStatus(int status) {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE isArchived = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, status);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setType_event(EventType.valueOf(rs.getString("type_event")));
                e.setStatut(EventStatus.valueOf(rs.getString("statut")));
                e.setArchived(rs.getInt("isArchived") == 1);
                e.setImageUrl(rs.getString("image_url"));

                e.setLatitude(rs.getDouble("latitude"));
                e.setLongitude(rs.getDouble("longitude"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }
    @Override
    public List<Evenement> getAll() {
        return getByArchiveStatus(0);
    }

    public List<Evenement> getAllArchieved() {
        return getByArchiveStatus(1);
    }


    //lladmin
    private void updateArchiveStatus(int id, int status) {
        String req = "UPDATE evenement SET isArchived = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void archiver(int id) {
        updateArchiveStatus(id, 1);
    }

    public void desarchiver(int id) {
        updateArchiveStatus(id, 0);
    }



    //lladmin
    public List<Evenement> searchByTitle(String title) {
        List<Evenement> newList = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE titre LIKE ? AND isArchived = 0";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, "%" + title + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setType_event(EventType.valueOf(rs.getString("type_event").toLowerCase()));
                e.setStatut(EventStatus.valueOf(rs.getString("statut").toLowerCase()));
                e.setArchived(false);
                e.setImageUrl(rs.getString("image_url"));

                newList.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return newList;
    }

    //lladmin
    public List<Evenement> filterByStatus(EventStatus status) {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE statut = ? AND isArchived = 0";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, status.name());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setType_event(EventType.valueOf(rs.getString("type_event").toLowerCase()));
                e.setStatut(EventStatus.valueOf(rs.getString("statut").toLowerCase()));
                e.setImageUrl(rs.getString("image_url"));

                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    //llemployee
    public List<Evenement> getVisibleEventsForEmployee() {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE statut IN (?, ?) AND isArchived = 0";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "planifier");
            ps.setString(2, "terminer");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setImageUrl(rs.getString("image_url"));
                e.setStatut(EventStatus.valueOf(rs.getString("statut")));
                e.setType_event(EventType.valueOf(rs.getString("type_event")));

                e.setLatitude(rs.getDouble("latitude"));
                e.setLongitude(rs.getDouble("longitude"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return list;
    }

    //llemplyee
    public List<Evenement> searchPlanifierByTitle(String title) {
        List<Evenement> list = new ArrayList<>();

        String req = "SELECT * FROM evenement " + "WHERE statut = ? AND isArchived = 0 AND titre LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {

            ps.setString(1, EventStatus.planifier.name());
            ps.setString(2, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setImageUrl(rs.getString("image_url"));
                e.setStatut(EventStatus.valueOf(rs.getString("statut").toLowerCase()));
                e.setType_event(EventType.valueOf(rs.getString("type_event").toLowerCase()));

                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    //llemployee
    public List<Evenement> filterByType(EventType type) {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE type_event = ? AND statut = ? AND isArchived = 0";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, type.name());
            pst.setString(2, EventStatus.planifier.name());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setLieu(rs.getString("lieu"));
                e.setDate_event(rs.getDate("date_event").toLocalDate());
                e.setType_event(EventType.valueOf(rs.getString("type_event").toLowerCase()));
                e.setStatut(EventStatus.valueOf(rs.getString("statut").toLowerCase()));
                e.setImageUrl(rs.getString("image_url"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }


    public String saveImageToProject(File sourceFile) throws Exception {
        String uploadPath = "C:/Users/islem/stratix_web/public/uploads/events/";

        String extension = "";
        int i = sourceFile.getName().lastIndexOf('.');
        if (i > 0) { extension = sourceFile.getName().substring(i); }

        String newFileName = UUID.randomUUID().toString().substring(0, 13) + extension;

        Path destination = Paths.get(uploadPath + newFileName);

        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }



}
