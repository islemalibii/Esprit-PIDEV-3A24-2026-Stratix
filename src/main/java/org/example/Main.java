package org.example;

import models.*;
import models.enums.EventStatus;
import models.enums.EventType;
import services.*;
import services.service_ressource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Services
        ProjetService projetService = new ProjetService();
        ServiceService serviceManager = null;
        ServiceEvenemnet evenementManager = null;
        service_ressource sr = new service_ressource();

        try {
            // 2. Establishing Connections
            serviceManager = new services.ServiceService();
            evenementManager = new services.ServiceEvenemnet();
            System.out.println("✅ Connexion établie via Singleton");

            // --- SECTION 1: ÉVÉNEMENTS ---
            System.out.println("\n--- 📅 Tests Événements ---");
            Evenement e1 = new Evenement(1, "Jaw", "barchajaw", EventType.formation, EventStatus.annuler, "jarda", LocalDate.of(2026, 2, 16));
            Evenement e2 = new Evenement(2, "7aflaa", "jawbdhawedds", EventType.recrutement, EventStatus.planifier, "salleM002", LocalDate.of(2026, 2, 16));

            // evenementManager.add(e1);
            evenementManager.update(e2);

            System.out.println("Événements actifs : " + evenementManager.getAll());
            int idEventToArchive = 1;
            evenementManager.archiver(idEventToArchive);
            System.out.println("Événement ID " + idEventToArchive + " archivé.");
            System.out.println("Événements archivés : " + evenementManager.getAllArchieved());

            // --- SECTION 2: RESSOURCES ---
            System.out.println("\n--- 📦 Tests Ressources ---");
            Ressource r = new Ressource(1, "Ordinateur", "Materiel", 10, "HP");
            Ressource r4 = new Ressource(2, "tables", "Materiel", 100, "f2");

            sr.add(r);
            sr.add(r4);

            // Coherent Loop: Now uses 'rs' to display all resources in the list
            List<Ressource> resourceList = sr.getAll();
            System.out.println("Liste des ressources enregistrées :");
            for (Ressource rs : resourceList) {
                System.out.println(rs.getid() + " | " + rs.getNom() + " | " + rs.getType_ressource() + " | " + rs.getQuatite() + " | " + rs.getFournisseur());
            }

            // Resource Update/Delete tests
            Ressource r1_update = new Ressource(1, "Ordinateur Portable", "Materiel Informatique", 20, "Dell");
            sr.update(r1_update);
            System.out.println("Ressource ID 1 mise à jour.");

            r.setid(1); // Setting ID for deletion
            sr.delete(r);
            System.out.println("Ressource ID 1 supprimée.");

            // --- SECTION 3: PROJETS ---
            System.out.println("\n--- 🏗️ Tests Projets ---");
            System.out.println("Liste des projets disponibles:");

            // ✅ CORRECTION 1: getAllProjets() au lieu de listerTousLesProjets()


            // --- SECTION 4: SERVICES ---
            System.out.println("\n--- 🛠️ Tests Services ---");
            Service s1 = new Service(0, "Developpement Web", "Creation site web", "2026-02-08", "2026-03-01", "2026-06-30", 101, 5000.00, 1, false);
            Service s2 = new Service(0, "Formation Java", "Cours programmation Java", "2026-02-08", "2026-02-15", "2026-04-15", 102, 3000.00, 2, false);
            Service s3 = new Service(0, "Maintenance", "Maintenance serveurs", "2026-02-08", "2026-02-10", "2026-12-31", 103, 2000.00, 3, false);

            serviceManager.ajouter(s1);
            serviceManager.ajouter(s2);
            serviceManager.ajouter(s3);
            System.out.println("3 services ajoutés.");

            List<Service> services = serviceManager.afficherAll();
            if (!services.isEmpty()) {
                Service sModif = services.get(0);
                sModif.setTitre("Developpement Web Avancé");
                sModif.setBudget(5500.00);
                serviceManager.updateTitre(sModif);
                System.out.println("Service ID " + sModif.getId() + " mis à jour.");
            }

            if (services.size() >= 3) {
                int dernierId = services.get(services.size() - 1).getId();
                serviceManager.archiver(dernierId);
                System.out.println("Service ID " + dernierId + " archivé.");
            }

            // --- FINAL SUMMARY ---
            System.out.println("\n--- 📊 Résultat Final ---");
            System.out.println("Services actifs : " + serviceManager.afficherAll().size());
            System.out.println("Services archivés : " + serviceManager.afficherArchives().size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n🏁 Tests terminés.");
        }
    }
}