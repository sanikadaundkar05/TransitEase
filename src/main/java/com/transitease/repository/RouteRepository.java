package com.transitease.repository;

import com.transitease.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByRouteNumber(String routeNumber);

    boolean existsByRouteNumber(String routeNumber);

    boolean existsByRouteNumberAndIdNot(String routeNumber, Long id);

    long countByActive(Boolean active);

    java.util.List<Route> findByActive(Boolean active);
}
