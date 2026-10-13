package com.transitease.dto;

import com.transitease.entity.Route;

public class RouteResponse {

    private Long id;
    private String routeNumber;
    private String source;
    private String destination;
    private String description;
    private Boolean active;
    private Double fare;
    private Double distanceKm;
    private String stops;

    public RouteResponse() {
    }

    public RouteResponse(Long id, String routeNumber, String source, String destination, String description, Boolean active) {
        this(id, routeNumber, source, destination, description, active, null, null, null);
    }

    public RouteResponse(Long id, String routeNumber, String source, String destination, String description, Boolean active,
                         Double fare, Double distanceKm, String stops) {
        this.id = id;
        this.routeNumber = routeNumber;
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.active = active;
        this.fare = fare;
        this.distanceKm = distanceKm;
        this.stops = stops;
    }

    public static RouteResponse fromEntity(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getRouteNumber(),
                route.getSource(),
                route.getDestination(),
                route.getDescription(),
                route.getActive(),
                route.getFare(),
                route.getDistanceKm(),
                route.getStops()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getStops() {
        return stops;
    }

    public void setStops(String stops) {
        this.stops = stops;
    }
}
