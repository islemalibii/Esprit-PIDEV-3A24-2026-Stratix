package services;

import models.Role;
import models.Utilisateur;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbotService {
    
    private static ChatbotService instance;
    private UtilisateurService utilisateurService;
    
    private ChatbotService() {
        utilisateurService = UtilisateurService.getInstance();
    }
    
    public static ChatbotService getInstance() {
        if (instance == null) {
            instance = new ChatbotService();
        }
        return instance;
    }
    
    public String processQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Posez-moi une question! 😊";
        }
        
        String q = normalizeText(question);
        
        // Questions d'aide
        if (matchesAny(q, "aide", "help", "commande", "que peux tu", "quoi faire") || q.equals("?")) {
            return getHelp();
        }
        
        // Recherche d'utilisateur par nom
        if (matchesAny(q, "trouve", "cherche", "recherche") || 
            matchesAny(q, "info sur", "information sur")) {
            return searchUser(question);
        }
        
        // Questions statistiques - Employés
        if ((matchesAny(q, "combien", "nombre", "total") && matchesAny(q, "employe", "employee")) ||
            matchesAll(q, new String[]{"combien", "employe"}) || 
            matchesAll(q, new String[]{"nombre", "employe"}) ||
            matchesAll(q, new String[]{"total", "employe"})) {
            return getEmployeesCount();
        }
        
        // Questions statistiques - Admins
        if ((matchesAny(q, "combien", "nombre", "total") && matchesAny(q, "admin", "administrateur")) ||
            matchesAll(q, new String[]{"combien", "admin"}) || 
            matchesAll(q, new String[]{"nombre", "admin"})) {
            return getAdminsCount();
        }
        
        // Questions statistiques - Inactifs
        if ((matchesAny(q, "combien", "nombre", "total") && matchesAny(q, "inactif", "desactive", "inactiv")) || 
            (matchesAny(q, "combien", "nombre") && matchesAny(q, "pas actif", "non actif"))) {
            return getInactiveUsersCount();
        }
        
        // Questions d'identification - Responsable RH
        if ((matchesAny(q, "qui", "quel") && matchesAny(q, "rh", "ressource humaine")) ||
            matchesAny(q, "responsable rh", "resp rh")) {
            return getResponsablesRH();
        }
        
        // Questions d'identification - CEO
        if ((matchesAny(q, "qui", "quel") && matchesAny(q, "ceo", "directeur", "patron", "chef")) ||
            matchesAny(q, "directeur general")) {
            return getCEOs();
        }
        
        // Questions d'identification - Liste admins
        if (matchesAny(q, "liste", "affiche", "montre", "voir") && matchesAny(q, "admin", "administrateur")) {
            return getAdminsList();
        }
        
        // Questions d'identification - Liste employés
        if (matchesAny(q, "liste", "affiche", "montre", "voir") && matchesAny(q, "employe", "employee", "salarie")) {
            return getEmployeesList();
        }
        
        // Questions statistiques - Total utilisateurs
        if ((matchesAny(q, "combien", "nombre", "total") && matchesAny(q, "utilisateur", "user", "personne")) ||
            matchesAny(q, "statistique", "stat")) {
            return getTotalUsersCount();
        }
        
        // Question non reconnue
        return "Je n'ai pas compris votre question. 🤔\n\n" +
               "Essayez:\n" +
               "• 'Combien d'employés?'\n" +
               "• 'Qui est responsable RH?'\n" +
               "• 'Liste des admins'\n" +
               "• Tapez 'aide' pour plus d'options";
    }
    
    // Normalise le texte pour la comparaison
    private String normalizeText(String text) {
        return text.toLowerCase()
                   .trim()
                   .replaceAll("[éèêë]", "e")
                   .replaceAll("[àâä]", "a")
                   .replaceAll("[ùûü]", "u")
                   .replaceAll("[ïî]", "i")
                   .replaceAll("[ôö]", "o")
                   .replaceAll("[ç]", "c")
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .replaceAll("\\s+", " ");
    }
    
    // Vérifie si le texte contient au moins un des mots
    private boolean matchesAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(normalizeText(keyword))) {
                return true;
            }
        }
        return false;
    }
    
    // Vérifie si le texte contient tous les mots
    private boolean matchesAll(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (!text.contains(normalizeText(keyword))) {
                return false;
            }
        }
        return true;
    }
    
    // Recherche un utilisateur par nom
    private String searchUser(String question) {
        try {
            String[] words = question.split("\\s+");
            StringBuilder searchName = new StringBuilder();
            boolean foundKeyword = false;
            
            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase();
                
                if (word.matches("(trouve|cherche|recherche|info|information)")) {
                    foundKeyword = true;
                    continue;
                }
                
                if (word.matches("(sur|utilisateur|user)")) {
                    continue;
                }
                
                if (foundKeyword && !word.isEmpty()) {
                    if (searchName.length() > 0) {
                        searchName.append(" ");
                    }
                    searchName.append(words[i]);
                }
            }
            
            if (searchName.length() == 0 && words.length > 0) {
                searchName.append(words[words.length - 1]);
            }
            
            String finalSearchName = searchName.toString().trim();
            
            if (finalSearchName.isEmpty()) {
                return "❌ Veuillez préciser le nom de l'utilisateur.\nExemple: 'Info sur Taha'";
            }
            
            List<Utilisateur> users = utilisateurService.getAll();
            String searchLower = finalSearchName.toLowerCase();
            
            List<Utilisateur> found = users.stream()
                .filter(u -> u.getNom().toLowerCase().contains(searchLower) || 
                           u.getPrenom().toLowerCase().contains(searchLower) ||
                           (u.getNom() + " " + u.getPrenom()).toLowerCase().contains(searchLower))
                .limit(3)
                .collect(Collectors.toList());
            
            if (found.isEmpty()) {
                return "❌ Aucun utilisateur trouvé avec le nom '" + finalSearchName + "'";
            }
            
            StringBuilder sb = new StringBuilder("🔍 Résultats de recherche:\n\n");
            for (Utilisateur u : found) {
                sb.append(String.format("👤 %s %s\n", u.getNom(), u.getPrenom()));
                sb.append(String.format("   📧 %s\n", u.getEmail()));
                sb.append(String.format("   📱 %s\n", u.getTel()));
                sb.append(String.format("   👔 %s\n", getRoleDisplay(u.getRole())));
                sb.append(String.format("   📊 %s\n\n", u.isActif() ? "Actif" : "Inactif"));
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erreur lors de la recherche.";
        }
    }
    
    private String getRoleDisplay(Role role) {
        switch (role) {
            case ADMIN: return "Administrateur";
            case CEO: return "CEO";
            case EMPLOYE: return "Employé";
            case RESPONSABLE_RH: return "Responsable RH";
            case RESPONSABLE_PROJET: return "Responsable Projet";
            case RESPONSABLE_PRODUCTION: return "Responsable Production";
            default: return role.name();
        }
    }
    
    private String getHelp() {
        return "🤖 Je peux répondre à ces questions:\n\n" +
               "📊 Statistiques:\n" +
               "• Combien d'employés?\n" +
               "• Combien d'admins?\n" +
               "• Nombre d'utilisateurs?\n" +
               "• Combien d'inactifs?\n\n" +
               "👥 Identification:\n" +
               "• Qui est responsable RH?\n" +
               "• Qui est CEO?\n" +
               "• Liste des admins\n" +
               "• Liste des employés\n\n" +
               "🔍 Recherche:\n" +
               "• Trouve utilisateur [nom]\n" +
               "• Info sur [nom]";
    }
    
    private String getEmployeesCount() {
        try {
            List<Utilisateur> employes = utilisateurService.getByRole(Role.EMPLOYE);
            long actifs = employes.stream().filter(Utilisateur::isActif).count();
            return String.format("📊 Il y a %d employés actifs sur %d au total.", actifs, employes.size());
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getAdminsCount() {
        try {
            List<Utilisateur> admins = utilisateurService.getByRole(Role.ADMIN);
            long actifs = admins.stream().filter(Utilisateur::isActif).count();
            return String.format("🔐 Il y a %d administrateurs actifs.", actifs);
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getTotalUsersCount() {
        try {
            List<Utilisateur> users = utilisateurService.getAll();
            long actifs = users.stream().filter(Utilisateur::isActif).count();
            return String.format("👥 Il y a %d utilisateurs au total (%d actifs, %d inactifs).", 
                               users.size(), actifs, users.size() - actifs);
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getInactiveUsersCount() {
        try {
            List<Utilisateur> users = utilisateurService.getAll();
            long inactifs = users.stream().filter(u -> !u.isActif()).count();
            return String.format("💤 Il y a %d utilisateurs inactifs.", inactifs);
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getResponsablesRH() {
        try {
            List<Utilisateur> responsables = utilisateurService.getByRole(Role.RESPONSABLE_RH);
            if (responsables.isEmpty()) {
                return "❌ Aucun responsable RH trouvé.";
            }
            StringBuilder sb = new StringBuilder("👔 Responsables RH:\n\n");
            for (Utilisateur u : responsables) {
                if (u.isActif()) {
                    sb.append(String.format("• %s %s\n  📧 %s\n  📱 %s\n\n", 
                                          u.getNom(), u.getPrenom(), u.getEmail(), u.getTel()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getCEOs() {
        try {
            List<Utilisateur> ceos = utilisateurService.getByRole(Role.CEO);
            if (ceos.isEmpty()) {
                return "❌ Aucun CEO trouvé.";
            }
            StringBuilder sb = new StringBuilder("⭐ CEOs:\n\n");
            for (Utilisateur u : ceos) {
                if (u.isActif()) {
                    sb.append(String.format("• %s %s\n  📧 %s\n  📱 %s\n\n", 
                                          u.getNom(), u.getPrenom(), u.getEmail(), u.getTel()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getAdminsList() {
        try {
            List<Utilisateur> admins = utilisateurService.getByRole(Role.ADMIN);
            if (admins.isEmpty()) {
                return "❌ Aucun administrateur trouvé.";
            }
            StringBuilder sb = new StringBuilder("🔐 Liste des administrateurs:\n\n");
            int i = 1;
            for (Utilisateur u : admins) {
                if (u.isActif()) {
                    sb.append(String.format("%d. %s %s\n   📧 %s\n\n", 
                                          i++, u.getNom(), u.getPrenom(), u.getEmail()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
    
    private String getEmployeesList() {
        try {
            List<Utilisateur> employes = utilisateurService.getByRole(Role.EMPLOYE)
                    .stream()
                    .filter(Utilisateur::isActif)
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (employes.isEmpty()) {
                return "❌ Aucun employé trouvé.";
            }
            
            StringBuilder sb = new StringBuilder("💼 Liste des employés (10 premiers):\n\n");
            int i = 1;
            for (Utilisateur u : employes) {
                sb.append(String.format("%d. %s %s - %s\n", 
                                      i++, u.getNom(), u.getPrenom(), u.getEmail()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erreur lors de la récupération des données.";
        }
    }
}
