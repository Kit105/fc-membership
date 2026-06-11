package com.firstclub.fc_membership.mapper;

import com.firstclub.fc_membership.dto.response.BenefitResponse;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.dto.response.PlanResponse;
import com.firstclub.fc_membership.dto.response.TierResponse;
import com.firstclub.fc_membership.entity.MembershipPlan;
import com.firstclub.fc_membership.entity.MembershipTier;
import com.firstclub.fc_membership.entity.UserMembership;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class MembershipMapper {

    public MembershipResponse toMembershipResponse(UserMembership membership) {
        LocalDateTime now = LocalDateTime.now();
        return MembershipResponse.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .userName(membership.getUser().getName())
                .plan(toPlanResponse(membership.getPlan()))
                .tier(toTierResponse(membership.getTier()))
                .startDate(membership.getStartDate())
                .expiryDate(membership.getExpiryDate())
                .status(membership.getStatus())
                .expired(now.isAfter(membership.getExpiryDate()))
                .daysUntilExpiry(Math.max(0, ChronoUnit.DAYS.between(now, membership.getExpiryDate())))
                .build();
    }

    public PlanResponse toPlanResponse(MembershipPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .planType(plan.getPlanType())
                .displayName(plan.getPlanType().getDisplayName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .description(plan.getDescription())
                .build();
    }

    public TierResponse toTierResponse(MembershipTier tier) {
        return TierResponse.builder()
                .id(tier.getId())
                .tierType(tier.getTierType())
                .description(tier.getDescription())
                .tierOrder(tier.getTierOrder())
                .benefits(tier.getBenefits().stream()
                        .map(b -> BenefitResponse.builder()
                                .id(b.getId())
                                .benefitType(b.getBenefitType())
                                .value(b.getValue())
                                .description(b.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}