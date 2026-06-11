package com.firstclub.fc_membership.dto.response;

import com.firstclub.fc_membership.enums.CohortType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private CohortType cohort;
    private LocalDateTime createdAt;
}