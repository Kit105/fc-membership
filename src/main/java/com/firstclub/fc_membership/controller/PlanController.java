package com.firstclub.fc_membership.controller;

import com.firstclub.fc_membership.dto.response.ApiResponse;
import com.firstclub.fc_membership.dto.response.PlanResponse;
import com.firstclub.fc_membership.mapper.MembershipMapper;
import com.firstclub.fc_membership.repository.MembershipPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Browse available subscription plans")
public class PlanController {

    private final MembershipPlanRepository planRepository;
    private final MembershipMapper mapper;

    @GetMapping
    @Operation(summary = "List all active plans — Monthly, Quarterly, Yearly")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAllPlans() {
        List<PlanResponse> plans = planRepository.findByActiveTrueOrderByPriceAsc()
                .stream()
                .map(mapper::toPlanResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}