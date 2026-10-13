package com.transitease.dto;

import jakarta.validation.constraints.NotBlank;

public class RouteRequest {

    @NotBlank(message = "Route number is required")
    private String routeNumber;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;

    private String description;

    private Boolean active = true;

    private Double fare;

    private Double distanceKm;

    private String stops;

    public RouteRequest() {
    }

    public RouteRequest(String routeNumber, String source, String destination, String description, Boolean active) {
        this(routeNumber, source, destination, description, active, null, null, null);
    }

    public RouteRequest(String routeNumber, String source, String destination, String description, Boolean active,
                        Double fare, Double distanceKm, String stops) {
        this.routeNumber = routeNumber;
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.active = (active != null) ? active : true;
        this.fare = fare;
        this.distanceKm = distanceKm;
        this.stops = stops;
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
        this.active = (active != null) ? active : true;
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
