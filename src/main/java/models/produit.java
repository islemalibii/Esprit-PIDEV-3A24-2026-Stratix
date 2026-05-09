package models;

import java.time.LocalDate;

public class produit {
    private int id;
    private String nom;
    private String description;
    private String categorie;
    private double prix;
    private int stock_actuel;
    private int stock_min;
    private String date_creation;      // Date d'ajout dans le système
    private String ressources_necessaires;
    private String image_path;
    private String details;

    // NOUVEAUX ATTRIBUTS
    private String date_fabrication;    // Date de fabrication
    private String date_peremption;     // Date de péremption
    private String date_garantie;       // Date de fin de garantie
    private String type_produit;        // "Alimentaire", "Médicament", "Électronique", etc.

    // Constructeurs
    public produit() {}

    public produit(int id, String nom, String description, String categorie, double prix,
                   int stock_actuel, int stock_min, String date_creation,
                   String ressources_necessaires, String image_path,
                   String date_fabrication, String date_peremption,
                   String date_garantie, String type_produit,String details) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.stock_actuel = stock_actuel;
        this.stock_min = stock_min;
        this.date_creation = date_creation;
        this.ressources_necessaires = ressources_necessaires;
        this.image_path = image_path;
        this.date_fabrication = date_fabrication;
        this.date_peremption = date_peremption;
        this.date_garantie = date_garantie;
        this.type_produit = type_produit;
        this.details = details;
    }

    // Getters et Setters existants
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getStock_actuel() { return stock_actuel; }
    public void setStock_actuel(int stock_actuel) { this.stock_actuel = stock_actuel; }

    public int getStock_min() { return stock_min; }
    public void setStock_min(int stock_min) { this.stock_min = stock_min; }

    public String getDate_creation() { return date_creation; }
    public void setDate_creation(String date_creation) { this.date_creation = date_creation; }

    public String getRessources_necessaires() { return ressources_necessaires; }
    public void setRessources_necessaires(String ressources_necessaires) {
        this.ressources_necessaires = ressources_necessaires;
    }

    public String getImage_path() { return image_path; }
    public void setImage_path(String image_path) { this.image_path = image_path; }

    // NOUVEAUX Getters et Setters
    public String getDate_fabrication() { return date_fabrication; }
    public void setDate_fabrication(String date_fabrication) {
        this.date_fabrication = date_fabrication;
    }

    public String getDate_peremption() { return date_peremption; }
    public void setDate_peremption(String date_peremption) {
        this.date_peremption = date_peremption;
    }

    public String getDate_garantie() { return date_garantie; }
    public void setDate_garantie(String date_garantie) {
        this.date_garantie = date_garantie;
    }

    public String getType_produit() { return type_produit; }
    public void setType_produit(String type_produit) {
        this.type_produit = type_produit;
    }

    // Méthodes utilitaires pour vérifier l'état du produit
    public boolean estPerime() {
        if (date_peremption == null) return false;
        LocalDate datePer = LocalDate.parse(date_peremption);
        return LocalDate.now().isAfter(datePer);
    }

    public boolean estBientotPerime(int joursAlerte) {
        if (date_peremption == null) return false;
        LocalDate datePer = LocalDate.parse(date_peremption);
        long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), datePer);
        return joursRestants <= joursAlerte && joursRestants > 0;
    }

    public boolean garantieExpiree() {
        if (date_garantie == null) return false;
        LocalDate dateGar = LocalDate.parse(date_garantie);
        return LocalDate.now().isAfter(dateGar);
    }

    public String getStatutPeremption() {
        if (date_peremption == null) return "Non concerné";
        if (estPerime()) return "PÉRIMÉ";
        if (estBientotPerime(30)) return "Bientôt périmé (moins de 30 jours)";
        return "Valide";
    }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return "produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type_produit + '\'' +
                ", prix=" + prix +
                ", stock=" + stock_actuel +
                ", fabrication='" + date_fabrication + '\'' +
                ", péremption='" + date_peremption + '\'' +
                ", garantie='" + date_garantie + '\'' +
                ", Détails='" + details + '\''+
                '}';
    }
}