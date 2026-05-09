package models;

import java.sql.Date;

public class Tache {

    private int id;
    private String titre;
    private String description;
    private Date deadline;
    private String statut;
    private int employeId;
    private int projetId;
    private String priorite;

    public Tache() {}

    public Tache(String titre, String description, Date deadline,
                 String statut, int employeId, int projetId, String priorite) {
        this.titre = titre;
        this.description = description;
        this.deadline = deadline;
        this.statut = statut;
        this.employeId = employeId;
        this.projetId = projetId;
        this.priorite = priorite;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getEmployeId() { return employeId; }
    public void setEmployeId(int employeId) { this.employeId = employeId; }

    public int getProjetId() { return projetId; }
    public void setProjetId(int projetId) { this.projetId = projetId; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
}
