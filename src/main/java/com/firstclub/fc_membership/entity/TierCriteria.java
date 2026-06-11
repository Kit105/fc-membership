package com.firstclub.fc_membership.entity;

import com.firstclub.fc_membership.enums.CohortType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tier_criteria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    private Integer minOrders;

    private BigDecimal minMonthlyOrderValue;

    @Enumerated(EnumType.STRING)
    private CohortType requiredCohort;
}