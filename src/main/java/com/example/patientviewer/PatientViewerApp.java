package com.example.patientviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class PatientViewerApp extends Application {

    private final String BASE_URL = "http://localhost:8080/api";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, LinkedList<String>> columnData = new HashMap<>();
    private Map<String, VBox> columnListViews = new HashMap<>();
    private Label headerLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Queue System");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: white;");

        headerLabel = new Label("Latest Registration");
        headerLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 20px; -fx-font-weight: bold;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);
        mainLayout.setTop(headerLabel);

        HBox contentArea = new HBox(10);
        GridPane leftSection = createLeftSection();
        GridPane rightSection = createRightSection();

        // Set the HBox to use percentage-based sizing
        HBox.setHgrow(leftSection, Priority.NEVER);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        // Set the widths using pixels
        leftSection.setPrefWidth(300);
        rightSection.setPrefWidth(700); // Increased width to accommodate the new layout

        contentArea.getChildren().addAll(leftSection, rightSection);
        mainLayout.setCenter(contentArea);

        // Adjust the initial window size
        Scene scene = new Scene(mainLayout, 1050, 600); // Increased width to fit the new layout
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeColumnData();
        startPeriodicUpdates();
    }

    private GridPane createLeftSection() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));
        grid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row = new RowConstraints();
        row.setPercentHeight(33.33);
        grid.getRowConstraints().addAll(row, row, row);

        String[] categories = {"A", "E", "B", "W", "P", "D"};
        for (int i = 0; i < categories.length; i++) {
            Button categoryButton = new Button(categories[i]);
            categoryButton.setMaxWidth(Double.MAX_VALUE);
            categoryButton.setMaxHeight(Double.MAX_VALUE);
            categoryButton.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;"); // Increased font size
            categoryButton.setOnAction(e -> registerPatient(categoryButton.getText()));
            GridPane.setFillWidth(categoryButton, true);
            GridPane.setFillHeight(categoryButton, true);
            grid.add(categoryButton, i % 2, i / 2);
        }

        return grid;
    }

    private GridPane createRightSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));

        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setPercentWidth(23);
        columnConstraint.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(columnConstraint, columnConstraint, columnConstraint, columnConstraint);

        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(rowConstraint);

        String[] columnNames = {"2", "5", "8", "6"};
        for (int i = 0; i < columnNames.length; i++) {
            VBox columnBox = createColumnBox(columnNames[i]);
            GridPane.setHgrow(columnBox, Priority.ALWAYS);
            GridPane.setVgrow(columnBox, Priority.ALWAYS);
            GridPane.setFillWidth(columnBox, true);
            GridPane.setFillHeight(columnBox, true);
            grid.add(columnBox, i, 0);
        }

        return grid;
    }

    private VBox createColumnBox(String columnName) {
        VBox columnBox = new VBox(5);
        columnBox.setAlignment(Pos.TOP_CENTER);
        columnBox.setFillWidth(true);
        VBox.setVgrow(columnBox, Priority.ALWAYS);

        Label columnLabel = new Label(columnName);
        columnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        columnLabel.setMaxWidth(Double.MAX_VALUE);
        columnLabel.setAlignment(Pos.CENTER);

        VBox idContainer = new VBox(5);
        idContainer.setAlignment(Pos.TOP_CENTER);
        idContainer.setFillWidth(true);
        VBox.setVgrow(idContainer, Priority.ALWAYS);

        for (int j = 0; j < 4; j++) {
            Label idLabel = new Label("");
            idLabel.setStyle("-fx-padding: 5px; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-alignment: center;");
            idLabel.setMaxWidth(Double.MAX_VALUE);
            idLabel.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(idLabel, Priority.ALWAYS);
            idContainer.getChildren().add(idLabel);
        }

        ScrollPane scrollPane = new ScrollPane(idContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        columnListViews.put(columnName, idContainer);

        Button moreButton = new Button("▼");
        moreButton.setMaxWidth(Double.MAX_VALUE);
        moreButton.setOnAction(e -> handleMoreButtonClick(columnName));

        columnBox.getChildren().addAll(columnLabel, scrollPane, moreButton);

        return columnBox;
    }
    private void handleMoreButtonClick(String column) {
        // Implement the logic for when the "More" button is clicked
        // This could involve showing a popup with more items, etc.
        System.out.println("More button clicked for column: " + column);
    }

    private void updateColumnDisplay(String column) {
        VBox idContainer = columnListViews.get(column);
        LinkedList<String> items = columnData.get(column);

        for (int i = 0; i < 4; i++) {
            Label idLabel = (Label) idContainer.getChildren().get(i);
            if (i < items.size()) {
                idLabel.setText(items.get(i));
            } else {
                idLabel.setText("");
            }
        }
    }
    private void initializeColumnData() {
        columnData.put("2", new LinkedList<>());
        columnData.put("5", new LinkedList<>());
        columnData.put("8", new LinkedList<>());
        columnData.put("6", new LinkedList<>());
    }

    private void registerPatient(String category) {
        String endpoint = getEndpointForCategory(category);
        if (endpoint.isEmpty()) {
            showAlert("Error", "Invalid category");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("patientCategory", category);

        sendRegistrationRequest(endpoint, requestBody);
    }

    private String getEndpointForCategory(String category) {
        switch (category) {
            case "P":
            case "A":
            case "W":
                return BASE_URL + "/register/row2-patient";
            case "E":
                return BASE_URL + "/register/patientE";
            case "D":
                return BASE_URL + "/register/patientD";
            case "B":
                return BASE_URL + "/register/patientB";
            default:
                return "";
        }
    }

    private void sendRegistrationRequest(String endpoint, Map<String, Object> bodyData) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String dateParam = "?date=" + java.time.LocalDate.now().toString();

        try {
            String jsonBody = objectMapper.writeValueAsString(bodyData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + dateParam))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleRegistrationResponse)
                    .exceptionally(e -> {
                        Platform.runLater(() -> showAlert("Error", "Failed to register patient: " + e.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            showAlert("Error", "Failed to create request: " + e.getMessage());
        }
    }

    private void handleRegistrationResponse(String responseBody) {
        Platform.runLater(() -> {
            try {
                Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                String patientId = (String) response.get("patientId");
                if (patientId != null) {
                    updateQueueDisplay(patientId);
                    updateHeaderWithLatestPatientId(patientId);
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage());
            }
        });
    }


    private void updateQueueDisplay(String patientId) {
        String column = getColumnForPatientId(patientId);
        if (column != null) {
            LinkedList<String> columnItems = columnData.get(column);
            columnItems.addFirst(patientId);
            while (columnItems.size() > 4) {
                columnItems.removeLast();
            }
            updateColumnDisplay(column);
        }
    }

    private void updateHeaderWithLatestPatientId(String patientId) {
        headerLabel.setText("Latest Registered: " + patientId);
    }

    private String getColumnForPatientId(String patientId) {
        if (patientId == null || patientId.length() < 2) {
            return null;
        }

        String category = patientId.substring(0, 1);
        String sectionNumber = patientId.substring(1, 2);

        switch (category) {
            case "P":
            case "A":
            case "W":
                return "2";
            case "E":
                return "5";
            case "D":
                return "8";
            case "B":
                return "6";
            default:
                return null;
        }
    }



    private void startPeriodicUpdates() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);  // Update every 5 seconds
                    Platform.runLater(this::fetchQueueData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void fetchQueueData() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String[] endpoints = {
                BASE_URL + "/row2",
                BASE_URL + "/row5",
                BASE_URL + "/row8",
                BASE_URL + "/row6"
        };

        for (String endpoint : endpoints) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleQueueDataResponse)
                    .exceptionally(e -> {
                        Platform.runLater(() -> showAlert("Error", "Failed to fetch queue data: " + e.getMessage()));
                        return null;
                    });
        }
    }


    private void handleQueueDataResponse(String responseBody) {
        try {
            // First, try to parse the response as an error object
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            if (responseMap.containsKey("status") && responseMap.containsKey("error")) {
                // This is an error response
                int status = ((Number) responseMap.get("status")).intValue(); // Fixed: Use Number and intValue()
                String error = (String) responseMap.get("error");
                String path = (String) responseMap.get("path");

                Platform.runLater(() -> {
                    showAlert("Error", "Failed to fetch queue data: " + error + " (Status: " + status + ", Path: " + path + ")");
                });
                return;
            }

            // If it's not an error, proceed with parsing as patient data
            List<Map<String, Object>> patientList = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});

            // Clear existing data
            for (String column : columnData.keySet()) {
                columnData.get(column).clear();
            }

            // Process each patient in the list
            for (Map<String, Object> patient : patientList) {
                String patientId = (String) patient.get("patientId");
                String column = getColumnForPatientId(patientId);

                if (column != null) {
                    LinkedList<String> columnItems = columnData.get(column);
                    columnItems.addLast(patientId);
                    while (columnItems.size() > 4) {
                        columnItems.removeFirst();
                    }
                }
            }

            // Update display for all columns
            for (String column : columnData.keySet()) {
                Platform.runLater(() -> updateColumnDisplay(column));
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to parse queue data: " + e.getMessage());
            });
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}