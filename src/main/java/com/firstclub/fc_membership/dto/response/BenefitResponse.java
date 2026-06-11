package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.BenefitType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BenefitResponse {
    private Long id;
    private BenefitType benefitType;
    private String value;
    private String description;
}