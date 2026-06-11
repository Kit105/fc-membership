package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.response.EligibleTierResponse;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.entity.MembershipTier;
import com.firstclub.fc_membership.entity.UserMembership;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.exception.MembershipException;
import com.firstclub.fc_membership.exception.ResourceNotFoundException;
import com.firstclub.fc_membership.mapper.MembershipMapper;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import com.firstclub.fc_membership.repository.UserRepository;
import com.firstclub.fc_membership.service.strategy.TierEligibilityStrategy;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierEvaluationService {

    private final List<TierEligibilityStrategy> strategies;
    private final UserMembershipRepository membershipRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;
    private final MembershipMapper mapper;

    /**
     * Evaluates and upgrades the user's tier if they qualify for a higher one.
     * @Retryable means if two threads clash on @Version, this retries automatically
     * instead of returning a 409 error to the client.
     */
    @Retryable(
            retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public MembershipResponse evaluateAndUpdateTier(Long userId) {
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipException(
                        "No active membership for user " + userId, HttpStatus.NOT_FOUND));

        TierType current = membership.getTier().getTierType();
        TierType best = determineBestTier(userId);

        if (best.isHigherThan(current)) {
            MembershipTier newTier = tierRepository.findByTierType(best)
                    .orElseThrow(() -> new MembershipException(
                            "Tier config missing: " + best, HttpStatus.INTERNAL_SERVER_ERROR));
            membership.setTier(newTier);
            membershipRepository.save(membership);
            log.info("AUTO-UPGRADE user={} : {} → {}", userId, current, best);
        } else {
            log.debug("No tier change for user={} (current={}, best={})",
                    userId, current, best);
        }

        return mapper.toMembershipResponse(membership);
    }

    /**
     * Read-only check — returns the highest tier the user currently qualifies for
     * WITHOUT updating anything. Used by the eligible-tier endpoint and subscribe auto-assign.
     */
    @Transactional(readOnly = true)
    public EligibleTierResponse getEligibleTierInfo(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        TierType eligibleTier = determineBestTier(userId);

        Optional<UserMembership> activeMembership =
                membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);

        if (activeMembership.isEmpty()) {
            return EligibleTierResponse.builder()
                    .eligibleTier(eligibleTier)
                    .currentTier(null)
                    .upgradeAvailable(false)
                    .message("Subscribe to get started at " + eligibleTier + " tier")
                    .build();
        }

        TierType currentTier = activeMembership.get().getTier().getTierType();
        boolean upgradeAvailable = eligibleTier.isHigherThan(currentTier);

        String message = upgradeAvailable
                ? "Upgrade available: " + currentTier + " → " + eligibleTier
                : "You are at your highest eligible tier: " + currentTier;

        return EligibleTierResponse.builder()
                .eligibleTier(eligibleTier)
                .currentTier(currentTier)
                .upgradeAvailable(upgradeAvailable)
                .message(message)
                .build();
    }

    /**
     * Public wrapper around determineBestTier — used by MembershipService
     * during subscribe to auto-assign the correct starting tier.
     */
    public TierType determineBestEligibleTier(Long userId) {
        return determineBestTier(userId);
    }

    private TierType determineBestTier(Long userId) {
        List<TierType> highToLow =
                Arrays.asList(TierType.PLATINUM, TierType.GOLD, TierType.SILVER);

        List<TierEligibilityStrategy> ordered = strategies.stream()
                .sorted(Comparator.comparingInt(TierEligibilityStrategy::getPriority))
                .toList();

        for (TierType candidate : highToLow) {
            boolean eligible = ordered.stream()
                    .allMatch(s -> s.isEligible(userId, candidate));
            if (eligible) {
                log.debug("User {} qualifies for {}", userId, candidate);
                return candidate;
            }
        }

        return TierType.SILVER;
    }
}