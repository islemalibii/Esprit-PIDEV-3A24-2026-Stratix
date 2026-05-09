package services;

import services.CategorieServiceService;
import services.ServiceService;
import models.Service;
import models.CategorieService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceServiceTest {

    static ServiceService service;
    static CategorieServiceService categorieService;
    static int idServiceTest;
    static int idCategorieTest;

    @BeforeAll
    static void setup() throws SQLException {
        service = new ServiceService();
        categorieService = new CategorieServiceService();

        //CategorieService cat = new CategorieService(0, "Test Cat", "Description test", null);
        //categorieService.ajouter(cat);

        List<CategorieService> categories = categorieService.afficherAll();
        for (CategorieService c : categories) {
            if (c.getNom().equals("Test Cat")) {
                idCategorieTest = c.getId();
                break;
            }
        }
    }

    @Test
    @Order(1)
    void testAjouterService() throws SQLException {
        //Service s = new Service(0, "Test Service", "Description de test",
                //"2026-02-15", "2026-03-01", "2026-06-30",
                //999, 9999.99, idCategorieTest);

        //service.ajouter(s);

        List<Service> services = service.afficherAll();
        assertFalse(services.isEmpty());

        boolean trouve = services.stream()
                .anyMatch(serv -> serv.getTitre().equals("Test Service"));
        assertTrue(trouve);

        for (Service serv : services) {
            if (serv.getTitre().equals("Test Service")) {
                idServiceTest = serv.getId();
                break;
            }
        }
        assertTrue(idServiceTest > 0);
    }

    @Test
    @Order(2)
    void testGetById() throws SQLException {
        Service s = service.getById(idServiceTest);
        assertNotNull(s);
        assertEquals("Test Service", s.getTitre());
        assertNotNull(s.getCategorie());
        assertEquals("Test Cat", s.getCategorie().getNom());
    }

    @Test
    @Order(3)
    void testModifierService() throws SQLException {
        Service s = service.getById(idServiceTest);
        s.setTitre("Service Modifié");
        s.setBudget(15000.00);

        service.updateTitre(s);

        Service modifie = service.getById(idServiceTest);
        assertEquals("Service Modifié", modifie.getTitre());
        assertEquals(15000.00, modifie.getBudget(), 0.001);
    }

    @Test
    @Order(4)
    void testAfficherAll() throws SQLException {
        List<Service> services = service.afficherAll();
        assertNotNull(services);
        assertTrue(services.size() >= 1);
    }

    //@Test
    //@Order(5)
    //void testSupprimerService() throws SQLException {
    //    service.delete(idServiceTest);
    //    Service s = service.getById(idServiceTest);
    //    assertNull(s);
    //}

    //@AfterAll
    //static void cleanUp() throws SQLException {
    //    try {
    //        categorieService.delete(idCategorieTest);
    //    } catch (SQLException e) {
    //    }

    //    service.close();
    //   categorieService.close();
    //}
}