package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCountStrategy implements TierEligibilityStrategy {

    private final OrderRepository orderRepository;
    private final MembershipTierRepository tierRepository;

    @Override
    public boolean isEligible(Long userId, TierType targetTier) {
        return tierRepository.findByTierType(targetTier)
                .map(tier -> tier.getCriteriaList().stream()
                        .filter(c -> c.getMinOrders() != null)
                        .anyMatch(c -> {
                            long orderCount = orderRepository.countByUserId(userId);
                            log.debug("OrderCount check — user={}, orders={}, required={}",
                                    userId, orderCount, c.getMinOrders());
                            return orderCount >= c.getMinOrders();
                        }))
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}