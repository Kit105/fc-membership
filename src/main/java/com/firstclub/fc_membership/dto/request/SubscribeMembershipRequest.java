package com.firstclub.fc_membership.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscribeMembershipRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Tier ID is required")
    private Long tierId;
}