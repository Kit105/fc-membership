package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyOrderValueStrategy implements TierEligibilityStrategy {

    private final OrderRepository orderRepository;
    private final MembershipTierRepository tierRepository;

    @Override
    public boolean isEligible(Long userId, TierType targetTier) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        BigDecimal monthlySpend = orderRepository.sumOrderValueSince(userId, startOfMonth);

        return tierRepository.findByTierType(targetTier)
                .map(tier -> tier.getCriteriaList().stream()
                        .filter(c -> c.getMinMonthlyOrderValue() != null)
                        .anyMatch(c -> {
                            log.debug("MonthlyValue check — user={}, spent={}, required={}",
                                    userId, monthlySpend, c.getMinMonthlyOrderValue());
                            return monthlySpend.compareTo(c.getMinMonthlyOrderValue()) >= 0;
                        }))
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}