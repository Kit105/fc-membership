package com.firstclub.fc_membership.repository;

import com.firstclub.fc_membership.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.user.id = :userId AND o.createdAt >= :since")
    BigDecimal sumOrderValueSince(@Param("userId") Long userId,
                                  @Param("since") LocalDateTime since);
}