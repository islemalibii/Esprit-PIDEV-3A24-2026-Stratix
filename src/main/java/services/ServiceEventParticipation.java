package services;

import models.Participation;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceEventParticipation {
    Connection cnx ;
    public ServiceEventParticipation() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void addParticipation(int eventId, String email) {
        String sql = "INSERT INTO participation(event_id, user_email) VALUES (?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, eventId);
            pst.setString(2, email);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean alreadyParticipated(int eventId, String email) {
        String sql = "SELECT COUNT(*) FROM participation WHERE event_id=? AND user_email=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, eventId);
            pst.setString(2, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
