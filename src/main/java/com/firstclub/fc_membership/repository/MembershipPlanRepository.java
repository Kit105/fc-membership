package com.firstclub.fc_membership.repository;

import com.firstclub.fc_membership.entity.MembershipPlan;
import com.firstclub.fc_membership.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    Optional<MembershipPlan> findByPlanType(PlanType planType);

    List<MembershipPlan> findByActiveTrueOrderByPriceAsc();
}