package com.fantacalcio.fantaschedina.domain.enums;

public enum LeagueStatus {
    SETUP("In configurazione"),
    ACTIVE("Attiva"),
    CLOSED("Chiusa");

    private final String label;

    LeagueStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}