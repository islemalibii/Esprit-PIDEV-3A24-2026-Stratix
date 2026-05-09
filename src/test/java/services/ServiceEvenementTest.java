package services;

import services.ServiceEvenemnet;
import models.Evenement;
import models.enums.EventStatus;
import models.enums.EventType;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceEvenementTest {
    static ServiceEvenemnet service;
    static int idTestEvent;
    @BeforeAll
    static void setup() {
        service = new ServiceEvenemnet();
    }


    @Test
    @Order(1)
    void testAddEvenement() {
        Evenement e = new Evenement();
        e.setType_event(EventType.formation);
        e.setStatut(EventStatus.annuler);
        e.setDate_event(LocalDate.now());
        e.setDescription("formation behya");
        e.setLieu("esprit");
        e.setTitre("testTitle");

        service.add(e);

        List<Evenement> evenements = service.getAll();

        assertFalse(evenements.isEmpty(), "Shouldn't be empty !!!");
        assertTrue(
                evenements.stream().anyMatch(ev -> ev.getTitre().equals("testTitle"))
        );

        idTestEvent = evenements.stream()
                .filter(ev -> ev.getTitre().equals("testTitle"))
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(2)
    void testUpdateEvenement() {
        Evenement e = new Evenement();
        e.setId(idTestEvent);
        e.setType_event(EventType.reunion);
        e.setDate_event(LocalDate.now().plusDays(1));
        e.setDescription("description jdida");
        e.setStatut(EventStatus.planifier);
        e.setLieu("online");
        e.setTitre("newTitle");

        service.update(e);

        List<Evenement> evenements = service.getAll();
        boolean trouve = evenements.stream()
                .anyMatch(ev -> ev.getTitre().equals("newTitle"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testArchiverEvenement() {
        service.archiver(idTestEvent);
        List<Evenement> evenements = service.getAll();

        boolean stillExists = evenements.stream()
                .anyMatch(ev -> ev.getId() == idTestEvent);

        assertFalse(stillExists, "it should be removed >:/ !!");
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        service.deleteById(idTestEvent);
        System.out.println("Testing done safely :)");
    }
}