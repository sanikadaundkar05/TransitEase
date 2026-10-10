package com.transitease.service;

import com.transitease.dto.RouteRequest;
import com.transitease.dto.RouteResponse;
import com.transitease.entity.Route;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return RouteResponse.fromEntity(route);
    }

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        if (routeRepository.existsByRouteNumber(request.getRouteNumber())) {
            throw new RuntimeException("Route number already exists");
        }
        Route route = new Route();
        route.setRouteNumber(request.getRouteNumber());
        route.setSource(request.getSource());
        route.setDestination(request.getDestination());
        route.setDescription(request.getDescription());
        route.setActive(request.getActive() != null ? request.getActive() : true);
        
        Route savedRoute = routeRepository.save(route);
        return RouteResponse.fromEntity(savedRoute);
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
                
        if (routeRepository.existsByRouteNumberAndIdNot(request.getRouteNumber(), id)) {
            throw new RuntimeException("Route number already exists for another route");
        }
        
        route.setRouteNumber(request.getRouteNumber());
        route.setSource(request.getSource());
        route.setDestination(request.getDestination());
        route.setDescription(request.getDescription());
        route.setActive(request.getActive() != null ? request.getActive() : true);
        
        Route updatedRoute = routeRepository.save(route);
        return RouteResponse.fromEntity(updatedRoute);
    }
}
