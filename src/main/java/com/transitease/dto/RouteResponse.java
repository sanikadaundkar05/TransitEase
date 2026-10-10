package com.transitease.dto;

import com.transitease.entity.Route;

public class RouteResponse {

    private Long id;
    private String routeNumber;
    private String source;
    private String destination;
    private String description;
    private Boolean active;

    public RouteResponse() {
    }

    public RouteResponse(Long id, String routeNumber, String source, String destination, String description, Boolean active) {
        this.id = id;
        this.routeNumber = routeNumber;
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.active = active;
    }

    public static RouteResponse fromEntity(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getRouteNumber(),
                route.getSource(),
                route.getDestination(),
                route.getDescription(),
                route.getActive()
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
}
