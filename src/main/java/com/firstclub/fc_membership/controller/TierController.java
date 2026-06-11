package com.firstclub.fc_membership.controller;

import com.firstclub.fc_membership.dto.response.ApiResponse;
import com.firstclub.fc_membership.dto.response.TierResponse;
import com.firstclub.fc_membership.mapper.MembershipMapper;
import com.firstclub.fc_membership.repository.MembershipTierRepository;
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
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@Tag(name = "Tiers", description = "Browse tiers and their benefits")
public class TierController {

    private final MembershipTierRepository tierRepository;
    private final MembershipMapper mapper;

    @GetMapping
    @Operation(summary = "List all tiers with benefits — Silver, Gold, Platinum")
    public ResponseEntity<ApiResponse<List<TierResponse>>> getAllTiers() {
        List<TierResponse> tiers = tierRepository.findAllByOrderByTierOrderAsc()
                .stream()
                .map(mapper::toTierResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(tiers));
    }
}