package service.export;

import models.produit;
import models.Ressource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportExcelService {

    // ==================== EXPORT DES PRODUITS VERS EXCEL ====================

    public static void exporterProduitsVersExcel(List<produit> produits, String cheminFichier) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Produits");

        // Création de l'en-tête
        Row headerRow = sheet.createRow(0);
        String[] colonnes = {
                "ID", "Nom", "Description", "Catégorie", "Prix (DT)",
                "Stock Actuel", "Stock Minimum", "Date Création",
                "Date Fabrication", "Date Péremption", "Date Garantie",
                "Ressources Nécessaires"
        };

        // Style pour l'en-tête
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short)12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Création des cellules d'en-tête
        for (int i = 0; i < colonnes.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colonnes[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        // Style pour les données
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Style pour les dates
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        // Remplissage des données
        int rowNum = 1;
        for (produit p : produits) {
            Row row = sheet.createRow(rowNum++);

            // ID
            row.createCell(0).setCellValue(p.getId());

            // Nom
            row.createCell(1).setCellValue(p.getNom() != null ? p.getNom() : "");

            // Description
            row.createCell(2).setCellValue(p.getDescription() != null ? p.getDescription() : "");

            // Catégorie
            row.createCell(3).setCellValue(p.getCategorie() != null ? p.getCategorie() : "");

            // Prix
            row.createCell(4).setCellValue(p.getPrix());

            // Stock actuel
            row.createCell(5).setCellValue(p.getStock_actuel());

            // Stock minimum
            row.createCell(6).setCellValue(p.getStock_min());

            // Date création
            row.createCell(7).setCellValue(p.getDate_creation() != null ? p.getDate_creation() : "");

            // Date fabrication
            row.createCell(8).setCellValue(p.getDate_fabrication() != null ? p.getDate_fabrication() : "");

            // Date péremption
            row.createCell(9).setCellValue(p.getDate_peremption() != null ? p.getDate_peremption() : "");

            // Date garantie
            row.createCell(10).setCellValue(p.getDate_garantie() != null ? p.getDate_garantie() : "");

            // Ressources nécessaires
            row.createCell(11).setCellValue(p.getRessources_necessaires() != null ? p.getRessources_necessaires() : "");
        }

        // Ajuster la largeur des colonnes
        for (int i = 0; i < colonnes.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Sauvegarde du fichier
        try (FileOutputStream outputStream = new FileOutputStream(cheminFichier)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    // ==================== EXPORT DES RESSOURCES VERS EXCEL ====================

    public static void exporterRessourcesVersExcel(List<Ressource> ressources, String cheminFichier) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ressources");

        // En-tête
        Row headerRow = sheet.createRow(0);
        String[] colonnes = {"ID", "Nom", "Type", "Quantité", "Fournisseur"};

        // Style pour l'en-tête
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short)12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < colonnes.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colonnes[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        // Style pour les données
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Style pour les quantités faibles (rouge)
        CellStyle lowStockStyle = workbook.createCellStyle();
        lowStockStyle.cloneStyleFrom(dataStyle);
        lowStockStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        lowStockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Données
        int rowNum = 1;
        for (Ressource r : ressources) {
            Row row = sheet.createRow(rowNum++);

            // ID
            row.createCell(0).setCellValue(r.getid());

            // Nom
            row.createCell(1).setCellValue(r.getNom() != null ? r.getNom() : "");

            // Type
            row.createCell(2).setCellValue(r.getType_ressource() != null ? r.getType_ressource() : "");

            // Quantité (avec style spécial si < 5)
            Cell quantiteCell = row.createCell(3);
            quantiteCell.setCellValue(r.getQuatite());
            if (r.getQuatite() <= 5) {
                quantiteCell.setCellStyle(lowStockStyle);
            } else {
                quantiteCell.setCellStyle(dataStyle);
            }

            // Fournisseur
            row.createCell(4).setCellValue(r.getFournisseur() != null ? r.getFournisseur() : "");
        }

        // Ajuster la largeur des colonnes
        for (int i = 0; i < colonnes.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Sauvegarde du fichier
        try (FileOutputStream outputStream = new FileOutputStream(cheminFichier)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}