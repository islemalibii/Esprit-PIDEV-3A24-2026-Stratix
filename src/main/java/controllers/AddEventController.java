package controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import models.Evenement;
import models.enums.EventStatus;
import models.enums.EventType;
import netscape.javascript.JSObject;
import services.ServiceEvenemnet;
import javafx.stage.FileChooser;
import java.io.File;



import java.io.IOException;
import java.time.LocalDate;

public class AddEventController {
    @FXML private TextField titreField;
    @FXML private TextField descriptionField;
    @FXML private TextField lieuField;
    @FXML private DatePicker datePicker;
    @FXML private TextField imageUrlField;
    @FXML private TextField mapSearchField;
    @FXML private WebView mapPicker;
    private File selectedImageFile;


    private final JavaConnector bridge = new JavaConnector();
    private double selectedLat = 0;
    private double selectedLng = 0;



    @FXML private ComboBox<EventType> typeCombo;
    @FXML private ComboBox<EventStatus> statusCombo;

    private ServiceEvenemnet service = new ServiceEvenemnet();

    @FXML
    public void initialize() {
        // types kol yokhrjou
        typeCombo.getItems().addAll(EventType.values());

        // Date par défaut hya lyoum
        datePicker.setValue(LocalDate.now());

        // par defaut tji planifier w disabled
        statusCombo.getItems().addAll(EventStatus.values());
        statusCombo.setValue(EventStatus.planifier);
        statusCombo.setDisable(true);


        WebEngine engine = mapPicker.getEngine();
        engine.load(getClass().getResource("/select_location.html").toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");

                window.setMember("javaConnector", bridge);

                engine.executeScript("setTimeout(function() { map.invalidateSize(); }, 500);");
            }
        });

    }

    private boolean validateInputs() {

        if (titreField.getText().isEmpty()) {
            showError("Le titre est obligatoire");
            return false;
        }

        if (descriptionField.getText().isEmpty()) {
            showError("La description est obligatoire");
            return false;
        }

        if (lieuField.getText().isEmpty()) {
            showError("Le lieu est obligatoire");
            return false;
        }

        if (datePicker.getValue() == null) {
            showError("Veuillez sélectionner une date");
            return false;
        }

        if (typeCombo.getValue() == null) {
            showError("Veuillez sélectionner le type d'événement");
            return false;
        }

        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showError("La date ne peut pas être dans le passé !");
            return false;
        }
        if (imageUrlField.getText().isEmpty()) {
            showError("Veuillez sélectionner une image pour l'événement");
            return false;
        }

        if (selectedLat == 0.0 && selectedLng == 0.0) {
            showError("Veuillez sélectionner l'emplacement précis sur la carte (cliquez sur la carte ou utilisez la recherche)");
            return false;
        }

        return true;
    }
    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une affiche");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(titreField.getScene().getWindow());
        if (selectedFile != null) {
            this.selectedImageFile = selectedFile; // Store the file object
            imageUrlField.setText(selectedFile.getName()); // Show just the name in the UI
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleMapSearch() {
        String address = mapSearchField.getText();
        if (address != null && !address.isEmpty()) {
            lieuField.setText(address);
            mapPicker.getEngine().executeScript("searchAddress('" + address.replace("'", "\\'") + "')");
        }
    }

    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                selectedLat = lat;
                selectedLng = lng;
                System.out.println("Success! Java received: " + lat + ", " + lng);
            });
        }
    }
    @FXML
    private void addEvent() {
        if (!validateInputs()) return;

        Evenement e = new Evenement();

        e.setTitre(titreField.getText());
        e.setDescription(descriptionField.getText());
        e.setLieu(lieuField.getText());
        e.setDate_event(datePicker.getValue());
        e.setType_event(typeCombo.getValue());
        e.setStatut(statusCombo.getValue());

        if (selectedImageFile != null) {
            String dbPath = processAndCopyImage(selectedImageFile);
            if (dbPath != null) {
                e.setImageUrl(dbPath);
            }
        } else {
            e.setImageUrl(imageUrlField.getText()); // Fallback
        }
        e.setLatitude(selectedLat);
        e.setLongitude(selectedLng);
        System.out.println("Controller sending to Service: Lat=" + e.getLatitude() + " Lng=" + e.getLongitude());

        service.add(e);


        showSuccessAndGoBack();
    }

    private void showSuccessAndGoBack() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès !!!");
        alert.setHeaderText(null);
        alert.setContentText("Événement créé avec succès !!!");
        alert.showAndWait();
        goBack();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/service-view.fxml"));
            Parent root = loader.load();
            Scene scene = titreField.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());


            StackPane contentArea = (StackPane) root.lookup("#contentArea");
            if (contentArea != null) {
                Node eventView = FXMLLoader.load(getClass().getResource("/EventDashboard.fxml"));
                contentArea.getChildren().setAll(eventView);
            }
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String processAndCopyImage(File sourceFile) {
        try {
            // Path to your Symfony project
            String uploadDir = "C:/Users/islem/stratix_web/public/uploads/events/";

            // Ensure directory exists
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Create a unique filename to avoid conflicts (like Symfony does)
            String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
            String uniqueName = java.util.UUID.randomUUID().toString().substring(0, 13) + extension;

            java.nio.file.Path targetPath = java.nio.file.Paths.get(uploadDir + uniqueName);

            // Physically copy the file
            java.nio.file.Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path that Symfony expects
            return "/uploads/events/" + uniqueName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}