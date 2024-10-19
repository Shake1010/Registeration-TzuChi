package com.example.patientviewer.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Row6 {
    private String patientId;
    private Integer patientNumber;
    private LocalDateTime registeredTime;
    private Boolean inQueue;


    @Override
    public String toString() {
        return "Row6{" +
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
        Row6 row6 = (Row6) o;
        return Objects.equals(patientId, row6.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId);
    }
}
