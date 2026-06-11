package com.firstclub.fc_membership.service.strategy;

import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
                        .map(tier -> tier.getCriteriaList().stream()
                                .filter(c -> c.getRequiredCohort() != null)
                                .anyMatch(c -> {
                                    log.debug("Cohort check — user={}, cohort={}, required={}",
                                            userId, user.getCohort(), c.getRequiredCohort());
                                    return user.getCohort() == c.getRequiredCohort();
                                })))
                .orElse(false);
    }

    @Override
    public int getPriority() {
        return 3;
    }
}