package com.firstclub.fc_membership.entity;

import com.firstclub.fc_membership.enums.TierType;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_tiers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TierType tierType;

    private String description;

    @Column(nullable = false)
    private Integer tierOrder;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<TierBenefit> benefits = new ArrayList<>();

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<TierCriteria> criteriaList = new ArrayList<>();
}