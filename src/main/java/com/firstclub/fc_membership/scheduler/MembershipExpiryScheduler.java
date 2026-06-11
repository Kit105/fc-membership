package com.firstclub.fc_membership.scheduler;

import com.firstclub.fc_membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final MembershipService membershipService;

    @Scheduled(fixedRateString = "${membership.expiry.check-interval-ms:3600000}")
    public void checkAndExpireMemberships() {
        log.debug("Running membership expiry check...");
        membershipService.processExpiredMemberships();
    }
}