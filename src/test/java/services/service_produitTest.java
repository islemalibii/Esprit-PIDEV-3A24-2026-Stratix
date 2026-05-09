package services;

import services.service_produit;
import models.produit;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class service_produitTest {
    static service_produit service;
    static int idProduitTest;

    @BeforeAll
    static void setup() {
        service = new service_produit();
    }

    @Test
    @Order(1)
    void testAjouterproduit() throws SQLException {
        // Création du produit avec le constructeur par défaut + setters
        produit p = new produit();
        p.setNom("Laptop Dell Inspiron 15");
        p.setDescription("Ordinateur portable Intel i5, 8GB RAM, SSD 512GB");
        p.setCategorie("Matériel informatique");
        p.setPrix(1850.000);
        p.setStock_actuel(30);
        p.setStock_min(10);
        p.setDate_creation("2026-03-15");
        p.setRessources_necessaires("Chargeur, carton emballage, étiquette garantie");

        // Ajout à la base de données
        service.add(p);

        // Récupération de l'ID généré
        List<produit> produits = service.getAll();
        assertFalse(produits.isEmpty());

        // Vérification que le produit a bien été ajouté
        boolean trouve = produits.stream()
                .anyMatch(prod -> "Laptop Dell Inspiron 15".equals(prod.getNom()));
        assertTrue(trouve, "Le produit devrait être présent dans la base de données");

        // Sauvegarde de l'ID pour les tests suivants
        produits.stream()
                .filter(prod -> "Laptop Dell Inspiron 15".equals(prod.getNom()))
                .findFirst()
                .ifPresent(prod -> idProduitTest = prod.getId());
    }

    @Test
    @Order(2)
    void testModifierproduit() throws SQLException {
        assertTrue(idProduitTest > 0, "L'ID du produit test devrait être disponible");

        // Récupérer le produit existant
        produit p = new produit();
        p.setId(idProduitTest);  // Utiliser l'ID réel au lieu de 1
        p.setNom("NomModifie");
        p.setDescription("DescriptionModifie");
        p.setCategorie("Catégorie Modifie");
        p.setPrix(20000);
        p.setStock_actuel(100);
        p.setStock_min(90);
        p.setDate_creation("2026-03-09");
        p.setRessources_necessaires("ressourcesnecessaireModifie");

        service.update(p);

        // Vérification
        List<produit> produits = service.getAll();
        boolean trouve = produits.stream()
                .anyMatch(per -> per.getNom().equals("NomModifie") && per.getId() == idProduitTest);
        assertTrue(trouve, "Le produit modifié devrait être présent");
    }

    //@AfterAll
    //static void cleanUp() throws SQLException {
      //  if (idProduitTest > 0) {
            // Supprimer uniquement le produit de test
        //    service.delete(idProduitTest);
       // }
        // ou tout supprimer si vous préférez
        // service.deleteAll();
  //  }
}