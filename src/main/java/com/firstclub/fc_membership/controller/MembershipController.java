package com.firstclub.fc_membership.controller;

import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.ApiResponse;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import com.firstclub.fc_membership.service.MembershipService;
import com.firstclub.fc_membership.service.TierEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/membership")
@RequiredArgsConstructor
@Tag(name = "Memberships", description = "Subscribe, upgrade, downgrade, cancel and track membership")
public class MembershipController {

    private final MembershipService membershipService;
    private final TierEvaluationService tierEvaluationService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe user to a plan and tier")
    public ResponseEntity<ApiResponse<MembershipResponse>> subscribe(
            @PathVariable Long userId,
            @Valid @RequestBody SubscribeMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        membershipService.subscribe(userId, request), "Subscription created"));
    }

    @GetMapping
    @Operation(summary = "Get current active membership with expiry info")
    public ResponseEntity<ApiResponse<MembershipResponse>> getCurrent(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.getCurrentMembership(userId)));
    }

    @GetMapping("/history")
    @Operation(summary = "Get full membership history for the user")
    public ResponseEntity<ApiResponse<List<MembershipResponse>>> getHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.getMembershipHistory(userId)));
    }

    @PutMapping("/upgrade")
    @Operation(summary = "Manually upgrade to next tier — Silver→Gold, Gold→Platinum")
    public ResponseEntity<ApiResponse<MembershipResponse>> upgrade(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.upgradeTier(userId), "Tier upgraded successfully"));
    }

    @PutMapping("/downgrade")
    @Operation(summary = "Manually downgrade to previous tier — Platinum→Gold, Gold→Silver")
    public ResponseEntity<ApiResponse<MembershipResponse>> downgrade(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.downgradeTier(userId), "Tier downgraded"));
    }

    @DeleteMapping("/cancel")
    @Operation(summary = "Cancel the active membership")
    public ResponseEntity<ApiResponse<MembershipResponse>> cancel(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.cancelMembership(userId), "Membership cancelled"));
    }

    @PostMapping("/evaluate-tier")
    @Operation(summary = "Trigger automatic tier evaluation based on order activity")
    public ResponseEntity<ApiResponse<MembershipResponse>> evaluateTier(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                tierEvaluationService.evaluateAndUpdateTier(userId), "Tier evaluation complete"));
    }
}