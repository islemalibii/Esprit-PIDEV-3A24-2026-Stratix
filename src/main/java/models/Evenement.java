package models;

import models.enums.EventStatus;
import models.enums.EventType;

import java.time.LocalDate;
import java.util.*;

public class Evenement {
    private int id;
    private String titre, description, lieu;
    private LocalDate date_event;
    private EventType type_event;
    private EventStatus statut;
    private List<Ressource> Ressources = new ArrayList<>();
    private boolean isArchived;
    private String image_url;
    private double latitude;
    private double longitude;

    public Evenement() {}

    public Evenement(int id, String titre, String description, EventType type_event, EventStatus statut, String lieu, LocalDate date_event) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type_event = type_event;
        this.statut = statut;
        this.lieu = lieu;
        this.date_event = date_event;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getType_event() {
        return type_event;
    }

    public void setType_event(EventType type_event) {
        this.type_event = type_event;
    }

    public EventStatus getStatut() {
        return statut;
    }

    public void setStatut(EventStatus statut) {
        this.statut = statut;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public LocalDate getDate_event() {
        return date_event;
    }

    public void setDate_event(LocalDate date_event) {
        this.date_event = date_event;
    }

    public List<Ressource> getRessources() {
        return Ressources;
    }

    public void setRessources(List<Ressource> Ressources) {
        this.Ressources = Ressources;
    }

    public boolean isArchived() {
        return isArchived;
    }
    public void setArchived(boolean archived) {
        isArchived = archived;
    }
    public String getImageUrl() {
        return image_url;
    }
    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }


    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "evenement{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type_event='" + type_event + '\'' +
                ", statut='" + statut + '\'' +
                ", lieu='" + lieu + '\'' +
                ", date_event=" + date_event +
                '}';
    }
}
