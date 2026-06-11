package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.CreateUserRequest;
import com.firstclub.fc_membership.dto.request.PlaceOrderRequest;
import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.enums.CohortType;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipPlanRepository;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TierEvaluationIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private MembershipService membershipService;
    @Autowired private OrderService orderService;
    @Autowired private TierEvaluationService tierEvaluationService;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;

    private Long userId;

    @BeforeEach
    void setup() {
        // Create a fresh user for each test
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test User");
        req.setEmail("test_eval_" + System.nanoTime() + "@test.com");
        req.setCohort(CohortType.REGULAR);
        userId = userService.createUser(req).getId();

        // Subscribe to monthly plan at SILVER tier
        Long planId = planRepository.findByPlanType(
                com.firstclub.fc_membership.enums.PlanType.MONTHLY).get().getId();
        Long tierId = tierRepository.findByTierType(TierType.SILVER).get().getId();

        SubscribeMembershipRequest subReq = new SubscribeMembershipRequest();
        subReq.setPlanId(planId);
        subReq.setTierId(tierId);
        membershipService.subscribe(userId, subReq);
    }

    @Test
    void userWithNoOrdersStaysOnSilver() {
        MembershipResponse result = tierEvaluationService.evaluateAndUpdateTier(userId);

        assertThat(result.getTier().getTierType()).isEqualTo(TierType.SILVER);
    }

    @Test
    void userWith5OrdersPromotedToGold() {
        placeOrders(5, new BigDecimal("100"));

        MembershipResponse result = tierEvaluationService.evaluateAndUpdateTier(userId);

        assertThat(result.getTier().getTierType()).isEqualTo(TierType.GOLD);
    }

    @Test
    void userWithOrdersButLowSpendDoesNotReachPlatinum() {
        // 10 orders BUT only ₹100 each = ₹1000 total, needs ₹5000 monthly
        placeOrders(10, new BigDecimal("100"));

        MembershipResponse result = tierEvaluationService.evaluateAndUpdateTier(userId);

        // AND semantics: has orders but NOT enough spend → stays GOLD not PLATINUM
        assertThat(result.getTier().getTierType()).isEqualTo(TierType.GOLD);
    }

    @Test
    void userWith10OrdersAnd5000SpendPromotedToPlatinum() {
        // Meets BOTH PLATINUM criteria
        placeOrders(10, new BigDecimal("500")); // 10 orders × ₹500 = ₹5000

        MembershipResponse result = tierEvaluationService.evaluateAndUpdateTier(userId);

        assertThat(result.getTier().getTierType()).isEqualTo(TierType.PLATINUM);
    }

    private void placeOrders(int count, BigDecimal amount) {
        for (int i = 0; i < count; i++) {
            PlaceOrderRequest req = new PlaceOrderRequest();
            req.setUserId(userId);
            req.setTotalAmount(amount);
            req.setDescription("Test order " + i);
            orderService.placeOrder(req);
        }
    }
}