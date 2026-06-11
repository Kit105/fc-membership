package com.firstclub.fc_membership.enums;

public enum TierType {

    SILVER(1),
    GOLD(2),
    PLATINUM(3);

    private final int order;

    TierType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public boolean isHigherThan(TierType other) {
        return this.order > other.order;
    }

    public boolean isLowerThan(TierType other) {
        return this.order < other.order;
    }

}
