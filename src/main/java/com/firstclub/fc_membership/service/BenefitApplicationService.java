package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.entity.TierBenefit;
import com.firstclub.fc_membership.entity.UserMembership;
import com.firstclub.fc_membership.enums.BenefitType;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitApplicationService {

    // Orders above this amount qualify for free delivery on SILVER tier
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("500");

    private final UserMembershipRepository membershipRepository;

    /**
     * Applies the user's active tier benefits to the given order amount.
     * If the user has no active membership, no benefits are applied.
     */
    @Transactional(readOnly = true)
    public AppliedBenefits applyBenefits(Long userId, BigDecimal orderAmount) {
        Optional<UserMembership> activeMembership =
                membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);

        if (activeMembership.isEmpty()) {
            log.debug("No active membership for user {} — no benefits applied", userId);
            return AppliedBenefits.none(orderAmount);
        }

        List<TierBenefit> benefits = activeMembership.get().getTier().getBenefits();
        List<String> appliedDescriptions = new ArrayList<>();

        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal deliveryCharge = new BigDecimal("49"); // default delivery charge

        for (TierBenefit benefit : benefits) {

            if (benefit.getBenefitType() == BenefitType.DISCOUNT_PERCENTAGE) {
                BigDecimal pct = new BigDecimal(benefit.getValue());
                discountAmount = orderAmount
                        .multiply(pct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                appliedDescriptions.add(pct + "% membership discount (−₹" + discountAmount + ")");
                log.debug("Applied {}% discount for user {}: −₹{}", pct, userId, discountAmount);
            }

            if (benefit.getBenefitType() == BenefitType.FREE_DELIVERY) {
                boolean freeDeliveryApplies = benefit.getValue().equals("true")
                        && orderAmount.compareTo(FREE_DELIVERY_THRESHOLD) >= 0;
                if (freeDeliveryApplies) {
                    deliveryCharge = BigDecimal.ZERO;
                    appliedDescriptions.add("Free delivery");
                    log.debug("Free delivery applied for user {}", userId);
                }
            }
        }

        BigDecimal finalAmount = orderAmount
                .subtract(discountAmount)
                .add(deliveryCharge)
                .max(BigDecimal.ZERO); // never go below 0

        return AppliedBenefits.builder()
                .originalAmount(orderAmount)
                .discountApplied(discountAmount)
                .deliveryCharge(deliveryCharge)
                .finalAmount(finalAmount)
                .appliedBenefits(appliedDescriptions)
                .build();
    }

    @Getter
    @Builder
    public static class AppliedBenefits {
        private final BigDecimal originalAmount;
        private final BigDecimal discountApplied;
        private final BigDecimal deliveryCharge;
        private final BigDecimal finalAmount;
        private final List<String> appliedBenefits;

        public static AppliedBenefits none(BigDecimal amount) {
            return AppliedBenefits.builder()
                    .originalAmount(amount)
                    .discountApplied(BigDecimal.ZERO)
                    .deliveryCharge(new BigDecimal("49"))
                    .finalAmount(amount.add(new BigDecimal("49")))
                    .appliedBenefits(List.of("No active membership — standard delivery charge applied"))
                    .build();
        }
    }
}