package services;

import services.service_ressource;
import models.Ressource;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class service_ressourceTest {

    static service_ressource service;
    static int idRessourceTest;

    @BeforeAll
    static void setup() {
        service = new service_ressource();
    }

    // ---------- TEST AJOUT ----------
    @Test
    @Order(1)
    void testAjouterRessource() throws SQLException {

        Ressource r = new Ressource(1,
                "Clavier HP",
                "Matériel informatique",
                20,
                "HP Tunisie"
        );

        service.add(r);

        List<Ressource> Ressources = service.getAll();

        assertFalse(Ressources.isEmpty());

        assertTrue(
                Ressources.stream().anyMatch(res ->
                        res.getNom().equals("Clavier HP")
                )
        );
    }

    // ---------- TEST MODIFICATION ----------
    @Test
    @Order(2)
    void testModifierRessource() throws SQLException {

        Ressource r = new Ressource();
        r.setid(1);
        r.setNom("Clavier Gamer RGB");
        r.setType_ressource("Gaming");
        r.setQuatite(15);
        r.setFournisseur("MyTek");

        service.update(r);

        List<Ressource> Ressources = service.getAll();

        boolean trouve = Ressources.stream()
                .anyMatch(res ->
                        res.getNom().equals("Clavier Gamer RGB")
                );

        assertTrue(trouve);
    }

    // ---------- CLEAN DATABASE ----------
    @AfterAll
    static void cleanUp() throws SQLException {
        service.deleteAll();
    }
}
