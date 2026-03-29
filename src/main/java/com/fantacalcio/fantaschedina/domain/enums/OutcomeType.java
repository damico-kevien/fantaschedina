package com.fantacalcio.fantaschedina.domain.enums;

public enum OutcomeType {
    RESULT_1X2("1X2"),
    DOUBLE_CHANCE("Doppia chance"),
    GOAL_NOGOAL("Goal / No goal"),
    OVER_UNDER("Over / Under");

    private final String label;

    OutcomeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}