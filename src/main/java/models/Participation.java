package models;

public class Participation {
    private int id;
    private int eventId;
    private String userEmail;

    public Participation() {
    }

    public Participation(int id, int eventId, String userEmail) {
        this.id = id;
        this.eventId = eventId;
        this.userEmail = userEmail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
