package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Evenement;
import models.enums.EventStatus;
import services.ServiceEvenemnet;

import javafx.scene.image.ImageView;
import services.ServiceEventFeedback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EventDashboardController {

    @FXML private Label totalLabel;
    @FXML private Label plannedLabel;
    @FXML private Label finishedLabel;
    @FXML private Label cancelledLabel;

    @FXML private FlowPane eventContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatusCombo;
    private final String PROJECT_UPLOAD_PATH = "file:/C:/Users/islem/stratix_web/public";




    private ServiceEvenemnet service = new ServiceEvenemnet();
    private List<Evenement> events = new ArrayList<>();

    @FXML
    public void initialize() {

        filterStatusCombo.getItems().add("Tous");
        for (EventStatus s : EventStatus.values()) {
            filterStatusCombo.getItems().add(s.name());
        }
        filterStatusCombo.setValue("Tous");

        events = service.getAll();
        updateCounters();
        displayEvents(events);
        search();
    }

    private void updateCounters() {
        long planned = events.stream()
                .filter(e -> e.getStatut() == EventStatus.planifier)
                .count();
        long finished = events.stream()
                .filter(e -> e.getStatut() == EventStatus.terminer)
                .count();
        long cancelled = events.stream()
                .filter(e -> e.getStatut() == EventStatus.annuler)
                .count();

        totalLabel.setText(String.valueOf(events.size()));
        plannedLabel.setText(String.valueOf(planned));
        finishedLabel.setText(String.valueOf(finished));
        cancelledLabel.setText(String.valueOf(cancelled));
    }


    private void displayEvents(List<Evenement> list) {
        eventContainer.getChildren().clear();
        for (Evenement e : list) {
            eventContainer.getChildren().add(createEventCard(e));
        }
    }

    private VBox createEventCard(Evenement e) {
        VBox card = new VBox(12);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(300);
        card.setPadding(new Insets(0, 0, 15, 0));
        card.setAlignment(Pos.TOP_CENTER);


        ImageView imageView = new ImageView();
        try {
            String imagePath;
            if (e.getImageUrl() == null || e.getImageUrl().isEmpty()) {
                imagePath = getClass().getResource("/images/placeholder.png").toExternalForm();
            } else {

                imagePath = PROJECT_UPLOAD_PATH + e.getImageUrl();
            }

            Image img = new Image(imagePath, 300, 180, true, true);
            imageView.setImage(img);
        } catch (Exception ex) {
            System.out.println("Erreur chargement image: " + ex.getMessage());
            try {
                imageView.setImage(new Image(getClass().getResource("/images/placeholder.png").toExternalForm()));
            } catch (Exception ignored) {}
        }




        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(300, 180);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        imageView.setClip(clip);

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(0, 15, 0, 15));

        Label title = new Label(e.getTitre());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Label typeLabel = new Label(e.getType_event() != null ? e.getType_event().name() : "TYPE NON DÉFINI");
        typeLabel.getStyleClass().add("card-description");
        typeLabel.setWrapText(true);

        Label dateLieu = new Label(e.getDate_event() + " | " + e.getLieu());
        dateLieu.getStyleClass().add("card-details");

        Label status = new Label(e.getStatut().name().toUpperCase());
        status.getStyleClass().addAll("status-badge", "status-" + e.getStatut().name().toLowerCase());



        Button detailBtn = new Button("Details");
        detailBtn.getStyleClass().add("btn-archive-card");
        detailBtn.setMaxWidth(Double.MAX_VALUE);

        detailBtn.setOnAction(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetailsAdmin.fxml"));
                Node view = loader.load();

                EventDetailsAdminController controller = loader.getController();
                controller.setEventData(e);

                StackPane contentArea = (StackPane) eventContainer.getScene().lookup("#contentArea");
                contentArea.getChildren().setAll(view);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        infoBox.getChildren().addAll(title, typeLabel, dateLieu, status, detailBtn);
        card.getChildren().addAll(imageView, infoBox);

        return card;
    }

    @FXML
    private void goToAddEvent() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/addEvent.fxml"));

            Stage stage = (Stage) eventContainer.getScene().getWindow();

            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);

        } catch (IOException e) {
            System.err.println("Erreur de chargement de la page d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToArchived() {
        try {
            Node archiveView = FXMLLoader.load(getClass().getResource("/ArchivedEvents.fxml"));
            StackPane contentArea = (StackPane) eventContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(archiveView);
            } else {
                System.err.println("Erreur : contentArea non trouvé !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String query = searchField.getText();
        String selected = filterStatusCombo.getValue();

        List<Evenement> results;

        boolean hasSearch = query != null && !query.isEmpty();
        boolean hasFilter = selected != null && !selected.equals("Tous");

        if (hasSearch && hasFilter) {
            EventStatus status = EventStatus.valueOf(selected);
            results = service.searchByTitle(query).stream()
                    .filter(e -> e.getStatut() == status)
                    .toList();
        }
        else if (hasSearch) {
            results = service.searchByTitle(query);
        }

        else if (hasFilter) {
            EventStatus status = EventStatus.valueOf(selected);
            results = service.filterByStatus(status);
        }
        else {
            results = service.getAll();
        }
        displayEvents(results);
    }


    @FXML
    private void search() {
        applyFilters();
    }



    @FXML
    private void handleFilter() {
        applyFilters();
    }




    // teb3a api qr code bch l sheet tt3aba f database
    @FXML
    private void syncWithGoogle() {
        String csvUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQ_V85FbQ-ggyuPiPlHHxHc9EHt85oY8LFpcL2VI_tS46Z1EmYGePe0kAk3ky_27zC4c8U_Nfgvs9-n/pub?output=csv";

        ServiceEventFeedback serviceFeedback = new ServiceEventFeedback();
        int newItems = 0;

        try (Scanner scanner = new Scanner(new URL(csvUrl).openStream(), "UTF-8")) {
            if (scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println("Ligne lue : " + line);
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 4) {
                    int rating = Integer.parseInt(data[1].replaceAll("\"", ""));
                    String comment = data[2].replaceAll("\"", "");
                    int eventId = Integer.parseInt(data[3].replaceAll("\"", ""));
                    if (!serviceFeedback.exists(eventId, comment)) {
                        models.EventFeedback fb = new models.EventFeedback();
                        fb.setEvenementId(eventId);
                        fb.setRating(rating);
                        fb.setCommentaire(comment);
                        fb.setDateFeedback(java.time.LocalDate.now());
                        serviceFeedback.add(fb);
                        newItems++;
                    }
                }
            }
            System.out.println("Synchronisation reussie : " + newItems + " nouveaux feedbacks ajoutes");
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

}