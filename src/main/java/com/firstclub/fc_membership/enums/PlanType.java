package com.firstclub.fc_membership.enums;

public enum PlanType {

    MONTHLY(30, "Monthly Plan"),
    QUARTERLY(90, "Quarterly Plan"),
    YEARLY(365, "Yearly Plan");

    private final int durationDays;
    private final String displayName;

    PlanType(int durationDays, String displayName) {
        this.durationDays = durationDays;
        this.displayName = displayName;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public String getDisplayName() {
        return displayName;
    }

}
