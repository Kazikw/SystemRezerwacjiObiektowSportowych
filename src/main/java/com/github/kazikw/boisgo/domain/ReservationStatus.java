package com.github.kazikw.boisgo.domain;

public enum ReservationStatus {
    PENDING("Oczekująca"),
    CONFIRMED("Potwierdzona"),
    CANCELLED("Anulowana"),
    COMPLETED("Zakończona");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}