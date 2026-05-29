package com.routeplanner.route_planner.repository;

import com.routeplanner.route_planner.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository
        extends JpaRepository<Route, Long> {
}