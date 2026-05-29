package com.routeplanner.route_planner.repository;

import com.routeplanner.route_planner.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository
        extends JpaRepository<Location, Long> {

    Optional<Location> findByName(String name);
}