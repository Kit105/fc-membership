package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.TierType;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TierResponse {
    private Long id;
    private TierType tierType;
    private String description;
    private Integer tierOrder;
    private List<BenefitResponse> benefits;
}