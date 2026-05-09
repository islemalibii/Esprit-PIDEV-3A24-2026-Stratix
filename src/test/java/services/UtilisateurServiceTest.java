package services;

import services.UtilisateurService;
import models.Utilisateur;
import models.Role;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilisateurServiceTest {
    static UtilisateurService service;
    static int idAdminTest;
    static int idEmployeTest;
    static int idCEOTest;

    @BeforeAll
    static void setup() {
        service = UtilisateurService.getInstance();
    }

    @Test
    @Order(1)
    void testAjouterAdmin() throws SQLException {
        Utilisateur admin = new Utilisateur("TestAdmin", "TestPrenom", "testadmin@example.com",
                "0600000000", 11111111, "testpass", Role.ADMIN);
        service.ajouter(admin);

        List<Utilisateur> admins = service.getByRole(Role.ADMIN);
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().anyMatch(a -> a.getEmail().equals("testadmin@example.com")));

        // Récupérer l'ID pour les tests suivants
        idAdminTest = admins.stream()
                .filter(a -> a.getEmail().equals("testadmin@example.com"))
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(2)
    void testAjouterEmploye() throws SQLException {
        Utilisateur employe = new Utilisateur("TestEmp", "TestPrenom", "testemp@example.com",
                "0611111111", 22222222, "testpass", Role.EMPLOYE,
                "IT", "Développeur", 3000.0, "Java, SQL");
        service.ajouter(employe);

        List<Utilisateur> employes = service.getByRole(Role.EMPLOYE);
        assertFalse(employes.isEmpty());
        assertTrue(employes.stream().anyMatch(e -> e.getEmail().equals("testemp@example.com")));

        // Récupérer l'ID pour les tests suivants
        idEmployeTest = employes.stream()
                .filter(e -> e.getEmail().equals("testemp@example.com"))
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(3)
    void testAjouterCEO() throws SQLException {
        Utilisateur ceo = new Utilisateur("TestCEO", "TestPrenom", "testceo@example.com",
                "0622222222", 33333333, "testpass", Role.CEO);
        service.ajouter(ceo);

        List<Utilisateur> ceos = service.getByRole(Role.CEO);
        assertFalse(ceos.isEmpty());
        assertTrue(ceos.stream().anyMatch(c -> c.getEmail().equals("testceo@example.com")));

        // Récupérer l'ID pour les tests suivants
        idCEOTest = ceos.stream()
                .filter(c -> c.getEmail().equals("testceo@example.com"))
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(4)
    void testGetAll() throws SQLException {
        List<Utilisateur> utilisateurs = service.getAll();
        assertFalse(utilisateurs.isEmpty());
        assertTrue(utilisateurs.size() >= 3);
    }

    @Test
    @Order(5)
    void testGetById() throws SQLException {
        Utilisateur admin = service.getById(idAdminTest);
        assertNotNull(admin);
        assertEquals("TestAdmin", admin.getNom());
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    @Order(6)
    void testGetByEmail() throws SQLException {
        Utilisateur employe = service.getByEmail("testemp@example.com");
        assertNotNull(employe);
        assertEquals("TestEmp", employe.getNom());
        assertEquals(Role.EMPLOYE, employe.getRole());
    }

    @Test
    @Order(7)
    void testGetByRole() throws SQLException {
        List<Utilisateur> admins = service.getByRole(Role.ADMIN);
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().anyMatch(a -> a.getEmail().equals("testadmin@example.com")));
    }

    @Test
    @Order(8)
    void testModifierAdmin() throws SQLException {
        Utilisateur admin = service.getById(idAdminTest);
        assertNotNull(admin);

        admin.setTel("0699999999");
        admin.setNom("AdminModifie");
        service.modifier(admin);

        Utilisateur adminModifie = service.getById(idAdminTest);
        assertEquals("0699999999", adminModifie.getTel());
        assertEquals("AdminModifie", adminModifie.getNom());
    }

    @Test
    @Order(9)
    void testModifierEmploye() throws SQLException {
        Utilisateur employe = service.getById(idEmployeTest);
        assertNotNull(employe);

        employe.setSalaire(4000.0);
        employe.setPoste("Senior Développeur");
        service.modifier(employe);

        Utilisateur employeModifie = service.getById(idEmployeTest);
        assertEquals(4000.0, employeModifie.getSalaire());
        assertEquals("Senior Développeur", employeModifie.getPoste());
    }

    @Test
    @Order(10)
    void testSupprimerCEO() throws SQLException {
        service.supprimer(idCEOTest);
        Utilisateur ceoSupprime = service.getById(idCEOTest);
        assertNull(ceoSupprime);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        // Nettoyer les données de test
        if (idAdminTest > 0) {
            service.supprimer(idAdminTest);
        }
        if (idEmployeTest > 0) {
            service.supprimer(idEmployeTest);
        }
        // idCEOTest déjà supprimé dans testSupprimerCEO
    }
}
