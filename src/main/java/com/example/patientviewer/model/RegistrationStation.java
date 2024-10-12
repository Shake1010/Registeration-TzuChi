package com.example.patientviewer.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class RegistrationStation {
    private Long registeredSequence;
    private String patientId;
    private Integer sectionNumber;
    private LocalDateTime registeredTime;

    // Constructors
    public RegistrationStation() {}

    public RegistrationStation(Long registeredSequence, String patientId, Integer sectionNumber, LocalDateTime registeredTime) {
        this.registeredSequence = registeredSequence;
        this.patientId = patientId;
        this.sectionNumber = sectionNumber;
        this.registeredTime = registeredTime;
    }

    // Getters and Setters
    public Long getRegisteredSequence() { return registeredSequence; }
    public void setRegisteredSequence(Long registeredSequence) { this.registeredSequence = registeredSequence; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Integer getSectionNumber() { return sectionNumber; }
    public void setSectionNumber(Integer sectionNumber) { this.sectionNumber = sectionNumber; }

    public LocalDateTime getRegisteredTime() { return registeredTime; }
    public void setRegisteredTime(LocalDateTime registeredTime) { this.registeredTime = registeredTime; }

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