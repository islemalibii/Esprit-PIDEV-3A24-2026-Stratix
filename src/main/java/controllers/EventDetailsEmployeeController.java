package controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import models.Evenement;
import models.enums.EventStatus;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import services.EventEmailApi;
import services.ServiceEventParticipation;

import java.io.IOException;

public class EventDetailsEmployeeController {

    @FXML private ImageView eventImageView;
    @FXML private Label titleLabel, dateLabel, locationLabel, typeLabel, descriptionLabel;
    @FXML private ImageView qrCodeImageView;
    @FXML private VBox qrCodeContainer;
    @FXML private Button participateBtn;
    @FXML private WebView mapView;
    private Evenement currentEvent;

    public void initialize() {
        mapView.setContextMenuEnabled(false);
    }



    public void setEventData(Evenement e) {
        this.currentEvent = e;
        titleLabel.setText(e.getTitre());
        descriptionLabel.setText(e.getDescription());
        dateLabel.setText(e.getDate_event().toString());
        locationLabel.setText(e.getLieu());

        if (e.getType_event() != null) {
            typeLabel.setText(e.getType_event().name());
        } else {
            typeLabel.setText("Non spécifié");
        }


        if (e.getStatut() == EventStatus.terminer) {
            qrCodeContainer.setVisible(true);
            qrCodeContainer.setManaged(true);
            setupQRCode(e);
            participateBtn.setVisible(false);
            participateBtn.setManaged(false);
            mapView.setVisible(false);
            mapView.setManaged(false);
        }
        else if (e.getStatut() == EventStatus.planifier) {

            qrCodeContainer.setVisible(false);
            qrCodeContainer.setManaged(false);
            participateBtn.setVisible(true);
            participateBtn.setManaged(true);

            mapView.setVisible(true);
            mapView.setManaged(true);
            setupMap(e);


            participateBtn.setText("Participer");
            participateBtn.setDisable(false);
            participateBtn.setOnAction(event -> {
                String userEmail = utils.SessionManager.getInstance().getEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    System.err.println("Erreur: Aucun email trouvé dans la session.");
                    participateBtn.setText("Session Error");
                    return;
                }
                participateBtn.setText("Envoi en cours...");
                participateBtn.setDisable(true);

                new Thread(() -> {
                    try {
                        ServiceEventParticipation partService = new ServiceEventParticipation();

                        if (partService.alreadyParticipated(e.getId(), userEmail)) {
                            javafx.application.Platform.runLater(() -> {
                                participateBtn.setText("Déjà inscrit");
                                participateBtn.setDisable(true);
                            });
                            return;
                        }

                        partService.addParticipation(e.getId(), userEmail);
                        EventEmailApi.sendEmail(userEmail, e.getTitre(), e.getDate_event().toString());

                        javafx.application.Platform.runLater(() -> {
                            participateBtn.setText("Inscrit");
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            participateBtn.setText("Erreur");
                            participateBtn.setDisable(false);
                        });
                    }
                }).start();
            });


        }


        try {
            String imagePath = (e.getImageUrl() == null || e.getImageUrl().isEmpty())
                    ? "/images/placeholder.png" : e.getImageUrl();
            Image img = new Image(imagePath);
            eventImageView.setImage(img);

            Rectangle clip = new Rectangle(450, 350);
            clip.setArcWidth(30);
            clip.setArcHeight(30);
            eventImageView.setClip(clip);
        } catch (Exception ex) {
            System.out.println("Image non trouvée: " + ex.getMessage());
        }
    }

    public void setupQRCode(Evenement e) {
        try {
            String baseUrl = "https://docs.google.com/forms/d/e/1FAIpQLSdyhSYlc-lUcxNWibtq93mYXuAggpd-ZjwN2q2Y9hvc9NiPpw/viewform?usp=pp_url&entry.826892988=";
            String fullUrl = baseUrl + e.getId();
            String encodedUrl = java.net.URLEncoder.encode(fullUrl, java.nio.charset.StandardCharsets.UTF_8.toString());
            Image qrImage = new Image("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedUrl);
            qrCodeImageView.setImage(qrImage);

        } catch (Exception ex) {
            System.out.println("Erreur QR Code : " + ex.getMessage());
        }
    }


    private void setupMap(Evenement e) {
        if (e.getLatitude() == 0 && e.getLongitude() == 0) {
            System.err.println("Warning: Event coordinates are 0.0. Map might not show correctly.");
        }

        WebEngine engine = mapView.getEngine();
        java.net.URL url = getClass().getResource("/map.html");
        if (url == null) return;

        engine.load(url.toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String script = String.format(java.util.Locale.US, "showCoordinates(%f, %f, '%s')",
                        e.getLatitude(), e.getLongitude(), e.getLieu().replace("'", "\\'"));

                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
                delay.setOnFinished(ev -> {
                    engine.executeScript(script);
                });
                delay.play();
            }
        });
    }

    @FXML
    private void goBack() {
        navigateBack();
    }

    @FXML
    private void handleEvenements() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventEmployeeDashboard.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                titleLabel.getScene().setRoot(root);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}