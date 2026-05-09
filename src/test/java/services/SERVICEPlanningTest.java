package services;

import services.SERVICEPlanning;
import models.Planning;
import org.junit.jupiter.api.*;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SERVICEPlanningTest {

    private static SERVICEPlanning service;

    @BeforeAll
    public static void setup() {
        service = new SERVICEPlanning();
    }

    @Test
    @Order(1)
    public void testAddPlanning() {
        Planning p = new Planning(1, Date.valueOf("2026-02-15"),
                Time.valueOf("09:00:00"), Time.valueOf("17:00:00"), "JOUR");
        service.addPlanning(p);
        List<Planning> plannings = service.getAllPlannings();
        Assertions.assertTrue(plannings.size() > 0, "Le planning doit être ajouté");
    }

    @Test
    @Order(2)
    public void testUpdatePlanning() {
        List<Planning> plannings = service.getAllPlannings();
        Planning p = plannings.get(0);
        p.setTypeShift("NUIT");
        service.updatePlanning(p);

        Planning updated = service.getAllPlannings().get(0);
        Assertions.assertEquals("NUIT", updated.getTypeShift());
    }

    @Test
    @Order(3)
    public void testDeletePlanning() {
        List<Planning> plannings = service.getAllPlannings();
        Planning p = plannings.get(0);
        service.deletePlanning(p.getId());

        List<Planning> afterDelete = service.getAllPlannings();
        Assertions.assertFalse(afterDelete.stream().anyMatch(pl -> pl.getId() == p.getId()));
    }

    @Test
    @Order(4)
    public void testCompterTotalEmployes() {
        int total = service.compterTotalEmployes();
        Assertions.assertTrue(total >= 0);
    }
}
