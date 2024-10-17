package com.example.patientviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientViewerApp extends Application {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final Map<String, LinkedList<String>> columnData = new HashMap<>();
    private final Map<String, VBox> columnListViews = new HashMap<>();
    private Label headerLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Queue System");

        BorderPane mainLayout = createMainLayout();
        Scene scene = new Scene(mainLayout, 1050, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeColumnData();
        startPeriodicUpdates();
    }

    private BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: white;");

        headerLabel = createHeaderLabel();
        mainLayout.setTop(headerLabel);

        HBox contentArea = new HBox(10);
        GridPane leftSection = createLeftSection();
        GridPane rightSection = createRightSection();

        HBox.setHgrow(leftSection, Priority.NEVER);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        leftSection.setPrefWidth(300);
        rightSection.setPrefWidth(700);

        contentArea.getChildren().addAll(leftSection, rightSection);
        mainLayout.setCenter(contentArea);

        return mainLayout;
    }

    private Label createHeaderLabel() {
        Label label = new Label("Latest Registration");
        label.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 20px; -fx-font-weight: bold;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
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
            Button categoryButton = createCategoryButton(categories[i]);
            grid.add(categoryButton, i % 2, i / 2);
        }

        return grid;
    }

    private Button createCategoryButton(String category) {
        Button button = new Button(category);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        button.setOnAction(e -> registerPatient(category));
        GridPane.setFillWidth(button, true);
        GridPane.setFillHeight(button, true);
        return button;
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

        VBox idContainer = createIdContainer();
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

    private VBox createIdContainer() {
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

        return idContainer;
    }

    private void handleMoreButtonClick(String column) {
        System.out.println("More button clicked for column: " + column);
    }

    private void updateColumnDisplay(String column) {
        VBox idContainer = columnListViews.get(column);
        LinkedList<String> items = columnData.get(column);

        for (int i = 0; i < 4; i++) {
            Label idLabel = (Label) idContainer.getChildren().get(i);
            idLabel.setText(i < items.size() ? items.get(i) : "");
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
        String dateParam = "?date=" + LocalDate.now();

        try {
            String jsonBody = OBJECT_MAPPER.writeValueAsString(bodyData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + dateParam))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
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
                Map<String, Object> response = OBJECT_MAPPER.readValue(responseBody, Map.class);
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

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleQueueDataResponse)
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            String errorMessage = "Failed to fetch queue data: " + e.getMessage();
                            System.err.println(errorMessage);
                            e.printStackTrace();
                            showAlert("Error", errorMessage);
                        });
                        return null;
                    });
        }
    }


    private String getColumnForEndpoint(String responseBody) {
        if (responseBody.contains("\"sectionNumber\":2")) return "2";
        if (responseBody.contains("\"sectionNumber\":5")) return "5";
        if (responseBody.contains("\"sectionNumber\":6")) return "6";
        if (responseBody.contains("\"sectionNumber\":8")) return "8";
        return null;
    }

    private void handleQueueDataResponse(String responseBody) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);

            int sectionNumber = rootNode.get("sectionNumber").asInt();
            String column = String.valueOf(sectionNumber);

            JsonNode patientsNode = rootNode.get("patients");

            LinkedList<String> columnItems = columnData.get(column);
            columnItems.clear();

            for (JsonNode patientNode : patientsNode) {
                String patientId = patientNode.get("patientId").asText();
                boolean inQueue = patientNode.get("inQueue").asBoolean();
                if (inQueue) {
                    columnItems.addLast(patientId);
                }
            }

            while (columnItems.size() > 4) {
                columnItems.removeFirst();
            }

            Platform.runLater(() -> updateColumnDisplay(column));
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to parse queue data: " + e.getMessage());
                e.printStackTrace();
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