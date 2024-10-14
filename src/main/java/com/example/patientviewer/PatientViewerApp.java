package com.example.patientviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientViewerApp extends Application {

    private final String BASE_URL = "http://localhost:8080/api";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, LinkedList<String>> columnData = new HashMap<>();
    private Map<String, ListView<String>> columnListViews = new HashMap<>();
    private Label headerLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Queue System");

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: white;");

        headerLabel = new Label("註冊的號碼");
        headerLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 20px; -fx-font-weight: bold;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);

        HBox contentArea = new HBox(10);
        GridPane leftSection = createLeftSection();
        GridPane rightSection = createRightSection();

        HBox.setHgrow(leftSection, Priority.NEVER);
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        leftSection.setPrefWidth(300);
        rightSection.setPrefWidth(700);

        contentArea.getChildren().addAll(leftSection, rightSection);

        mainLayout.getChildren().addAll(headerLabel, contentArea);

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeColumnData();
        startPeriodicUpdates();
    }

    private GridPane createLeftSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        String[] categories = {"A", "E", "B", "W", "P", "D"};
        for (int i = 0; i < categories.length; i++) {
            Button categoryButton = new Button(categories[i]);
            categoryButton.setMaxWidth(Double.MAX_VALUE);
            categoryButton.setMinHeight(80);
            categoryButton.setStyle("-fx-font-size: 18px;");
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
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        String[] columnNames = {"2", "5", "8", "6"};
        for (int i = 0; i < columnNames.length; i++) {
            Label columnLabel = new Label(columnNames[i]);
            columnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            grid.add(columnLabel, i, 0);

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(480);
            columnListViews.put(columnNames[i], listView);
            GridPane.setVgrow(listView, Priority.ALWAYS);
            GridPane.setHgrow(listView, Priority.ALWAYS);
            grid.add(listView, i, 1, 1, 5);  // Span 5 rows
        }

        return grid;
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
            columnItems.addFirst(patientId);  // Add to the start of the list (bottom of the display)
            updateColumnListView(column);
        }
    }

    private void updateHeaderWithLatestPatientId(String patientId) {
        headerLabel.setText("Latest Registered: " + patientId);
    }

    private String getColumnForPatientId(String patientId) {
        char category = patientId.charAt(0);
        switch (category) {
            case 'P':
            case 'A':
            case 'W':
                return "2";
            case 'E':
                return "5";
            case 'D':
                return "8";
            case 'B':
                return "6";
            default:
                return null;
        }
    }

    private void updateColumnListView(String column) {
        ListView<String> listView = columnListViews.get(column);
        LinkedList<String> items = columnData.get(column);
        ObservableList<String> observableItems = FXCollections.observableArrayList(items);
        listView.setItems(observableItems);
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
            // Parse the response and update the queue display
            // The exact implementation will depend on the structure of your API response
            // You'll need to extract the patient IDs and update the corresponding columns
        } catch (Exception e) {
            showAlert("Error", "Failed to parse queue data: " + e.getMessage());
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