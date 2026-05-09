package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Evenement;
import models.enums.EventStatus;
import models.enums.EventType;
import services.EventEmailApi;
import services.ServiceEvenemnet;
import services.ServiceEventParticipation;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventEmployeeController {


    @FXML private FlowPane eventContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterCombo;


    private ServiceEvenemnet service = new ServiceEvenemnet();
    private List<Evenement> events = new ArrayList<>();

    @FXML
    public void initialize() {


        typeFilterCombo.getItems().add("Tous");
        for (EventType t : EventType.values()) {
            typeFilterCombo.getItems().add(t.name());
        }
        typeFilterCombo.setValue("Tous");

        events = service.getVisibleEventsForEmployee();
        displayEvents(events);
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
            String imagePath = (e.getImageUrl() == null || e.getImageUrl().isEmpty())
                    ? "/images/placeholder.png" : e.getImageUrl();
            Image img = new Image(imagePath, 300, 180, true, true);
            imageView.setImage(img);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        imageView.setFitWidth(300);
        imageView.setFitHeight(180);

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

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);


        Button moreBtn = new Button("Voir plus");
        moreBtn.getStyleClass().add("btn-archive-card");

        moreBtn.setOnAction(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetailsEmployee.fxml"));
                Parent root = loader.load();

                EventDetailsEmployeeController controller = loader.getController();
                controller.setEventData(e);
                StackPane contentArea = (StackPane) eventContainer.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(root);
                } else {
                    eventContainer.getScene().setRoot(root);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        if (e.getStatut() == EventStatus.planifier) {
            Button participeBtn = new Button("Participer");
            participeBtn.getStyleClass().add("btn-modify-card");

            participeBtn.setOnAction(event -> {
                String userEmail = utils.SessionManager.getInstance().getEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    System.err.println("Erreur: Aucun email trouvé dans la session.");
                    participeBtn.setText("Session Error ");
                    return;
                }
                participeBtn.setText("Envoi en cours...");
                participeBtn.setDisable(true);
                new Thread(() -> {
                    try {
                        ServiceEventParticipation partService = new ServiceEventParticipation();

                        if (partService.alreadyParticipated(e.getId(), userEmail)) {
                            javafx.application.Platform.runLater(() -> {
                                participeBtn.setText("Déjà inscrit");
                                participeBtn.setDisable(true);
                            });
                            return;
                        }

                        partService.addParticipation(e.getId(), userEmail);
                        EventEmailApi.sendEmail(userEmail, e.getTitre(), e.getDate_event().toString());
                        javafx.application.Platform.runLater(() -> {
                            participeBtn.setText("Inscrit ");
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            participeBtn.setText("Erreur ");
                            participeBtn.setDisable(false);
                        });
                    }
                }).start();
            });

            actions.getChildren().add(participeBtn);

        } else if (e.getStatut() == EventStatus.terminer) {
            Label feedbackLabel = new Label("Donner feedback");
            feedbackLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            actions.getChildren().add(feedbackLabel);
        }

        actions.getChildren().addAll(moreBtn);
        infoBox.getChildren().addAll(title, typeLabel, dateLieu, actions);
        card.getChildren().addAll(imageView, infoBox);

        return card;
    }

    private void applyFilters() {
        String query = searchField.getText();
        String selectedType = typeFilterCombo.getValue();

        List<Evenement> results;

        boolean hasSearch = query != null && !query.isEmpty();
        boolean hasFilter = selectedType != null && !selectedType.equals("Tous");

        if (hasSearch && hasFilter) {
            EventType type = EventType.valueOf(selectedType);
            results = service.searchPlanifierByTitle(query).stream()
                    .filter(e -> e.getType_event() == type)
                    .toList();
        }
        else if (hasSearch) {
            results = service.searchPlanifierByTitle(query);
        }
        else if (hasFilter) {
            EventType type = EventType.valueOf(selectedType);
            results = service.filterByType(type);
        }
        else {
            results = service.getVisibleEventsForEmployee();
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

    @FXML
    private void handleEvenements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventEmployeeDashboard.fxml"));
            Parent root = loader.load();

            StackPane contentArea = (StackPane) eventContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}