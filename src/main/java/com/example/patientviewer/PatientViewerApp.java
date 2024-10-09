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

import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientViewerApp extends Application {

    private ListView<String> resultListView;
    private final String API_URL = "http://localhost:8080/api/register/patientE"; // Update this URL
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
        String[] letters = {"A", "E", "B", "W", "P", "D"};

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
        String column = getColumnForLetter(letter);
        TextField numberField = numberFields.get(column);

        // Here you would typically get the next number from your backend
        // For now, we'll just increment the number in the field
        int currentNumber = numberField.getText().isEmpty() ? 0 : Integer.parseInt(numberField.getText());
        int nextNumber = currentNumber + 1;
        numberField.setText(String.valueOf(nextNumber));

        String patientId = letter + nextNumber;

        sendRegistrationRequest(patientId);
    }

    private String getColumnForLetter(String letter) {
        switch (letter) {
            case "A": case "B": return "2";
            case "E": case "W": return "5";
            case "P": return "8";
            case "D": return "6";
            default: return "";
        }
    }

    private void testConnection() {
        System.out.println("Attempting to connect to: " + API_URL);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
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

    private void sendRegistrationRequest(String patientId) {
        System.out.println("Sending registration request to: " + API_URL);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString("{\"patientId\":\"" + patientId + "\"}"))
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

    private void updateResultList(String responseBody) {
        Platform.runLater(() -> {
            try {
                Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
                String patientId = (String) response.get("patientId");
                Integer registeredSequence = (Integer) response.get("registeredSequence");

                ObservableList<String> results = FXCollections.observableArrayList(
                        "Patient ID: " + patientId,
                        "Registered Sequence: " + registeredSequence
                );
                resultListView.setItems(results);
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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