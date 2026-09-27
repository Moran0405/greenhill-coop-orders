package com.greenhill.coop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test of the weekly ordering flow through the HTTP layer:
 * coordinator sets up catalogue and round, member orders, coordinator
 * sees the summary, and the read-only rule after the cut-off holds.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullWeeklyOrderingFlow() throws Exception {
        // --- Coordinator signs in ---
        MvcResult coordResult = mockMvc.perform(post("/api/auth/coordinator-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"coop-admin-2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("coordinator"))
                .andReturn();
        MockHttpSession coordinator = (MockHttpSession) coordResult.getRequest().getSession();

        // --- Coordinator registers a member household ---
        mockMvc.perform(post("/api/members").session(coordinator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nguyen Household\",\"email\":\"nguyen@test.com\",\"phone\":\"0400\"}"))
                .andExpect(status().isOk());

        // --- Coordinator adds a whole-unit product ---
        MvcResult productResult = mockMvc.perform(post("/api/products").session(coordinator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Free-range Eggs\",\"pricingType\":\"UNIT\",\"price\":7.50}"))
                .andExpect(status().isOk())
                .andReturn();
        long productId = ((Number) com.jayway.jsonpath.JsonPath.read(
                productResult.getResponse().getContentAsString(), "$.id")).longValue();

        // --- Coordinator opens a weekly round ---
        MvcResult roundResult = mockMvc.perform(post("/api/rounds").session(coordinator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Week 1\",\"opensAt\":\"2026-01-05T08:00:00\",\"closesAt\":\"2099-01-11T20:00:00\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long roundId = ((Number) com.jayway.jsonpath.JsonPath.read(
                roundResult.getResponse().getContentAsString(), "$.id")).longValue();

        // --- Member signs in and saves an order ---
        MvcResult memberResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nguyen@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("member"))
                .andReturn();
        MockHttpSession member = (MockHttpSession) memberResult.getRequest().getSession();

        mockMvc.perform(put("/api/rounds/" + roundId + "/my-order").session(member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":\"2\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(15.0));

        // --- Member can update the order before the cut-off ---
        mockMvc.perform(put("/api/rounds/" + roundId + "/my-order").session(member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":\"3\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(22.5));

        // --- Members cannot see other members' data or coordinator summaries ---
        mockMvc.perform(get("/api/rounds/" + roundId + "/summary").session(member))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/members").session(member))
                .andExpect(status().isForbidden());

        // --- Coordinator sees the aggregated wholesale summary ---
        mockMvc.perform(get("/api/rounds/" + roundId + "/summary").session(coordinator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalQuantity").value(3.0))
                .andExpect(jsonPath("$[0].estimatedCost").value(22.5));

        // --- Coordinator closes the round; the order becomes read only ---
        mockMvc.perform(post("/api/rounds/" + roundId + "/close").session(coordinator))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/rounds/" + roundId + "/my-order").session(member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":\"1\"}]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/rounds/" + roundId + "/my-order").session(member))
                .andExpect(status().isBadRequest());

        // --- Member can still VIEW their frozen order ---
        mockMvc.perform(get("/api/rounds/" + roundId + "/my-order").session(member))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editable").value(false));

        // --- Unauthenticated access is rejected ---
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }
}
