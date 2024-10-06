package com.example.patientviewer;

import com.example.patientviewer.model.Row5;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class PatientViewerApp extends Application {

    private TextField patientIdField;
    private ListView<String> resultListView;
    private final String API_URL = "http://localhost:8080/api/register/patientE"; // Update this URL
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Registration");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label patientIdLabel = new Label("Patient ID:");
        grid.add(patientIdLabel, 0, 0);

        patientIdField = new TextField();
        grid.add(patientIdField, 1, 0);

        Button registerButton = new Button("Register Patient");
        registerButton.setOnAction(e -> registerPatient());
        grid.add(registerButton, 1, 1);

        resultListView = new ListView<>();
        grid.add(resultListView, 0, 2, 2, 1);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(grid);

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void registerPatient() {
        String patientId = patientIdField.getText();
        if (patientId.isEmpty()) {
            showAlert("Error", "Patient ID is required");
            return;
        }

        Row5 patient = new Row5();
        patient.setPatientId(patientId);

        try {
            String jsonBody = objectMapper.writeValueAsString(patient);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::updateResultList)
                    .exceptionally(e -> {
                        Platform.runLater(() -> showAlert("Error", getDetailedErrorMessage(e)));
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            showAlert("Error", "Failed to create request: " + e.getMessage());
        }
    }

    private void updateResultList(String responseBody) {
        Platform.runLater(() -> {
            try {
                int registeredSequence = objectMapper.readValue(responseBody, Integer.class);
                ObservableList<String> results = FXCollections.observableArrayList(
                        "Registered Sequence: " + registeredSequence
                );
                resultListView.setItems(results);
                patientIdField.clear();
            } catch (Exception e) {
                showAlert("Error", "Failed to parse response: " + e.getMessage());
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
        StringBuilder message = new StringBuilder("An error occurred while registering the patient:\n");
        Throwable cause = e;
        while (cause != null) {
            message.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
            cause = cause.getCause();
        }
        return message.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

