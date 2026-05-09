package services;

import services.SERVICETache;
import models.Tache;
import org.junit.jupiter.api.*;
import java.sql.Date;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SERVICETacheTest {

    private static SERVICETache service;

    @BeforeAll
    public static void setup() {
        service = new SERVICETache();
    }

    @Test
    @Order(1)
    public void testAddTache() {
        Tache t = new Tache("Test", "Desc", Date.valueOf("2026-02-20"),
                "A_FAIRE", 1, 1, "HAUTE");
        service.addTache(t);

        List<Tache> taches = service.getAllTaches();
        Assertions.assertTrue(taches.size() > 0);
    }

    @Test
    @Order(2)
    public void testUpdateTache() {
        List<Tache> taches = service.getAllTaches();
        Tache t = taches.get(0);
        t.setStatut("TERMINEE");
        service.updateTache(t);

        Tache updated = service.getTacheById(t.getId());
        Assertions.assertEquals("TERMINEE", updated.getStatut());
    }

    @Test
    @Order(3)
    public void testDeleteTache() {
        List<Tache> taches = service.getAllTaches();
        Tache t = taches.get(0);
        service.deleteTache(t.getId());

        Assertions.assertNull(service.getTacheById(t.getId()));
    }
}
