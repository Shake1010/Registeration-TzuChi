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

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.net.ConnectException;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientViewerApp extends Application {

    private ListView<String> resultListView;
    private final String BASE_URL = "http://localhost:8080/api";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ToggleGroup letterGroup;
    private Map<String, TextField> numberFields;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Registration");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        HBox selectionArea = new HBox(10);
        VBox letterSelection = createLetterSelection();
        VBox numberDisplay = createNumberDisplay();
        selectionArea.getChildren().addAll(letterSelection, numberDisplay);

        Button registerButton = new Button("Register New Patient");
        registerButton.setOnAction(e -> registerPatient());

        Button testConnectionButton = new Button("Test Connection");
        testConnectionButton.setOnAction(e -> testConnection());

        resultListView = new ListView<>();

        layout.getChildren().addAll(selectionArea, registerButton, testConnectionButton, resultListView);

        Scene scene = new Scene(layout, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createLetterSelection() {
        VBox letterBox = new VBox(5);
        letterBox.setPadding(new Insets(10));
        letterBox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        letterGroup = new ToggleGroup();
        String[] letters = {"P", "A", "W", "E", "D", "B"};

        for (String letter : letters) {
            RadioButton rb = new RadioButton(letter);
            rb.setToggleGroup(letterGroup);
            letterBox.getChildren().add(rb);
        }

        return letterBox;
    }

    private VBox createNumberDisplay() {
        VBox numberBox = new VBox(5);
        numberBox.setPadding(new Insets(10));
        numberBox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        String[] columns = {"2", "5", "8", "6"};
        numberFields = new HashMap<>();

        for (String column : columns) {
            TextField tf = new TextField();
            tf.setEditable(false);
            tf.setPrefColumnCount(3);
            numberFields.put(column, tf);
            HBox row = new HBox(5);
            row.getChildren().addAll(new Label(column), tf);
            numberBox.getChildren().add(row);
        }

        return numberBox;
    }

    private void registerPatient() {
        RadioButton selectedLetter = (RadioButton) letterGroup.getSelectedToggle();
        if (selectedLetter == null) {
            showAlert("Error", "Please select a letter.");
            return;
        }

        String letter = selectedLetter.getText();
        String endpoint = getEndpointForLetter(letter);

        if (letter.equals("E")) {
            registerPatientE(endpoint);
        } else {
            registerOtherPatient(endpoint, letter);
        }
    }

    private String getEndpointForLetter(String letter) {
        switch (letter) {
            case "P": case "A": case "W": return BASE_URL + "/register/row2-patient";
            case "E": return BASE_URL + "/register/patientE";
            case "D": return BASE_URL + "/register/patientD";
            case "B": return BASE_URL + "/register/row6-patient"; // Assuming there's an endpoint for Row6
            default: return "";
        }
    }

    private void registerPatientE(String endpoint) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::updateResultList)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", getDetailedErrorMessage(e)));
                    e.printStackTrace();
                    return null;
                });
    }

    private void registerOtherPatient(String endpoint, String letter) {
        String column = getColumnForLetter(letter);
        TextField numberField = numberFields.get(column);

        int currentNumber = numberField.getText().isEmpty() ? 0 : Integer.parseInt(numberField.getText());
        int nextNumber = currentNumber + 1;
        numberField.setText(String.valueOf(nextNumber));

        String patientId = letter + nextNumber;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String jsonBody = String.format("{\"patientId\":\"%s\"}", patientId);
        String dateParam = "?date=" + java.time.LocalDate.now();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + dateParam))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error code: " + response.statusCode());
                    }
                    return response.body();
                })
                .thenAccept(this::updateResultList)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", getDetailedErrorMessage(e)));
                    e.printStackTrace();
                    return null;
                });
    }
    private String getColumnForLetter(String letter) {
        switch (letter) {
            case "P": case "A": case "W": return "2";
            case "E": return "5";
            case "D": return "8";
            case "B": return "6";
            default: return "";
        }
    }

    private void testConnection() {
        System.out.println("Attempting to connect to: " + BASE_URL);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/get/allRegister"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status code: " + response.statusCode());
                    return response;
                })
                .thenAccept(response -> Platform.runLater(() ->
                        showAlert("Connection Test", "Connected successfully. Status code: " + response.statusCode())))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Connection Error", getDetailedErrorMessage(e)));
                    e.printStackTrace();
                    return null;
                });
    }

    private void updateResultList(String responseBody) {
        Platform.runLater(() -> {
            try {
                if (responseBody == null || responseBody.isEmpty()) {
                    throw new Exception("Empty response from server");
                }

                Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

                String patientId = String.valueOf(response.get("patientId"));
                String registeredSequence = String.valueOf(response.get("registeredSequence"));

                if ("null".equals(patientId) || "null".equals(registeredSequence)) {
                    throw new Exception("Invalid response: patientId or registeredSequence is null");
                }

                ObservableList<String> results = FXCollections.observableArrayList(
                        "Patient ID: " + patientId,
                        "Registered Sequence: " + registeredSequence
                );
                resultListView.setItems(results);
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage() + "\nResponse body: " + responseBody);
            }
        });
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getDetailedErrorMessage(Throwable e) {
        StringBuilder message = new StringBuilder("An error occurred:\n");
        Throwable cause = e;
        while (cause != null) {
            message.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
            if (cause instanceof ConnectException) {
                message.append("The connection failed. The server might be down or unreachable.\n");
            } else if (cause instanceof UnknownHostException) {
                message.append("The host could not be resolved. Check if the URL is correct.\n");
            }
            cause = cause.getCause();
        }
        return message.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}