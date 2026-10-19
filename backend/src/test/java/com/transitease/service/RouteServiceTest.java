package com.transitease.service;

import com.transitease.dto.RouteRequest;
import com.transitease.dto.RouteResponse;
import com.transitease.entity.Route;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    private Route sampleRoute;
    private RouteRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRoute = new Route();
        sampleRoute.setId(1L);
        sampleRoute.setRouteNumber("101");
        sampleRoute.setSource("Swargate");
        sampleRoute.setDestination("Kothrud Depot");
        sampleRoute.setDescription("Swargate to Kothrud via Deccan");
        sampleRoute.setActive(true);
        sampleRoute.setFare(25.0);
        sampleRoute.setDistanceKm(12.5);
        sampleRoute.setStops("Swargate, Deccan Gymkhana, Paud Road, Kothrud Depot");

        sampleRequest = new RouteRequest();
        sampleRequest.setRouteNumber("101");
        sampleRequest.setSource("Swargate");
        sampleRequest.setDestination("Kothrud Depot");
        sampleRequest.setDescription("Swargate to Kothrud via Deccan");
        sampleRequest.setActive(true);
        sampleRequest.setFare(25.0);
        sampleRequest.setDistanceKm(12.5);
        sampleRequest.setStops("Swargate, Deccan Gymkhana, Paud Road, Kothrud Depot");
    }

    @Nested
    @DisplayName("Normal Cases (Happy Path)")
    class NormalCases {

        @Test
        @DisplayName("getAllRoutes returns list of mapped route responses")
        void testGetAllRoutes() {
            when(routeRepository.findAll()).thenReturn(List.of(sampleRoute));

            List<RouteResponse> results = routeService.getAllRoutes();

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("101", results.get(0).getRouteNumber());
            assertEquals("Swargate", results.get(0).getSource());
            assertEquals("Kothrud Depot", results.get(0).getDestination());
            assertEquals(25.0, results.get(0).getFare());
            verify(routeRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("getRouteById returns correct route response")
        void testGetRouteById_Success() {
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));

            RouteResponse result = routeService.getRouteById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("101", result.getRouteNumber());
            assertTrue(result.getActive());
            verify(routeRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("createRoute persists new route successfully")
        void testCreateRoute_Success() {
            when(routeRepository.existsByRouteNumber("101")).thenReturn(false);
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
                Route r = invocation.getArgument(0);
                r.setId(10L);
                return r;
            });

            RouteResponse result = routeService.createRoute(sampleRequest);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("101", result.getRouteNumber());
            assertEquals("Swargate", result.getSource());
            assertTrue(result.getActive());
            verify(routeRepository, times(1)).existsByRouteNumber("101");
            verify(routeRepository, times(1)).save(any(Route.class));
        }

        @Test
        @DisplayName("updateRoute modifies and saves route")
        void testUpdateRoute_Success() {
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            when(routeRepository.existsByRouteNumberAndIdNot("101", 1L)).thenReturn(false);
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            sampleRequest.setFare(30.0);
            sampleRequest.setDestination("Bavdhan");

            RouteResponse result = routeService.updateRoute(1L, sampleRequest);

            assertNotNull(result);
            assertEquals(30.0, result.getFare());
            assertEquals("Bavdhan", result.getDestination());
            verify(routeRepository, times(1)).save(sampleRoute);
        }

        @Test
        @DisplayName("deleteRoute removes route from repository")
        void testDeleteRoute_Success() {
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            doNothing().when(routeRepository).delete(sampleRoute);

            assertDoesNotThrow(() -> routeService.deleteRoute(1L));

            verify(routeRepository, times(1)).findById(1L);
            verify(routeRepository, times(1)).delete(sampleRoute);
        }

        @Test
        @DisplayName("toggleRouteActive switches active from true to false")
        void testToggleRouteActive_TrueToFalse() {
            sampleRoute.setActive(true);
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RouteResponse result = routeService.toggleRouteActive(1L);

            assertNotNull(result);
            assertFalse(result.getActive());
            verify(routeRepository, times(1)).save(sampleRoute);
        }
    }

    @Nested
    @DisplayName("Invalid Inputs & Exception Cases")
    class InvalidInputCases {

        @Test
        @DisplayName("getRouteById with non-existent id throws ResourceNotFoundException")
        void testGetRouteById_NotFound() {
            when(routeRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> routeService.getRouteById(999L));
            assertTrue(ex.getMessage().contains("Route not found with id: 999"));
        }

        @Test
        @DisplayName("createRoute with existing route number throws RuntimeException")
        void testCreateRoute_DuplicateRouteNumber() {
            when(routeRepository.existsByRouteNumber("101")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> routeService.createRoute(sampleRequest));
            assertEquals("Route number already exists", ex.getMessage());
            verify(routeRepository, never()).save(any(Route.class));
        }

        @Test
        @DisplayName("updateRoute with non-existent id throws ResourceNotFoundException")
        void testUpdateRoute_NotFound() {
            when(routeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> routeService.updateRoute(999L, sampleRequest));
            verify(routeRepository, never()).save(any(Route.class));
        }

        @Test
        @DisplayName("updateRoute with duplicate route number on another route throws RuntimeException")
        void testUpdateRoute_DuplicateOnAnotherRoute() {
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            when(routeRepository.existsByRouteNumberAndIdNot("101", 1L)).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> routeService.updateRoute(1L, sampleRequest));
            assertEquals("Route number already exists for another route", ex.getMessage());
            verify(routeRepository, never()).save(any(Route.class));
        }

        @Test
        @DisplayName("deleteRoute with non-existent id throws ResourceNotFoundException")
        void testDeleteRoute_NotFound() {
            when(routeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> routeService.deleteRoute(999L));
            verify(routeRepository, never()).delete(any(Route.class));
        }

        @Test
        @DisplayName("toggleRouteActive with non-existent id throws ResourceNotFoundException")
        void testToggleRouteActive_NotFound() {
            when(routeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> routeService.toggleRouteActive(999L));
            verify(routeRepository, never()).save(any(Route.class));
        }
    }

    @Nested
    @DisplayName("Boundary Cases")
    class BoundaryCases {

        @Test
        @DisplayName("getAllRoutes when repository is empty returns empty list")
        void testGetAllRoutes_Empty() {
            when(routeRepository.findAll()).thenReturn(Collections.emptyList());

            List<RouteResponse> results = routeService.getAllRoutes();

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("createRoute with active=null defaults to true")
        void testCreateRoute_NullActiveDefaultsToTrue() {
            sampleRequest.setActive(null);
            when(routeRepository.existsByRouteNumber("101")).thenReturn(false);
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RouteResponse result = routeService.createRoute(sampleRequest);

            assertNotNull(result);
            assertTrue(result.getActive());
        }

        @Test
        @DisplayName("createRoute with zero fare boundary (free transit concession)")
        void testCreateRoute_ZeroFare() {
            sampleRequest.setFare(0.0);
            sampleRequest.setDistanceKm(0.1);
            when(routeRepository.existsByRouteNumber("101")).thenReturn(false);
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RouteResponse result = routeService.createRoute(sampleRequest);

            assertNotNull(result);
            assertEquals(0.0, result.getFare());
            assertEquals(0.1, result.getDistanceKm());
        }

        @Test
        @DisplayName("updateRoute with active=null defaults to true")
        void testUpdateRoute_NullActiveDefaultsToTrue() {
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            when(routeRepository.existsByRouteNumberAndIdNot("101", 1L)).thenReturn(false);
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            sampleRequest.setActive(null);
            RouteResponse result = routeService.updateRoute(1L, sampleRequest);

            assertNotNull(result);
            assertTrue(result.getActive());
        }

        @Test
        @DisplayName("toggleRouteActive when route active is false toggles to true")
        void testToggleRouteActive_FalseToTrue() {
            sampleRoute.setActive(false);
            when(routeRepository.findById(1L)).thenReturn(Optional.of(sampleRoute));
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RouteResponse result = routeService.toggleRouteActive(1L);

            assertNotNull(result);
            assertTrue(result.getActive());
        }
    }
}
