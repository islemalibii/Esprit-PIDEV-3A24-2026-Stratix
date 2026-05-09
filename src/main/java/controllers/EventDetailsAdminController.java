package controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.Evenement;
import models.EventFeedback;
import models.enums.EventStatus;
import services.ServiceEvenemnet;
import services.ServiceEventExcelExport;
import services.ServiceEventFeedback;

import java.io.IOException;
import java.util.List;

public class EventDetailsAdminController {

    @FXML private ImageView eventImageView;
    @FXML private Label titleLabel, dateLabel, locationLabel, typeLabel, descriptionLabel, statusTextLabel, statusBadge;
    @FXML private VBox feedbackContainer;
    @FXML private VBox feedbackSection;
    @FXML private WebView mapView;
    @FXML private VBox mapSection;


    private Evenement currentEvent;
    private ServiceEvenemnet service = new ServiceEvenemnet();

    public void setEventData(Evenement e) {
        this.currentEvent = e;

        titleLabel.setText(e.getTitre());
        descriptionLabel.setText(e.getDescription());
        dateLabel.setText(e.getDate_event().toString());
        locationLabel.setText(e.getLieu());

        typeLabel.setText(e.getType_event() != null ? e.getType_event().name() : "NON SPÉCIFIÉ");

        if (e.getStatut() != null) {
            String statusName = e.getStatut().name().toUpperCase();
            statusTextLabel.setText(statusName);
            statusBadge.setText(statusName);

            statusBadge.getStyleClass().removeAll("status-planifier", "status-termine", "status-annule");
            statusBadge.getStyleClass().add("status-" + e.getStatut().name().toLowerCase());
        }

        //load ratings if terminer l event
        if (e.getStatut() == EventStatus.terminer) {
            feedbackSection.setVisible(true);
            mapSection.setVisible(false);
            mapSection.setManaged(false);
            loadFeedbacks(e.getId());

        }else if (e.getStatut() == EventStatus.planifier) {
            feedbackSection.setVisible(false);
            feedbackSection.setManaged(false);

            mapSection.setVisible(true);
            mapSection.setManaged(true);
            setupMap(e);
        } else {
            mapSection.setVisible(false);
            mapSection.setManaged(false);
        }



        try {
            String imagePath = (e.getImageUrl() == null || e.getImageUrl().isEmpty())
                    ? "/images/placeholder.png" : e.getImageUrl();
            eventImageView.setImage(new Image(imagePath));

            Rectangle clip = new Rectangle(450, 350);
            clip.setArcWidth(30); clip.setArcHeight(30);
            eventImageView.setClip(clip);
        } catch (Exception ex) {
            System.out.println("Erreur image: " + ex.getMessage());
        }
    }

    private void loadFeedbacks(int eventId) {
        ServiceEventFeedback sf = new ServiceEventFeedback();
        List<EventFeedback> feedbacks = sf.getByEvent(eventId);

        System.out.println("Fetched feedbacks count: " + feedbacks.size());

        feedbackContainer.getChildren().clear();

        if (feedbacks.isEmpty()) {
            Label empty = new Label("Aucun feedback pour cet événement.");
            empty.setStyle("-fx-font-size:14px; -fx-padding:5; -fx-text-fill: black;");
            feedbackContainer.getChildren().add(empty);
            return;
        }

        for (EventFeedback f : feedbacks) {
            HBox feedbackRow = new HBox(10);
            feedbackRow.setStyle("-fx-padding: 5; -fx-alignment: center-left;");

            Label starsLabel = new Label(getStarRating(f.getRating()));
            starsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label commentLabel = new Label("- " + f.getCommentaire());
            commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

            feedbackRow.getChildren().addAll(starsLabel, commentLabel);
            feedbackContainer.getChildren().add(feedbackRow);
        }
    }

    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private void setupMap(Evenement e) {
        WebEngine engine = mapView.getEngine();
        java.net.URL url = getClass().getResource("/map.html");

        if (url == null) {
            System.err.println("map.html non trouvé!");
            return;
        }

        engine.load(url.toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String script = String.format(java.util.Locale.US, "showCoordinates(%f, %f, '%s')",
                        e.getLatitude(),
                        e.getLongitude(),
                        e.getLieu().replace("'", "\\'"));

                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(600));
                delay.setOnFinished(event -> {
                    try {
                        engine.executeScript(script);
                    } catch (Exception ex) {
                        System.err.println("Erreur Script Map: " + ex.getMessage());
                    }
                });
                delay.play();
            }
        });
    }

    @FXML
    private void handleExportParticipants() {
        if (currentEvent == null) {
            System.out.println("No event selected");
            return;
        }
        ServiceEventExcelExport service = new ServiceEventExcelExport();
        service.exportParticipantsToExcel(currentEvent.getId());

        System.out.println("Export finished!");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException ex) {
            System.err.println("Erreur de navigation : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyEvent.fxml"));
            Parent root = loader.load();

            ModifyEventController controller = loader.getController();
            controller.setEvent(currentEvent);

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            Scene scene = stage.getScene();


            scene.setRoot(root);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void handleArchive() {
        service.archiver(currentEvent.getId());
        goBack();
    }

    @FXML
    private void goBack() {
        navigateTo("/EventDashboard.fxml");
    }

    @FXML
    private void handleEvenements() {
        navigateTo("/EventDashboard.fxml");
    }

}