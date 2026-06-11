package com.firstclub.fc_membership.entity;

import com.firstclub.fc_membership.enums.BenefitType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_benefits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType benefitType;

    @Column(name = "benefit_value", nullable = false)
    private String value;

    private String description;
}