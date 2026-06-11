package com.firstclub.fc_membership.entity;

import com.firstclub.fc_membership.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType planType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationDays;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
