package com.firstclub.fc_membership.repository;

import com.firstclub.fc_membership.entity.MembershipTier;
import com.firstclub.fc_membership.enums.TierType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    Optional<MembershipTier> findByTierType(TierType tierType);

    List<MembershipTier> findAllByOrderByTierOrderAsc();
}