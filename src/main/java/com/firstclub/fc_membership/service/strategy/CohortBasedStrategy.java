package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.firstclub.fc_membership.entity.TierCriteria;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CohortBasedStrategy implements TierEligibilityStrategy {

    private final UserRepository userRepository;
    private final MembershipTierRepository tierRepository;

    @Override
    public boolean isEligible(Long userId, TierType targetTier) {
        return userRepository.findById(userId)
                .flatMap(user -> tierRepository.findByTierType(targetTier)
                        .map(tier -> {
                            List<TierCriteria> applicable = tier.getCriteriaList().stream()
                                    .filter(c -> c.getRequiredCohort() != null)
                                    .toList();
                            // No cohort criterion for this tier → doesn't block it
                            if (applicable.isEmpty()) return true;
                            log.debug("Cohort — user={}, cohort={}, required={}",
                                    userId, user.getCohort(), applicable.get(0).getRequiredCohort());
                            return applicable.stream()
                                    .anyMatch(c -> user.getCohort() == c.getRequiredCohort());
                        }))
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 3;
    }
}