package com.membership.controller;

import com.membership.dto.response.MembershipTierResponse;
import com.membership.service.MembershipTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for membership tier operations.
 */
@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
@Tag(name = "Membership Tiers", description = "Retrieve membership tier definitions and benefits")
public class MembershipTierController {

    private final MembershipTierService tierService;

    @GetMapping
    @Operation(summary = "List all active tiers", description = "Returns tiers ordered by priority (Platinum first)")
    @ApiResponse(responseCode = "200", description = "Tiers retrieved successfully")
    public ResponseEntity<List<MembershipTierResponse>> getAllTiers() {
        return ResponseEntity.ok(tierService.getAllActiveTiers());
    }
}
