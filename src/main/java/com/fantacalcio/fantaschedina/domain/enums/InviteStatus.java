package com.fantacalcio.fantaschedina.domain.enums;

public enum InviteStatus {
    PENDING("In attesa"),
    USED("Utilizzato"),
    EXPIRED("Scaduto");

    private final String label;

    InviteStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}