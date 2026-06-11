package com.firstclub.fc_membership.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private String description;
    private LocalDateTime createdAt;
    private String updatedTier;
}