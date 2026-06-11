package com.firstclub.fc_membership.dto.request;

import com.firstclub.fc_membership.enums.CohortType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotNull(message = "Cohort is required")
    private CohortType cohort;
}