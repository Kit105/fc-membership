package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.CreateUserRequest;
import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.enums.CohortType;
import com.firstclub.fc_membership.enums.TierType;
import com.firstclub.fc_membership.repository.MembershipPlanRepository;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
import com.firstclub.fc_membership.repository.UserMembershipRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// Note: No @Transactional here — each service call must commit independently
// so threads can see each other's changes
@SpringBootTest
class MembershipConcurrencyTest {

    @Autowired private UserService userService;
    @Autowired private MembershipService membershipService;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;
    @Autowired private UserMembershipRepository membershipRepository;

    @Test
    void concurrentUpgradesShouldLeaveConsistentState() throws InterruptedException {
        // Setup: create user and subscribe (these commits must happen before threads start)
        Long userId = createUserAndSubscribe();

        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // all threads wait here until released together
                    membershipService.upgradeTier(userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // OptimisticLockException, "already at highest tier", etc. — all acceptable
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all threads at once
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Key assertion: the final membership state is consistent — never corrupted
        MembershipResponse current = membershipService.getCurrentMembership(userId);
        assertThat(current.getTier().getTierType())
                .isIn(TierType.SILVER, TierType.GOLD, TierType.PLATINUM);

        // At least one thread succeeded — data was actually written
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);

        // Total threads = successes + failures (no thread hung or disappeared)
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);

        System.out.println("Successes: " + successCount.get()
                + " | Failures: " + failureCount.get()
                + " | Final tier: " + current.getTier().getTierType());
    }

    private Long createUserAndSubscribe() {
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setName("Concurrency User");
        userReq.setEmail("concurrency_" + System.nanoTime() + "@test.com");
        userReq.setCohort(CohortType.REGULAR);
        Long userId = userService.createUser(userReq).getId();

        Long planId = planRepository.findByPlanType(
                com.firstclub.fc_membership.enums.PlanType.MONTHLY).get().getId();
        Long tierId = tierRepository.findByTierType(TierType.SILVER).get().getId();

        SubscribeMembershipRequest subReq = new SubscribeMembershipRequest();
        subReq.setPlanId(planId);
        subReq.setTierId(tierId);
        membershipService.subscribe(userId, subReq);

        return userId;
    }
}