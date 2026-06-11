package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.TierType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EligibleTierResponse {
    private TierType eligibleTier;    // highest tier they currently qualify for
    private TierType currentTier;     // tier they're on now (null if no active membership)
    private boolean upgradeAvailable; // true if eligibleTier is higher than currentTier
    private String message;           // human-readable summary
}