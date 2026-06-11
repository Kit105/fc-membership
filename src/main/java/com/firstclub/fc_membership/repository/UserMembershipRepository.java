package com.firstclub.fc_membership.repository;

import com.firstclub.fc_membership.entity.UserMembership;
import com.firstclub.fc_membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    Optional<UserMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    List<UserMembership> findByUserIdOrderByStartDateDesc(Long userId);

    boolean existsByUserIdAndStatus(Long userId, MembershipStatus status);

    @Query("SELECT m FROM UserMembership m WHERE m.status = 'ACTIVE' AND m.expiryDate < :now")
    List<UserMembership> findExpiredActiveMemberships(@Param("now") LocalDateTime now);
}