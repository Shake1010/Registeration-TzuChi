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
