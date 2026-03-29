package com.fantacalcio.fantaschedina.domain.enums;

public enum TransactionType {
    INITIAL_DEPOSIT("Deposito iniziale"),
    BET_CHARGE("Schedina"),
    WIN_CREDIT("Vincita"),
    AUTO_CHARGE("Auto-submit"),
    ADMIN_ADJUST("Rettifica admin");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}