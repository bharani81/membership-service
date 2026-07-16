package com.membership.controller;

import com.membership.dto.request.DowngradeRequest;
import com.membership.dto.request.SubscribeRequest;
import com.membership.dto.request.TierEvaluationRequest;
import com.membership.dto.request.UpgradeRequest;
import com.membership.dto.response.MembershipStatusResponse;
import com.membership.dto.response.TierEvaluationResponse;
import com.membership.dto.response.UserMembershipResponse;
import com.membership.service.TierEvaluationService;
import com.membership.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user membership lifecycle operations.
 *
 * <p>Endpoints cover the full membership lifecycle:
 * subscribe → upgrade/downgrade → cancel, plus status and tier evaluation.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "User Memberships", description = "Manage user membership subscriptions and lifecycle")
public class UserMembershipController {

    private final UserMembershipService membershipService;
    private final TierEvaluationService tierEvaluationService;

    // ========================
    // Membership CRUD
    // ========================

    @PostMapping("/api/v1/memberships")
    @Operation(summary = "Subscribe", description = "Create a new membership subscription for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subscription created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "User already has an active membership")
    })
    public ResponseEntity<UserMembershipResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.subscribe(request));
    }

    @PutMapping("/api/v1/memberships/{id}/upgrade")
    @Operation(summary = "Upgrade membership", description = "Upgrade to a higher-priority tier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership upgraded"),
            @ApiResponse(responseCode = "404", description = "Membership not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent modification conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid upgrade - target tier not higher")
    })
    public ResponseEntity<UserMembershipResponse> upgrade(
            @PathVariable Long id,
            @Valid @RequestBody UpgradeRequest request) {
        return ResponseEntity.ok(membershipService.upgrade(id, request));
    }

    @PutMapping("/api/v1/memberships/{id}/downgrade")
    @Operation(summary = "Downgrade membership", description = "Downgrade to a lower-priority tier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership downgraded"),
            @ApiResponse(responseCode = "404", description = "Membership not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent modification conflict"),
            @ApiResponse(responseCode = "422", description = "Invalid downgrade - target tier not lower")
    })
    public ResponseEntity<UserMembershipResponse> downgrade(
            @PathVariable Long id,
            @Valid @RequestBody DowngradeRequest request) {
        return ResponseEntity.ok(membershipService.downgrade(id, request));
    }

    @PutMapping("/api/v1/memberships/{id}/cancel")
    @Operation(summary = "Cancel membership")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership cancelled"),
            @ApiResponse(responseCode = "404", description = "Membership not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent modification or already cancelled")
    })
    public ResponseEntity<UserMembershipResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.cancel(id));
    }

    // ========================
    // User-scoped queries
    // ========================

    @GetMapping("/api/v1/users/{userId}/membership")
    @Operation(summary = "Get current membership", description = "Returns the user's active membership with full details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership found"),
            @ApiResponse(responseCode = "404", description = "No active membership for user")
    })
    public ResponseEntity<UserMembershipResponse> getCurrentMembership(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipService.getCurrentMembership(userId));
    }

    @GetMapping("/api/v1/users/{userId}/membership/status")
    @Operation(summary = "Get membership status", description = "Returns expiry status with days remaining and expiry warning")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved"),
            @ApiResponse(responseCode = "404", description = "No active membership for user")
    })
    public ResponseEntity<MembershipStatusResponse> getMembershipStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipService.getMembershipStatus(userId));
    }

    @PostMapping("/api/v1/users/{userId}/tier/evaluate")
    @Operation(summary = "Evaluate tier eligibility",
            description = "Runs the Strategy Pattern engine to determine which tier the user qualifies for")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation complete (tierFound may be false if no tier qualifies)"),
            @ApiResponse(responseCode = "400", description = "Invalid evaluation parameters")
    })
    public ResponseEntity<TierEvaluationResponse> evaluateTier(
            @PathVariable Long userId,
            @Valid @RequestBody TierEvaluationRequest request) {
        return ResponseEntity.ok(tierEvaluationService.evaluateTier(userId, request));
    }
}
