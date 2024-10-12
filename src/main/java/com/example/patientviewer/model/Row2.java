package com.example.patientviewer.model;

import java.util.Objects;

public class Row2 {
    private String patientId;
    private Character patientCategory;
    private Integer patientNumber;
    private Integer sectionNumber;
    private Boolean inQueue;
    private Priority priority;

    public enum Priority {
        LOW, MID, HIGH
    }

    // Constructors
    public Row2() {}

    public Row2(String patientId, Character patientCategory, Integer patientNumber, Integer sectionNumber, Boolean inQueue, Priority priority) {
        this.patientId = patientId;
        this.patientCategory = patientCategory;
        this.patientNumber = patientNumber;
        this.sectionNumber = sectionNumber;
        this.inQueue = inQueue;
        this.priority = priority;
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Character getPatientCategory() { return patientCategory; }
    public void setPatientCategory(Character patientCategory) { this.patientCategory = patientCategory; }

    public Integer getPatientNumber() { return patientNumber; }
    public void setPatientNumber(Integer patientNumber) { this.patientNumber = patientNumber; }

    public Integer getSectionNumber() { return sectionNumber; }
    public void setSectionNumber(Integer sectionNumber) { this.sectionNumber = sectionNumber; }

    public Boolean getInQueue() { return inQueue; }
    public void setInQueue(Boolean inQueue) { this.inQueue = inQueue; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "Row2{" +
                "patientId='" + patientId + '\'' +
                ", patientCategory=" + patientCategory +
                ", patientNumber=" + patientNumber +
                ", sectionNumber=" + sectionNumber +
                ", inQueue=" + inQueue +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row2 row2 = (Row2) o;
        return Objects.equals(patientId, row2.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId);
    }
}
