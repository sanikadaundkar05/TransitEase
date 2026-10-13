package com.transitease.controller;

import com.transitease.dto.RouteRequest;
import com.transitease.dto.RouteResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private UserRepository userRepository;

    private String managerToken;
    private String passengerToken;
    private RouteResponse sampleRoute;

    @BeforeEach
    void setUp() {
        User manager = new User();
        manager.setId(10L);
        manager.setFullName("Route Manager");
        manager.setEmail("manager@example.com");
        manager.setRole(Role.ROUTE_FARE_MANAGER);

        User passenger = new User();
        passenger.setId(20L);
        passenger.setFullName("Passenger");
        passenger.setEmail("passenger@example.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        managerToken = jwtService.generateToken(manager);
        passengerToken = jwtService.generateToken(passenger);

        sampleRoute = new RouteResponse(1L, "101", "Station A", "Station B", "Express route", true, 25.0, 15.5, "Stop 1, Stop 2");
    }

    @Test
    void testGetAllRoutes_Public_Success() throws Exception {
        when(routeService.getAllRoutes()).thenReturn(List.of(sampleRoute));

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeNumber").value("101"))
                .andExpect(jsonPath("$[0].fare").value(25.0));
    }

    @Test
    void testGetRouteById_Public_Success() throws Exception {
        when(routeService.getRouteById(1L)).thenReturn(sampleRoute);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeNumber").value("101"))
                .andExpect(jsonPath("$.distanceKm").value(15.5));
    }

    @Test
    void testCreateRoute_AsManager_Success() throws Exception {
        when(routeService.createRoute(any(RouteRequest.class))).thenReturn(sampleRoute);

        String jsonBody = """
                {
                    "routeNumber": "101",
                    "source": "Station A",
                    "destination": "Station B",
                    "description": "Express route",
                    "active": true,
                    "fare": 25.0,
                    "distanceKm": 15.5,
                    "stops": "Stop 1, Stop 2"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeNumber").value("101"))
                .andExpect(jsonPath("$.fare").value(25.0));
    }

    @Test
    void testUpdateRoute_AsManager_Success() throws Exception {
        when(routeService.updateRoute(eq(1L), any(RouteRequest.class))).thenReturn(sampleRoute);

        String jsonBody = """
                {
                    "routeNumber": "101",
                    "source": "Station A",
                    "destination": "Station B",
                    "description": "Express route",
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/routes/1")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeNumber").value("101"));
    }

    @Test
    void testDeleteRoute_AsManager_Success() throws Exception {
        doNothing().when(routeService).deleteRoute(1L);

        mockMvc.perform(delete("/api/routes/1")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testToggleRouteStatus_AsManager_Success() throws Exception {
        sampleRoute.setActive(false);
        when(routeService.toggleRouteActive(1L)).thenReturn(sampleRoute);

        mockMvc.perform(patch("/api/routes/1/toggle-status")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void testCreateRoute_AsPassenger_ReturnsForbidden() throws Exception {
        String jsonBody = """
                {
                    "routeNumber": "102",
                    "source": "A",
                    "destination": "B"
                }
                """;

        mockMvc.perform(post("/api/routes")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }
}
