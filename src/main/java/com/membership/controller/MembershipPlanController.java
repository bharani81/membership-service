package com.membership.controller;

import com.membership.dto.response.MembershipPlanResponse;
import com.membership.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for membership plan operations.
 */
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Membership Plans", description = "Retrieve available membership plans")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    @GetMapping
    @Operation(summary = "List all active plans", description = "Returns all active membership plans ordered by duration")
    @ApiResponse(responseCode = "200", description = "Plans retrieved successfully")
    public ResponseEntity<List<MembershipPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get plan by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan found"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<MembershipPlanResponse> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }
}
