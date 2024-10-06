module com.example.patientviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports com.example.patientviewer;
    opens com.example.patientviewer.model to com.fasterxml.jackson.databind;
}