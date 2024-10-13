package com.example.patientviewer;
import com.example.patientviewer.model.Row2;

import com.example.patientviewer.model.Row5;

import com.example.patientviewer.model.Row6;

import com.example.patientviewer.model.Row8;
import com.example.patientviewer.model.RegistrationStation;
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
        } else if (letter.equals("D")) {
            registerPatientD(endpoint);
        } else if (letter.equals("B")) {
            registerPatientB(endpoint);
        } else {
            registerRow2Patient(endpoint, letter);
        }
    }

    private String getEndpointForLetter(String letter) {
        switch (letter) {
            case "P": case "A": case "W": return BASE_URL + "/register/row2-patient";
            case "E": return BASE_URL + "/register/patientE";
            case "D": return BASE_URL + "/register/patientD";
            case "B": return BASE_URL + "/register/patientB"; // Assuming there's an endpoint for Row6
            default: return "";
        }
    }
    private void registerPatientE(String endpoint) {
        sendRegistrationRequest(endpoint, this::handlePatientEResponse);
    }

    private void registerPatientD(String endpoint) {
        sendRegistrationRequest(endpoint, this::handlePatientDResponse);
    }

    private void handlePatientEResponse(String responseBody) {
        handleGenericResponse(responseBody, "5");
    }

    private void handlePatientDResponse(String responseBody) {
        handleGenericResponse(responseBody, "8");
    }

    private void handlePatientBResponse(String responseBody) {
        handleGenericResponse(responseBody, "6");
    }

    private void handleRow2Response(String responseBody, char category) {
        Platform.runLater(() -> {
            try {
                Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

                String patientId = (String) response.get("patientId");
                String registeredSequence = String.valueOf(response.get("registeredSequence"));

                if (patientId == null || registeredSequence == null) {
                    throw new Exception("Invalid response: missing required fields");
                }

                // Extract the number from patientId (e.g., "P1" -> 1)
                int patientNumber = Integer.parseInt(patientId.substring(1));

                // Update the TextField for row 2
                TextField row2Field = numberFields.get("2");
                if (row2Field != null) {
                    String currentText = row2Field.getText();
                    String updatedText = updateRow2Text(currentText, category, patientNumber);
                    row2Field.setText(updatedText);
                }

                ObservableList<String> results = FXCollections.observableArrayList(
                        "Patient ID: " + patientId,
                        "Registered Sequence: " + registeredSequence
                );
                resultListView.setItems(results);

                System.out.println("Successfully processed Row2 response: " + responseBody);
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage() + "\nResponse body: " + responseBody);
                System.err.println("Error processing Row2 response: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String updateRow2Text(String currentText, char category, int number) {
        String[] parts = currentText.isEmpty() ? new String[3] : currentText.split(" ");
        if (parts.length < 3) {
            parts = new String[]{"", "", ""};
        }

        switch (category) {
            case 'P':
                parts[0] = "P" + number;
                break;
            case 'A':
                parts[1] = "A" + number;
                break;
            case 'W':
                parts[2] = "W" + number;
                break;
        }

        return String.join(" ", parts).trim();
    }

    private void handleGenericResponse(String responseBody, String rowKey) {
        Platform.runLater(() -> {
            try {
                Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

                // Try to find a field that looks like a patient ID
                String patientId = null;
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    if (entry.getKey().toLowerCase().contains("patient") && entry.getKey().toLowerCase().contains("id")) {
                        patientId = String.valueOf(entry.getValue());
                        break;
                    }
                }

                // Try to find a field that looks like a registered sequence
                String registeredSequence = null;
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    if (entry.getKey().toLowerCase().contains("sequence")) {
                        registeredSequence = String.valueOf(entry.getValue());
                        break;
                    }
                }

                if (patientId == null || registeredSequence == null) {
                    throw new Exception("Invalid response: missing required fields");
                }

                // Extract the number from patientId (e.g., "D14" -> 14)
                int patientNumber = Integer.parseInt(patientId.replaceAll("\\D+", ""));

                // Update the TextField for the corresponding row
                TextField rowField = numberFields.get(rowKey);
                if (rowField != null) {
                    rowField.setText(String.valueOf(patientNumber));
                }

                ObservableList<String> results = FXCollections.observableArrayList(
                        "Patient ID: " + patientId,
                        "Registered Sequence: " + registeredSequence
                );
                resultListView.setItems(results);

                System.out.println("Successfully processed response: " + responseBody);
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage() + "\nResponse body: " + responseBody);
                System.err.println("Error processing response: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private void registerPatientB(String endpoint) {
        sendRegistrationRequest(endpoint, this::handlePatientBResponse);
    }

    private void registerRow2Patient(String endpoint, String letter) {
        char category = letter.charAt(0);



        Map<String, Object> row2Data = new HashMap<>();
        row2Data.put("patientCategory", category);


        sendRegistrationRequest(endpoint, responseBody -> handleRow2Response(responseBody, category), row2Data);
    }
    private void sendRegistrationRequest(String endpoint, java.util.function.Consumer<String> responseHandler) {
        sendRegistrationRequest(endpoint, responseHandler, null);
    }

    private void sendRegistrationRequest(String endpoint, java.util.function.Consumer<String> responseHandler, Map<String, Object> bodyData) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String dateParam = "?date=" + java.time.LocalDate.now().toString();

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + dateParam))
                    .header("Content-Type", "application/json");

            if (bodyData != null) {
                String jsonBody = objectMapper.writeValueAsString(bodyData);
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                System.out.println("Sending request to " + endpoint + " with body: " + jsonBody);
            } else {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                System.out.println("Sending request to " + endpoint + " with no body");
            }

            HttpRequest request = requestBuilder.build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("Received response from " + endpoint + ": " + response.body());
                        return response.body();
                    })
                    .thenAccept(responseHandler)
                    .exceptionally(e -> {
                        Platform.runLater(() -> showAlert("Error", getDetailedErrorMessage(e)));
                        System.err.println("Error sending request: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "Failed to create request: " + e.getMessage()));
            System.err.println("Error creating request: " + e.getMessage());
            e.printStackTrace();
        }
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

