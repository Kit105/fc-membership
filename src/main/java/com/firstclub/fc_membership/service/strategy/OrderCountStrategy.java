package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.firstclub.fc_membership.entity.TierCriteria;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCountStrategy implements TierEligibilityStrategy {

    private final OrderRepository orderRepository;
    private final MembershipTierRepository tierRepository;

    @Override
    public boolean isEligible(Long userId, TierType targetTier) {
        return tierRepository.findByTierType(targetTier)
                .map(tier -> {
                    List<TierCriteria> applicable = tier.getCriteriaList().stream()
                            .filter(c -> c.getMinOrders() != null)
                            .toList();
                    // No minOrders criterion for this tier → doesn't block it
                    if (applicable.isEmpty()) return true;
                    long orderCount = orderRepository.countByUserId(userId);
                    log.debug("OrderCount — user={}, orders={}, required={}",
                            userId, orderCount, applicable.get(0).getMinOrders());
                    return applicable.stream().anyMatch(c -> orderCount >= c.getMinOrders());
                })
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}