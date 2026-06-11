package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.firstclub.fc_membership.entity.TierCriteria;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyOrderValueStrategy implements TierEligibilityStrategy {

    private final OrderRepository orderRepository;
    private final MembershipTierRepository tierRepository;

    @Override
    public boolean isEligible(Long userId, TierType targetTier) {
        return tierRepository.findByTierType(targetTier)
                .map(tier -> {
                    List<TierCriteria> applicable = tier.getCriteriaList().stream()
                            .filter(c -> c.getMinMonthlyOrderValue() != null)
                            .toList();
                    // No monthly value criterion for this tier → doesn't block it
                    if (applicable.isEmpty()) return true;
                    LocalDateTime startOfMonth = LocalDateTime.now()
                            .withDayOfMonth(1)
                            .withHour(0).withMinute(0).withSecond(0).withNano(0);
                    BigDecimal monthlySpend = orderRepository.sumOrderValueSince(userId, startOfMonth);
                    log.debug("MonthlyValue — user={}, spent={}, required={}",
                            userId, monthlySpend, applicable.get(0).getMinMonthlyOrderValue());
                    return applicable.stream()
                            .anyMatch(c -> monthlySpend.compareTo(c.getMinMonthlyOrderValue()) >= 0);
                })
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}