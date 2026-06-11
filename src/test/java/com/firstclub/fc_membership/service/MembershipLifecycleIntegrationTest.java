package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.CreateUserRequest;
import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.enums.CohortType;
import com.firstclub.fc_membership.enums.MembershipStatus;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.exception.ActiveMembershipExistsException;
import com.firstclub.fc_membership.repository.MembershipPlanRepository;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import com.firstclub.fc_membership.dto.request.PlaceOrderRequest;
import com.firstclub.fc_membership.dto.response.OrderResponse;
import java.math.BigDecimal;
@SpringBootTest
@Transactional
class MembershipLifecycleIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private MembershipService membershipService;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;
    @Autowired private OrderService orderService;

    private Long userId;
    private Long planId;
    private Long silverTierId;

    @BeforeEach
    void setup() {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Lifecycle User");
        req.setEmail("lifecycle_" + System.nanoTime() + "@test.com");
        req.setCohort(CohortType.REGULAR);
        userId = userService.createUser(req).getId();

        planId = planRepository.findByPlanType(
                com.firstclub.fc_membership.enums.PlanType.MONTHLY).get().getId();
        silverTierId = tierRepository.findByTierType(TierType.SILVER).get().getId();
    }

    @Test
    void subscribeShouldCreateActiveMembershipAtChosenTier() {
        MembershipResponse membership = subscribe();

        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.getTier().getTierType()).isEqualTo(TierType.SILVER);
        assertThat(membership.getExpiryDate()).isAfter(membership.getStartDate());
    }

    @Test
    void subscribingTwiceShouldThrowException() {
        subscribe();

        assertThatThrownBy(this::subscribe)
                .isInstanceOf(ActiveMembershipExistsException.class);
    }

    @Test
    void cancelShouldChangeStatusToCancelled() {
        subscribe();

        MembershipResponse cancelled = membershipService.cancelMembership(userId);

        assertThat(cancelled.getStatus()).isEqualTo(MembershipStatus.CANCELLED);
    }

    @Test
    void upgradeShouldMoveToNextTier() {
        subscribe();

        MembershipResponse upgraded = membershipService.upgradeTier(userId);

        assertThat(upgraded.getTier().getTierType()).isEqualTo(TierType.GOLD);
    }

    @Test
    void upgradeFromPlatinumShouldThrowException() {
        subscribe();
        membershipService.upgradeTier(userId); // SILVER → GOLD
        membershipService.upgradeTier(userId); // GOLD → PLATINUM

        assertThatThrownBy(() -> membershipService.upgradeTier(userId))
                .isInstanceOf(com.firstclub.fc_membership.exception.MembershipException.class)
                .hasMessageContaining("highest tier");
    }

    @Test
    void cancelledMembershipShouldNotAppearAsCurrent() {
        subscribe();
        membershipService.cancelMembership(userId);

        assertThatThrownBy(() -> membershipService.getCurrentMembership(userId))
                .isInstanceOf(com.firstclub.fc_membership.exception.MembershipException.class);
    }

    @Test
    void subscribeWithoutTierIdShouldAutoAssignBestEligibleTier() {
        // Don't pass tierId — system should auto-assign
        SubscribeMembershipRequest req = new SubscribeMembershipRequest();
        req.setPlanId(planId);
        // tierId intentionally omitted

        MembershipResponse result = membershipService.subscribe(userId, req);

        // New user with no orders → should land on SILVER automatically
        assertThat(result.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(result.getTier().getTierType()).isEqualTo(TierType.SILVER);
    }

    @Test
    void placingEnoughOrdersShouldAutoUpgradeTierThroughOrderService() {
        // Subscribe first
        subscribe();

        // Place 5 orders through OrderService (the real production path)
        PlaceOrderRequest orderReq = new PlaceOrderRequest();
        orderReq.setUserId(userId);
        orderReq.setTotalAmount(new BigDecimal("200"));

        OrderResponse lastOrder = null;
        for (int i = 0; i < 5; i++) {
            lastOrder = orderService.placeOrder(orderReq);
        }

        // The 5th order response should show the tier was upgraded
        assertThat(lastOrder.getUpdatedTier()).isEqualTo(TierType.GOLD.name());

        // And the membership itself should reflect the new tier
        MembershipResponse current = membershipService.getCurrentMembership(userId);
        assertThat(current.getTier().getTierType()).isEqualTo(TierType.GOLD);
    }

    private MembershipResponse subscribe() {
        SubscribeMembershipRequest req = new SubscribeMembershipRequest();
        req.setPlanId(planId);
        req.setTierId(silverTierId);
        return membershipService.subscribe(userId, req);
    }
}