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

    public RouteRequest() {
    }

    public RouteRequest(String routeNumber, String source, String destination, String description, Boolean active) {
        this.routeNumber = routeNumber;
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.active = (active != null) ? active : true;
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
}
