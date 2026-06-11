package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.MembershipStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class MembershipResponse {
    private Long id;
    private Long userId;
    private String userName;
    private PlanResponse plan;
    private TierResponse tier;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private MembershipStatus status;
    private boolean expired;
    private long daysUntilExpiry;
}