package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.PlanType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class PlanResponse {
    private Long id;
    private PlanType planType;
    private String displayName;
    private BigDecimal price;
    private Integer durationDays;
    private String description;
}
