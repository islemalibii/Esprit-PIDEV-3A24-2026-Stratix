package models;

import java.util.Date;

public class Phase {
    private int id;
    private int projetId;
    private String nom;
    private String objectif;
    private Date dateDebut;
    private Date dateFin;
    private String statut; // Planifié, En cours, Terminé, Annulé
    private Double velociteEstimee;
    private Double velociteReelle;

    public Phase() {}

    public Phase(int id, int projetId, String nom, String objectif,
                 Date dateDebut, Date dateFin, String statut,
                 Double velociteEstimee, Double velociteReelle) {
        this.id = id;
        this.projetId = projetId;
        this.nom = nom;
        this.objectif = objectif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.velociteEstimee = velociteEstimee;
        this.velociteReelle = velociteReelle;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjetId() { return projetId; }
    public void setProjetId(int projetId) { this.projetId = projetId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Double getVelociteEstimee() { return velociteEstimee; }
    public void setVelociteEstimee(Double velociteEstimee) { this.velociteEstimee = velociteEstimee; }

    public Double getVelociteReelle() { return velociteReelle; }
    public void setVelociteReelle(Double velociteReelle) { this.velociteReelle = velociteReelle; }

    @Override
    public String toString() {
        return nom + " [" + statut + "]";
    }
}