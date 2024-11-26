module com.example.patientviewer {
    requires javafx.controls;

    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires javafx.fxml;


    // Add these if you need them
    requires transitive javafx.graphics;
    requires transitive javafx.base;

    exports com.example.patientviewer;
    exports com.example.patientviewer.model;

    opens com.example.patientviewer to javafx.fxml;
    opens com.example.patientviewer.model to javafx.fxml, com.fasterxml.jackson.databind;
}