package com.firstclub.fc_membership.entity;

import com.firstclub.fc_membership.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_memberships",
        indexes = {
                @Index(name = "idx_user_status", columnList = "user_id, status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Version
    private Long version;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}