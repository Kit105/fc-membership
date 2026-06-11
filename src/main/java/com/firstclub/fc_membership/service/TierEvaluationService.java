package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.entity.MembershipTier;
import com.firstclub.fc_membership.entity.UserMembership;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.exception.MembershipException;
import com.firstclub.fc_membership.mapper.MembershipMapper;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import com.firstclub.fc_membership.service.strategy.TierEligibilityStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TierEvaluationService {

    private final List<TierEligibilityStrategy> strategies;
    private final UserMembershipRepository membershipRepository;
    private final MembershipTierRepository tierRepository;
    private final MembershipMapper mapper;

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
            log.debug("No tier change for user={} (current={}, best={})", userId, current, best);
        }

        return mapper.toMembershipResponse(membership);
    }

    private TierType determineBestTier(Long userId) {
        List<TierType> highToLow = Arrays.asList(TierType.PLATINUM, TierType.GOLD, TierType.SILVER);

        List<TierEligibilityStrategy> ordered = strategies.stream()
                .sorted(Comparator.comparingInt(TierEligibilityStrategy::getPriority))
                .toList();

        for (TierType candidate : highToLow) {
            boolean eligible = ordered.stream()
                    .anyMatch(s -> s.isEligible(userId, candidate));
            if (eligible) {
                log.debug("User {} qualifies for {}", userId, candidate);
                return candidate;
            }
        }

        return TierType.SILVER;
    }
}