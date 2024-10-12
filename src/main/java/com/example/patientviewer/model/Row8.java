package com.example.patientviewer.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Row8 {
    private String patientId;
    private Integer patientNumber;
    private LocalDateTime registeredTime;
    private Boolean inQueue;

    // Constructors
    public Row8() {}

    public Row8(String patientId, Integer patientNumber, LocalDateTime registeredTime, Boolean inQueue) {
        this.patientId = patientId;
        this.patientNumber = patientNumber;
        this.registeredTime = registeredTime;
        this.inQueue = inQueue;
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Integer getPatientNumber() { return patientNumber; }
    public void setPatientNumber(Integer patientNumber) { this.patientNumber = patientNumber; }

    public LocalDateTime getRegisteredTime() { return registeredTime; }
    public void setRegisteredTime(LocalDateTime registeredTime) { this.registeredTime = registeredTime; }

    public Boolean getInQueue() { return inQueue; }
    public void setInQueue(Boolean inQueue) { this.inQueue = inQueue; }

    @Override
    public String toString() {
        return "Row8{" +
                "patientId='" + patientId + '\'' +
                ", patientNumber=" + patientNumber +
                ", registeredTime=" + registeredTime +
                ", inQueue=" + inQueue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row8 row8 = (Row8) o;
        return Objects.equals(patientId, row8.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId);
    }
}
