package com.transitease.controller;

import com.transitease.dto.RouteRequest;
import com.transitease.dto.RouteResponse;
import com.transitease.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROUTE_FARE_MANAGER')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROUTE_FARE_MANAGER')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.updateRoute(id, request);
        return ResponseEntity.ok(response);
    }
}
