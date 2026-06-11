package com.firstclub.fc_membership.config;

import com.firstclub.fc_membership.entity.*;
import com.firstclub.fc_membership.enums.*;
import com.firstclub.fc_membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedPlans();
        seedTiers();
        seedSampleUsers();
        log.info("✅ Seed data ready — http://localhost:8080/swagger-ui.html");
    }

    private void seedPlans() {
        if (planRepository.count() > 0) return;

        planRepository.saveAll(List.of(
                MembershipPlan.builder()
                        .planType(PlanType.MONTHLY)
                        .price(new BigDecimal("199.00"))
                        .durationDays(30)
                        .description("Flexible month-to-month. Cancel anytime.")
                        .active(true).build(),

                MembershipPlan.builder()
                        .planType(PlanType.QUARTERLY)
                        .price(new BigDecimal("499.00"))
                        .durationDays(90)
                        .description("Quarterly plan — save 16% vs monthly.")
                        .active(true).build(),

                MembershipPlan.builder()
                        .planType(PlanType.YEARLY)
                        .price(new BigDecimal("1499.00"))
                        .durationDays(365)
                        .description("Best value — save 37% vs monthly.")
                        .active(true).build()
        ));
        log.info("Seeded 3 plans");
    }

    private void seedTiers() {
        if (tierRepository.count() > 0) return;

        tierRepository.saveAll(List.of(
                buildSilverTier(),
                buildGoldTier(),
                buildPlatinumTier()
        ));
        log.info("Seeded 3 tiers");
    }

    private MembershipTier buildSilverTier() {
        MembershipTier silver = MembershipTier.builder()
                .tierType(TierType.SILVER)
                .description("Entry-level membership with core benefits.")
                .tierOrder(1).build();

        silver.getBenefits().addAll(List.of(
                benefit(silver, BenefitType.FREE_DELIVERY,       "true", "Free delivery on orders above ₹500"),
                benefit(silver, BenefitType.DISCOUNT_PERCENTAGE, "5",    "5% discount on selected items")
        ));
        silver.getCriteriaList().add(
                TierCriteria.builder().tier(silver).minOrders(0).build()
        );
        return silver;
    }

    private MembershipTier buildGoldTier() {
        MembershipTier gold = MembershipTier.builder()
                .tierType(TierType.GOLD)
                .description("Enhanced perks for regular shoppers.")
                .tierOrder(2).build();

        gold.getBenefits().addAll(List.of(
                benefit(gold, BenefitType.FREE_DELIVERY,       "true", "Free delivery on all orders"),
                benefit(gold, BenefitType.DISCOUNT_PERCENTAGE, "10",   "10% discount on all items"),
                benefit(gold, BenefitType.EARLY_SALE_ACCESS,   "true", "24-hour early access to sales")
        ));
        gold.getCriteriaList().addAll(List.of(
                TierCriteria.builder().tier(gold).minOrders(5).build(),
                TierCriteria.builder().tier(gold).minMonthlyOrderValue(new BigDecimal("2000.00")).build()
        ));
        return gold;
    }

    private MembershipTier buildPlatinumTier() {
        MembershipTier platinum = MembershipTier.builder()
                .tierType(TierType.PLATINUM)
                .description("Premium tier with exclusive perks and priority service.")
                .tierOrder(3).build();

        platinum.getBenefits().addAll(List.of(
                benefit(platinum, BenefitType.FREE_DELIVERY,       "true",  "Free delivery on all orders"),
                benefit(platinum, BenefitType.DISCOUNT_PERCENTAGE, "15",    "15% discount on all items"),
                benefit(platinum, BenefitType.PRIORITY_SUPPORT,    "true",  "24/7 priority support"),
                benefit(platinum, BenefitType.EXCLUSIVE_COUPONS,   "500",   "₹500 coupon every month"),
                benefit(platinum, BenefitType.EXCLUSIVE_ACCESS,    "true",  "Early access to product launches")
        ));
        platinum.getCriteriaList().addAll(List.of(
                TierCriteria.builder().tier(platinum).minOrders(15).build(),
                TierCriteria.builder().tier(platinum).minMonthlyOrderValue(new BigDecimal("5000.00")).build(),
                TierCriteria.builder().tier(platinum).requiredCohort(CohortType.PREMIUM_COHORT).build()
        ));
        return platinum;
    }

    private void seedSampleUsers() {
        if (userRepository.count() > 0) return;

        userRepository.saveAll(List.of(
                User.builder().name("Arjun Sharma").email("arjun@firstclub.com").cohort(CohortType.REGULAR).build(),
                User.builder().name("Priya Mehta").email("priya@firstclub.com").cohort(CohortType.PREMIUM_COHORT).build(),
                User.builder().name("Ravi Patel").email("ravi@firstclub.com").cohort(CohortType.CORPORATE).build()
        ));
        log.info("Seeded 3 sample users — IDs will be 1, 2, 3");
    }

    private TierBenefit benefit(MembershipTier tier, BenefitType type,
                                String value, String description) {
        return TierBenefit.builder()
                .tier(tier).benefitType(type)
                .value(value).description(description)
                .build();
    }
}