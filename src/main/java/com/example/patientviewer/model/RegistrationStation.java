package com.example.patientviewer.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class RegistrationStation {
    private Long registeredSequence;
    private String patientId;
    private Integer sectionNumber;
    private LocalDateTime registeredTime;


    @Override
    public String toString() {
        return "RegistrationStation{" +
                "registeredSequence=" + registeredSequence +
                ", patientId='" + patientId + '\'' +
                ", sectionNumber=" + sectionNumber +
                ", registeredTime=" + registeredTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationStation that = (RegistrationStation) o;
        return Objects.equals(registeredSequence, that.registeredSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registeredSequence);
    }
}