package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.CreateUserRequest;
import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.entity.UserMembership;
import com.firstclub.fc_membership.enums.CohortType;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipPlanRepository;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MembershipExpiryTest {

    @Autowired private UserService userService;
    @Autowired private MembershipService membershipService;
    @Autowired private UserMembershipRepository membershipRepository;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;

    @Test
    void expiredActiveMembershipShouldBeMarkedAsExpired() {
        // Create user and subscribe
        Long userId = createUser();
        Long planId = planRepository.findByPlanType(
                com.firstclub.fc_membership.enums.PlanType.MONTHLY).get().getId();
        Long tierId = tierRepository.findByTierType(TierType.SILVER).get().getId();

        SubscribeMembershipRequest req = new SubscribeMembershipRequest();
        req.setPlanId(planId);
        req.setTierId(tierId);
        membershipService.subscribe(userId, req);

        // Manually backdate the expiry to simulate a membership that ran out
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE).get();
        membership.setExpiryDate(LocalDateTime.now().minusDays(1));
        membershipRepository.save(membership);

        // Run the scheduler job
        membershipService.processExpiredMemberships();

        // Membership should now be EXPIRED not ACTIVE
        UserMembership updated = membershipRepository.findById(membership.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(MembershipStatus.EXPIRED);
    }

    @Test
    void activeMembershipNotYetExpiredShouldBeUntouched() {
        Long userId = createUser();
        Long planId = planRepository.findByPlanType(
                com.firstclub.fc_membership.enums.PlanType.MONTHLY).get().getId();
        Long tierId = tierRepository.findByTierType(TierType.SILVER).get().getId();

        SubscribeMembershipRequest req = new SubscribeMembershipRequest();
        req.setPlanId(planId);
        req.setTierId(tierId);
        membershipService.subscribe(userId, req);

        // Run the job without backdating — membership is still valid
        membershipService.processExpiredMemberships();

        // Should still be ACTIVE
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE).get();
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    private Long createUser() {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Expiry Test User");
        req.setEmail("expiry_" + System.nanoTime() + "@test.com");
        req.setCohort(CohortType.REGULAR);
        return userService.createUser(req).getId();
    }
}