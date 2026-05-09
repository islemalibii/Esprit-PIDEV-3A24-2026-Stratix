package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import models.Employe;
import models.Planning;
import services.EmployeeService;
import services.SERVICEPlanning;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class CalendarController {

    @FXML private Label lblMonthYear;
    @FXML private GridPane calendarGrid;
    @FXML private VBox eventsContainer;
    @FXML private Label lblSelectedDate;
    @FXML private ListView<String> eventsListView;

    private YearMonth currentYearMonth;
    private SERVICEPlanning planningService;
    private EmployeeService employeService;
    private Map<LocalDate, List<Planning>> planningsByDate;
    private LocalDate selectedDate;

    private final String[] monthColors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
            "#FFE194", "#E6B89C", "#FE938C", "#9B59B6",
            "#3498DB", "#F1C40F", "#E67E22", "#E74C3C"
    };

    @FXML
    public void initialize() {
        planningService = new SERVICEPlanning();
        employeService = new EmployeeService();
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        planningsByDate = new HashMap<>();

        loadPlannings();
        drawCalendar();
        showSelectedDateEvents();
    }

    private void loadPlannings() {
        planningsByDate.clear();
        List<Planning> allPlannings = planningService.getAllPlannings();

        for (Planning p : allPlannings) {
            LocalDate date = p.getDate().toLocalDate();
            if (!planningsByDate.containsKey(date)) {
                planningsByDate.put(date, new ArrayList<>());
            }
            planningsByDate.get(date).add(p);
        }

        System.out.println("📅 Plannings chargés: " + allPlannings.size() + " pour " + planningsByDate.size() + " dates");
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();

        // Style du mois courant
        int currentMonth = currentYearMonth.getMonthValue() - 1;
        String monthHeaderColor = monthColors[currentMonth];

        // Ajouter les noms des jours
        String[] daysOfWeek = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < 7; i++) {
            VBox dayHeader = new VBox();
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPrefWidth(140);
            dayHeader.setPrefHeight(40);

            if (i >= 5) {
                dayHeader.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8 8 0 0;");
            } else {
                dayHeader.setStyle("-fx-background-color: " + monthHeaderColor + "; -fx-background-radius: 8 8 0 0;");
            }

            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            dayLabel.setTextFill(Color.WHITE);

            dayHeader.getChildren().add(dayLabel);
            calendarGrid.add(dayHeader, i, 0);
        }

        // Obtenir le premier jour du mois
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date, day);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Mettre à jour le titre
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthYear = currentYearMonth.format(formatter);
        lblMonthYear.setText(monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1));
    }

    private VBox createDayCell(LocalDate date, int dayNumber) {
        VBox dayCell = new VBox(5);
        dayCell.setPrefWidth(140);
        dayCell.setPrefHeight(120);
        dayCell.setPadding(new Insets(8));
        dayCell.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: white;");

        Label dayNumLabel = new Label(String.valueOf(dayNumber));
        dayNumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        headerBox.getChildren().add(dayNumLabel);

        dayCell.getChildren().add(headerBox);

        if (planningsByDate.containsKey(date)) {
            List<Planning> dayPlannings = planningsByDate.get(date);

            VBox eventsBox = new VBox(3);
            eventsBox.setPadding(new Insets(0, 0, 0, 5));

            for (Planning p : dayPlannings) {
                HBox eventBox = createEventBadge(p);
                eventsBox.getChildren().add(eventBox);
            }

            dayCell.getChildren().add(eventsBox);
        }

        // Style pour aujourd'hui
        if (date.equals(LocalDate.now())) {
            dayCell.setStyle("-fx-border-color: " + monthColors[currentYearMonth.getMonthValue() - 1] +
                    "; -fx-border-width: 3; -fx-background-color: #f0f9ff;");
        }

        // Style pour le jour sélectionné
        if (date.equals(selectedDate)) {
            dayCell.setStyle("-fx-border-color: #3498db; -fx-border-width: 3; -fx-background-color: #ebf5ff;");
        }

        // ⭐ Rendre la cellule cliquable pour afficher les événements du jour
        dayCell.setOnMouseClicked(event -> {
            selectedDate = date;
            drawCalendar(); // Redessiner pour montrer la sélection
            showSelectedDateEvents(); // Afficher les événements
        });

        // Effet hover
        dayCell.setOnMouseEntered(e ->
                dayCell.setStyle("-fx-border-color: " + monthColors[currentYearMonth.getMonthValue() - 1] +
                        "; -fx-border-width: 2; -fx-background-color: #f8f9fa;")
        );
        dayCell.setOnMouseExited(e -> {
            if (date.equals(selectedDate)) {
                dayCell.setStyle("-fx-border-color: #3498db; -fx-border-width: 3; -fx-background-color: #ebf5ff;");
            } else if (date.equals(LocalDate.now())) {
                dayCell.setStyle("-fx-border-color: " + monthColors[currentYearMonth.getMonthValue() - 1] +
                        "; -fx-border-width: 3; -fx-background-color: #f0f9ff;");
            } else {
                dayCell.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: white;");
            }
        });

        return dayCell;
    }

    private HBox createEventBadge(Planning p) {
        HBox eventBox = new HBox(5);
        eventBox.setAlignment(Pos.CENTER_LEFT);

        Rectangle colorRect = new Rectangle(4, 12);
        String shiftColor = getShiftColor(p.getTypeShift());
        colorRect.setFill(Color.web(shiftColor));
        colorRect.setArcWidth(2);
        colorRect.setArcHeight(2);

        Employe emp = employeService.getEmployeById(p.getEmployeId());
        String empName = (emp != null) ? emp.getUsername() : "E" + p.getEmployeId();
        if (empName.length() > 6) {
            empName = empName.substring(0, 6) + ".";
        }

        Label empLabel = new Label(empName);
        empLabel.setFont(Font.font("Arial", 11));
        empLabel.setTextFill(Color.web(shiftColor));

        String timeStr = p.getHeureDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(timeStr);
        timeLabel.setFont(Font.font("Arial", 9));
        timeLabel.setTextFill(Color.GRAY);

        eventBox.getChildren().addAll(colorRect, empLabel, timeLabel);

        return eventBox;
    }

    private String getShiftColor(String shiftType) {
        switch(shiftType) {
            case "JOUR": return "#3b82f6";
            case "SOIR": return "#f59e0b";
            case "NUIT": return "#8b5cf6";
            case "CONGE": return "#10b981";
            case "MALADIE": return "#ef4444";
            case "FORMATION": return "#f97316";
            default: return "#6b7280";
        }
    }

    /**
     * ⭐ Affiche les événements du jour sélectionné ⭐
     */
    private void showSelectedDateEvents() {
        if (eventsListView == null) return;

        eventsListView.getItems().clear();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
        String formattedDate = selectedDate.format(dateFormatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

        // Titre du jour
        eventsListView.getItems().add("📅 " + formattedDate);
        eventsListView.getItems().add("───────────────────");

        if (planningsByDate.containsKey(selectedDate)) {
            List<Planning> dayPlannings = planningsByDate.get(selectedDate);

            for (Planning p : dayPlannings) {
                Employe emp = employeService.getEmployeById(p.getEmployeId());
                String empName = (emp != null) ? emp.getUsername() : "Employé " + p.getEmployeId();

                String shiftIcon = getShiftIcon(p.getTypeShift());
                String shiftName = getShiftName(p.getTypeShift());

                String timeStr = p.getHeureDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        " - " +
                        p.getHeureFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

                String eventLine = shiftIcon + " " + empName + " (" + shiftName + ")";
                eventsListView.getItems().add(eventLine);
                eventsListView.getItems().add("   🕒 " + timeStr);
                eventsListView.getItems().add(""); // Ligne vide pour séparation
            }
        } else {
            eventsListView.getItems().add("   Aucun planning pour cette date");
        }
    }

    private String getShiftIcon(String shiftType) {
        switch(shiftType) {
            case "JOUR": return "☀️";
            case "SOIR": return "🌆";
            case "NUIT": return "🌙";
            case "CONGE": return "🏖️";
            case "MALADIE": return "🤒";
            case "FORMATION": return "📚";
            default: return "📅";
        }
    }

    private String getShiftName(String shiftType) {
        switch(shiftType) {
            case "JOUR": return "Jour";
            case "SOIR": return "Soir";
            case "NUIT": return "Nuit";
            case "CONGE": return "Congé";
            case "MALADIE": return "Maladie";
            case "FORMATION": return "Formation";
            default: return shiftType;
        }
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        loadPlannings();
        drawCalendar();
        showSelectedDateEvents();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        loadPlannings();
        drawCalendar();
        showSelectedDateEvents();
    }

    @FXML
    private void goToToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        loadPlannings();
        drawCalendar();
        showSelectedDateEvents();
    }

    @FXML
    private void showDashboardFromButton() { loadView("/dashboard-view.fxml"); }
    @FXML
    private void showPlanningFromButton() { loadView("/PlanningListeView.fxml"); }
    @FXML
    private void showTachesFromButton() { loadView("/TacheListeView.fxml"); }
    @FXML
    private void showCalendarFromButton() { loadView("/calendar-view.fxml"); }
    @FXML
    private void showWhiteboardFromButton() { loadView("/WhiteboardView.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (MainController.staticContentArea != null) {
                MainController.staticContentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}