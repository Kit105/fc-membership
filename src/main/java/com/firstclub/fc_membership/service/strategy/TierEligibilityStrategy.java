package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;

public interface TierEligibilityStrategy {

    boolean isEligible(Long userId, TierType targetTier);

    int getPriority(); // lower = runs first
}