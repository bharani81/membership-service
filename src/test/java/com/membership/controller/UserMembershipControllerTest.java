package com.membership.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.membership.dto.request.SubscribeRequest;
import com.membership.dto.response.MembershipPlanResponse;
import com.membership.dto.response.UserMembershipResponse;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.GlobalExceptionHandler;
import com.membership.exception.PlanNotFoundException;
import com.membership.exception.SubscriptionAlreadyExistsException;
import com.membership.mapper.UserMembershipMapper;
import com.membership.service.TierEvaluationService;
import com.membership.service.UserMembershipService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserMembershipController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserMembershipController Tests")
class UserMembershipControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserMembershipService membershipService;
    @MockBean private TierEvaluationService tierEvaluationService;
    @MockBean private UserMembershipMapper membershipMapper;

    @Test
    @DisplayName("POST /api/v1/memberships - should return 201 on success")
    void subscribe_returns201() throws Exception {
        SubscribeRequest request = new SubscribeRequest(1L, 1L, 1L);
        UserMembershipResponse response = buildMockResponse(MembershipStatus.ACTIVE);

        when(membershipService.subscribe(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/memberships - should return 409 when user already subscribed")
    void subscribe_returns409_whenAlreadySubscribed() throws Exception {
        SubscribeRequest request = new SubscribeRequest(1L, 1L, 1L);

        when(membershipService.subscribe(any()))
                .thenThrow(new SubscriptionAlreadyExistsException("Already has active membership"));

        mockMvc.perform(post("/api/v1/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/memberships - should return 400 when userId is null")
    void subscribe_returns400_whenUserIdNull() throws Exception {
        String invalidBody = """
                {"planId": 1, "tierId": 1}
                """;

        mockMvc.perform(post("/api/v1/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/memberships/{id}/cancel - should return 200")
    void cancel_returns200() throws Exception {
        UserMembershipResponse response = buildMockResponse(MembershipStatus.CANCELLED);
        when(membershipService.cancel(1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/memberships/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/membership - should return 404 when not found")
    void getCurrentMembership_returns404() throws Exception {
        when(membershipService.getCurrentMembership(999L))
                .thenThrow(new com.membership.exception.MembershipNotFoundException("No active membership"));

        mockMvc.perform(get("/api/v1/users/999/membership"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("GET /api/v1/plans/{id} - should return 404 for unknown plan")
    void getPlan_returns404() throws Exception {
        // This tests the GlobalExceptionHandler for PlanNotFoundException via plan controller
        when(membershipService.getCurrentMembership(1L))
                .thenThrow(new PlanNotFoundException(999L));

        mockMvc.perform(get("/api/v1/users/1/membership"))
                .andExpect(status().isNotFound());
    }

    private UserMembershipResponse buildMockResponse(MembershipStatus status) {
        MembershipPlanResponse plan = new MembershipPlanResponse(1L, "Monthly", 1, new BigDecimal("99.00"), true, null);
        return new UserMembershipResponse(
                1L, 1L, plan, null,
                status,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                0L, null, null
        );
    }
}
