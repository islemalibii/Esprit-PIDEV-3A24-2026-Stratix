package service.export;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import models.produit;
import models.Ressource;

import java.awt.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportPDFService {

    // ==================== POUR LES PRODUITS ====================

    public static void exporterProduitsVersPDF(List<produit> produits, String cheminFichier) {
        try {
            // Création du document (format A4 paysage pour plus de colonnes)
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("CATALOGUE DES PRODUITS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Date
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String dateGen = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph date = new Paragraph("Généré le : " + dateGen, normalFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.add(new Paragraph(" "));

            // Statistiques
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("RÉSUMÉ", boldFont));
            document.add(new Paragraph("Total produits : " + produits.size(), normalFont));

            double valeurTotale = produits.stream()
                    .mapToDouble(p -> p.getPrix() * p.getStock_actuel())
                    .sum();
            document.add(new Paragraph("Valeur totale du stock : " + String.format("%.2f DT", valeurTotale), normalFont));

            long produitsPerimes = produits.stream()
                    .filter(produit::estPerime)
                    .count();
            document.add(new Paragraph("Produits périmés : " + produitsPerimes, normalFont));

            long stockFaible = produits.stream()
                    .filter(p -> p.getStock_actuel() <= p.getStock_min())
                    .count();
            document.add(new Paragraph("Stock faible : " + stockFaible, normalFont));

            document.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 1, 1, 1, 1, 2});

            // En-têtes
            String[] headers = {"ID", "Nom", "Catégorie", "Prix", "Stock", "Min", "Péremption", "Statut"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(new GrayColor(0.75f)); // Gris clair
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Données
            for (produit p : produits) {
                table.addCell(String.valueOf(p.getId()));
                table.addCell(p.getNom() != null ? p.getNom() : "-");
                table.addCell(p.getCategorie() != null ? p.getCategorie() : "-");
                table.addCell(String.format("%.2f DT", p.getPrix()));
                table.addCell(String.valueOf(p.getStock_actuel()));
                table.addCell(String.valueOf(p.getStock_min()));
                table.addCell(p.getDate_peremption() != null ? p.getDate_peremption() : "-");

                String statut;
                if (p.estPerime()) {
                    statut = "PÉRIMÉ";
                } else if (p.estBientotPerime(30)) {
                    statut = "Bientôt périmé";
                } else if (p.getStock_actuel() <= p.getStock_min()) {
                    statut = "Stock faible";
                } else {
                    statut = "OK";
                }
                table.addCell(statut);
            }

            document.add(table);
            document.close();
            System.out.println("PDF produit généré avec succès : " + cheminFichier);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== POUR LES RESSOURCES ====================

    public static void exporterRessourcesVersPDF(List<Ressource> ressources, String cheminFichier) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("INVENTAIRE DES RESSOURCES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String dateGen = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph date = new Paragraph("Généré le : " + dateGen, normalFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.add(new Paragraph(" "));

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("RÉSUMÉ", boldFont));
            document.add(new Paragraph("Total ressources : " + ressources.size(), normalFont));

            int quantiteTotale = ressources.stream()
                    .mapToInt(Ressource::getQuatite)
                    .sum();
            document.add(new Paragraph("Quantité totale : " + quantiteTotale, normalFont));

            long typesUniques = ressources.stream()
                    .map(Ressource::getType_ressource)
                    .distinct()
                    .count();
            document.add(new Paragraph("Types de ressources : " + typesUniques, normalFont));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 1, 2});

            String[] headers = {"ID", "Nom", "Type", "Quantité", "Fournisseur"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(new GrayColor(0.75f)); // Gris clair
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Ressource r : ressources) {
                table.addCell(String.valueOf(r.getid()));
                table.addCell(r.getNom() != null ? r.getNom() : "-");
                table.addCell(r.getType_ressource() != null ? r.getType_ressource() : "-");

                // Coloration en rouge si quantité faible (< 5)
                if (r.getQuatite() <= 5) {
                    PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(r.getQuatite())));
                    cell.setBackgroundColor(Color.RED);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                } else {
                    table.addCell(String.valueOf(r.getQuatite()));
                }

                table.addCell(r.getFournisseur() != null ? r.getFournisseur() : "-");
            }

            document.add(table);
            document.close();
            System.out.println("PDF ressource généré avec succès : " + cheminFichier);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF ressource : " + e.getMessage());
            e.printStackTrace();
        }
    }
}