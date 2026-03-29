package com.fantacalcio.fantaschedina.domain.enums;

public enum BetSlipStatus {
    PENDING("In attesa"),
    WON("Vinta"),
    LOST("Persa"),
    VOID("Nulla");

    private final String label;

    BetSlipStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}