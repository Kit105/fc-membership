package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.entity.*;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.exception.ActiveMembershipExistsException;
import com.firstclub.fc_membership.exception.MembershipException;
import com.firstclub.fc_membership.exception.ResourceNotFoundException;
import com.firstclub.fc_membership.mapper.MembershipMapper;
import com.firstclub.fc_membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final UserRepository userRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserMembershipRepository membershipRepository;
    private final MembershipMapper mapper;

    @Override
    @Transactional
    public MembershipResponse subscribe(Long userId, SubscribeMembershipRequest request) {
        User user = findUserOrThrow(userId);

        if (membershipRepository.existsByUserIdAndStatus(userId, MembershipStatus.ACTIVE)) {
            throw new ActiveMembershipExistsException(userId);
        }

        MembershipPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", request.getPlanId()));

        MembershipTier tier = tierRepository.findById(request.getTierId())
                .orElseThrow(() -> new ResourceNotFoundException("MembershipTier", request.getTierId()));

        if (!plan.isActive()) {
            throw new MembershipException("This plan is no longer available", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        UserMembership membership = UserMembership.builder()
                .user(user)
                .plan(plan)
                .tier(tier)
                .startDate(now)
                .expiryDate(now.plusDays(plan.getDurationDays()))
                .status(MembershipStatus.ACTIVE)
                .build();

        UserMembership saved = membershipRepository.save(membership);
        log.info("User {} subscribed: plan={}, tier={}, expires={}",
                userId, plan.getPlanType(), tier.getTierType(), saved.getExpiryDate());
        return mapper.toMembershipResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getCurrentMembership(Long userId) {
        findUserOrThrow(userId);
        return mapper.toMembershipResponse(getActiveMembershipOrThrow(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembershipHistory(Long userId) {
        findUserOrThrow(userId);
        return membershipRepository.findByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(mapper::toMembershipResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MembershipResponse upgradeTier(Long userId) {
        UserMembership membership = getActiveMembershipOrThrow(userId);
        TierType current = membership.getTier().getTierType();

        TierType next = getNextTier(current)
                .orElseThrow(() -> new MembershipException(
                        "Already at the highest tier: " + current, HttpStatus.BAD_REQUEST));

        MembershipTier newTier = tierRepository.findByTierType(next)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + next));

        membership.setTier(newTier);
        log.info("User {} upgraded: {} → {}", userId, current, next);
        return mapper.toMembershipResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public MembershipResponse downgradeTier(Long userId) {
        UserMembership membership = getActiveMembershipOrThrow(userId);
        TierType current = membership.getTier().getTierType();

        TierType previous = getPreviousTier(current)
                .orElseThrow(() -> new MembershipException(
                        "Already at the lowest tier: " + current, HttpStatus.BAD_REQUEST));

        MembershipTier newTier = tierRepository.findByTierType(previous)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + previous));

        membership.setTier(newTier);
        log.info("User {} downgraded: {} → {}", userId, current, previous);
        return mapper.toMembershipResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public MembershipResponse cancelMembership(Long userId) {
        UserMembership membership = getActiveMembershipOrThrow(userId);
        membership.setStatus(MembershipStatus.CANCELLED);
        log.info("User {} cancelled membership (id={})", userId, membership.getId());
        return mapper.toMembershipResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public void processExpiredMemberships() {
        List<UserMembership> expired =
                membershipRepository.findExpiredActiveMemberships(LocalDateTime.now());
        if (expired.isEmpty()) return;
        expired.forEach(m -> m.setStatus(MembershipStatus.EXPIRED));
        membershipRepository.saveAll(expired);
        log.info("Marked {} memberships as expired", expired.size());
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private UserMembership getActiveMembershipOrThrow(Long userId) {
        findUserOrThrow(userId);
        return membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipException(
                        "No active membership found for user " + userId, HttpStatus.NOT_FOUND));
    }

    private Optional<TierType> getNextTier(TierType current) {
        return switch (current) {
            case SILVER   -> Optional.of(TierType.GOLD);
            case GOLD     -> Optional.of(TierType.PLATINUM);
            case PLATINUM -> Optional.empty();
        };
    }

    private Optional<TierType> getPreviousTier(TierType current) {
        return switch (current) {
            case PLATINUM -> Optional.of(TierType.GOLD);
            case GOLD     -> Optional.of(TierType.SILVER);
            case SILVER   -> Optional.empty();
        };
    }
}