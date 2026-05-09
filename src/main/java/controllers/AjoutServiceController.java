package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Service;
import models.CategorieService;
import models.ResponsableInfo;
import models.UserRole;
import services.ServiceService;
import services.CategorieServiceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutServiceController implements Initializable {

    @FXML private Label lblTitrePopup;
    @FXML private Label lblError;
    @FXML private TextField txtTitre;
    @FXML private ComboBox<CategorieService> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateCreationPicker;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<ResponsableInfo> comboResponsable;
    @FXML private TextField txtBudget;
    @FXML private Button btnAction;

    private ServiceService serviceService;
    private CategorieServiceService categorieService;
    private Service serviceAModifier;
    private boolean modeModification = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateCreationPicker.setValue(LocalDate.now());
        chargerCategories();
        chargerResponsables();
        if (UserRole.getInstance().isEmployee()) {
            btnAction.setVisible(false);
            btnAction.setManaged(false);

        }
    }

    private void chargerCategories() {
        try {
            categorieService = new CategorieServiceService();
            List<CategorieService> categories = categorieService.afficherAll();
            comboCategorie.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            lblError.setText("Erreur chargement catégories");
        }
    }

    private void chargerResponsables() {
        try {
            if (serviceService == null) {
                serviceService = new ServiceService();
            }
            List<ResponsableInfo> responsables = serviceService.getResponsables();
            comboResponsable.setItems(FXCollections.observableArrayList(responsables));
        } catch (SQLException e) {
            lblError.setText("Erreur chargement responsables: " + e.getMessage());
        }
    }

    public void setServiceService(ServiceService serviceService) {
        this.serviceService = serviceService;
        chargerResponsables();
    }

    public void setServiceAModifier(Service service) {
        this.serviceAModifier = service;
        this.modeModification = true;
        lblTitrePopup.setText("MODIFIER SERVICE");
        btnAction.setText("Modifier");
        txtTitre.setText(service.getTitre());
        txtDescription.setText(service.getDescription());
        dateCreationPicker.setValue(LocalDate.parse(service.getDateCreation()));
        dateDebutPicker.setValue(LocalDate.parse(service.getDateDebut()));
        dateFinPicker.setValue(LocalDate.parse(service.getDateFin()));
        txtBudget.setText(String.valueOf(service.getBudget()));
        if (service.getCategorie() != null) {
            comboCategorie.setValue(service.getCategorie());
        }
        if (service.getUtilisateurId() > 0) {
            for (ResponsableInfo r : comboResponsable.getItems()) {
                if (r.getId() == service.getUtilisateurId()) {
                    comboResponsable.setValue(r);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleAjouter() {
        lblError.setText("");
        if (!validerChamps()) return;
        try {
            if (modeModification) {
                serviceAModifier.setTitre(txtTitre.getText());
                serviceAModifier.setDescription(txtDescription.getText());
                serviceAModifier.setDateDebut(dateDebutPicker.getValue().toString());
                serviceAModifier.setDateFin(dateFinPicker.getValue().toString());
                serviceAModifier.setBudget(Double.parseDouble(txtBudget.getText()));
                CategorieService catSelectionnee = comboCategorie.getValue();
                if (catSelectionnee != null) {
                    serviceAModifier.setCategorieId(catSelectionnee.getId());
                }
                ResponsableInfo respSelectionne = comboResponsable.getValue();
                if (respSelectionne != null) {
                    serviceAModifier.setUtilisateurId(respSelectionne.getId());
                }
                serviceService.updateTitre(serviceAModifier);
                showAlert("Succès", "Service modifié!");
            } else {
                Service service = new Service();
                service.setTitre(txtTitre.getText());
                service.setDescription(txtDescription.getText());
                service.setDateCreation(dateCreationPicker.getValue().toString());
                service.setDateDebut(dateDebutPicker.getValue().toString());
                service.setDateFin(dateFinPicker.getValue().toString());
                service.setBudget(Double.parseDouble(txtBudget.getText()));
                CategorieService catSelectionnee = comboCategorie.getValue();
                if (catSelectionnee != null) {
                    service.setCategorieId(catSelectionnee.getId());
                }
                ResponsableInfo respSelectionne = comboResponsable.getValue();
                if (respSelectionne != null) {
                    service.setUtilisateurId(respSelectionne.getId());
                }
                serviceService.ajouter(service);
                showAlert("Succès", "Service ajouté!");
            }
            fermer();
        } catch (SQLException e) {
            lblError.setText("Erreur SQL: " + e.getMessage());
        } catch (NumberFormatException e) {
            lblError.setText("Budget doit être un nombre");
        }
    }

    private boolean validerChamps() {
        if (txtTitre.getText().trim().isEmpty()) {
            lblError.setText("Le titre est obligatoire");
            return false;
        }
        if (comboCategorie.getValue() == null) {
            lblError.setText("Veuillez sélectionner une catégorie");
            return false;
        }
        if (comboResponsable.getValue() == null) {
            lblError.setText("Veuillez sélectionner un responsable");
            return false;
        }
        if (txtDescription.getText().trim().isEmpty()) {
            lblError.setText("La description est obligatoire");
            return false;
        }
        if (dateDebutPicker.getValue() == null) {
            lblError.setText("La date de début est obligatoire");
            return false;
        }
        if (dateFinPicker.getValue() == null) {
            lblError.setText("La date de fin est obligatoire");
            return false;
        }
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            lblError.setText("La date de fin doit être après la date de début");
            return false;
        }

        if (dateDebutPicker.getValue().isBefore(LocalDate.now())) {
            lblError.setText("La date de début ne peut pas être dans le passé");
            return false;
        }
        if (txtBudget.getText().trim().isEmpty()) {
            lblError.setText("Le budget est obligatoire");
            return false;
        }
        return true;
    }

    @FXML
    private void handleFermer() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}