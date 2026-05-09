package services;

import services.CategorieServiceService;
import models.CategorieService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategorieServiceTest {

    static CategorieServiceService service;
    static int idCategorieTest;

    @BeforeAll
    static void setup() {
        try {
            service = new CategorieServiceService();
            System.out.println("Connexion établie pour les tests de catégories");
        } catch (Exception e) {
            fail("Erreur de connexion: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testAjouterCategorie() throws SQLException {
        CategorieService c = new CategorieService();
        c.setNom("Test Catégorie");
        c.setDescription("Description de test");

        service.ajouter(c);

        List<CategorieService> categories = service.afficherAll();
        assertFalse(categories.isEmpty(), "La liste des catégories ne doit pas être vide");

        boolean trouve = categories.stream()
                .anyMatch(cat -> cat.getNom().equals("Test Catégorie"));
        assertTrue(trouve, "La catégorie de test doit être trouvée");

        for (CategorieService cat : categories) {
            if (cat.getNom().equals("Test Catégorie")) {
                idCategorieTest = cat.getId();
                break;
            }
        }
        assertTrue(idCategorieTest > 0, "L'ID de la catégorie doit être récupéré");

        System.out.println("Catégorie ajoutée avec ID: " + idCategorieTest);
    }

    @Test
    @Order(2)
    void testGetById() throws SQLException {
        assertTrue(idCategorieTest > 0, "L'ID de la catégorie doit être valide");

        CategorieService c = service.getById(idCategorieTest);
        assertNotNull(c, "La catégorie doit exister");
        assertEquals("Test Catégorie", c.getNom(), "Le nom doit correspondre");
        assertEquals("Description de test", c.getDescription(), "La description doit correspondre");

        System.out.println("Catégorie récupérée: " + c.getNom());
    }

    @Test
    @Order(3)
    void testModifierCategorie() throws SQLException {
        assertTrue(idCategorieTest > 0, "L'ID de la catégorie doit être valide");

        CategorieService c = service.getById(idCategorieTest);
        assertNotNull(c, "La catégorie doit exister");

        c.setNom("Catégorie Modifiée");
        c.setDescription("Description modifiée");

        service.modifier(c);

        CategorieService modifiee = service.getById(idCategorieTest);
        assertEquals("Catégorie Modifiée", modifiee.getNom(), "Le nom doit être modifié");
        assertEquals("Description modifiée", modifiee.getDescription(), "La description doit être modifiée");

        System.out.println("Catégorie modifiée: " + modifiee.getNom());
    }

    @Test
    @Order(4)
    void testAfficherAll() throws SQLException {
        List<CategorieService> categories = service.afficherAll();
        assertNotNull(categories, "La liste ne doit pas être null");
        assertTrue(categories.size() >= 1, "La liste doit contenir au moins 1 catégorie");

        System.out.println("Nombre de catégories: " + categories.size());
        for (CategorieService c : categories) {
            System.out.println(" - " + c.getNom() + " (ID: " + c.getId() + ")");
        }
    }

    //@Test
    //@Order(5)
    //void testSupprimerCategorie() throws SQLException {
    //   assertTrue(idCategorieTest > 0, "L'ID de la catégorie doit être valide");

    //    CategorieService avant = service.getById(idCategorieTest);
    //   assertNotNull(avant, "La catégorie doit exister avant suppression");
//
    //   service.delete(idCategorieTest);

    //   CategorieService apres = service.getById(idCategorieTest);
    //   assertNull(apres, "La catégorie ne doit plus exister après suppression");

    //   System.out.println("Catégorie ID " + idCategorieTest + " supprimée avec succès");
    //}

    //@AfterAll
    //static void cleanUp() {
      //  try {
        //    service.close();
          //  System.out.println("Nettoyage terminé");
        //} catch (SQLException e) {
          //  System.err.println("Erreur lors du nettoyage: " + e.getMessage());
        //}
    //}
}