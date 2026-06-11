package com.firstclub.fc_membership.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.firstclub.fc_membership.enums.BenefitType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal originalAmount;    // what the user entered
    private BigDecimal discountApplied;   // amount saved from tier discount
    private BigDecimal deliveryCharge;    // 0 if free delivery applies
    private BigDecimal finalAmount;       // what they actually pay
    private String description;
    private LocalDateTime createdAt;
    private List<String> appliedBenefits; // human-readable list of what was applied
    private String updatedTier;
}