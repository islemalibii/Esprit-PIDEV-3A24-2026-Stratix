package services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.MyDataBase;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceEventExcelExport {

    Connection cnx;

    public ServiceEventExcelExport() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void exportParticipantsToExcel(int eventId) {
        try {

            ResultSet rs = getParticipants(eventId);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Participants");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nom");
            header.createCell(1).setCellValue("Prenom");
            header.createCell(2).setCellValue("Telephone");
            header.createCell(3).setCellValue("Email");

            int rowNum = 1;

            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(rs.getString("nom"));
                row.createCell(1).setCellValue(rs.getString("prenom"));
                row.createCell(2).setCellValue(rs.getString("tel"));
                row.createCell(3).setCellValue(rs.getString("email"));
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.setInitialFileName("participants_event_" + eventId + ".xlsx");

            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            java.io.File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                System.out.println("Excel exported successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResultSet getParticipants(int eventId) {

        ResultSet rs = null;

        try {
            String query = "SELECT u.nom, u.prenom, u.tel, u.email " +
                    "FROM participation p " +
                    "JOIN utilisateur u ON p.user_email = u.email " +
                    "WHERE p.event_id = ?";

            PreparedStatement pst = cnx.prepareStatement(query);

            pst.setInt(1, eventId);
            rs = pst.executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rs;
    }
}
