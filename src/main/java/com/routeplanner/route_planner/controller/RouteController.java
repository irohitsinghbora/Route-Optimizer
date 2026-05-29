package com.routeplanner.route_planner.controller;

import com.routeplanner.route_planner.model.Location;
import com.routeplanner.route_planner.model.Route;
import com.routeplanner.route_planner.repository.LocationRepository;
import com.routeplanner.route_planner.repository.RouteRepository;
import com.routeplanner.route_planner.util.GraphService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class RouteController {

    private final LocationRepository locationRepo;
    private final RouteRepository routeRepo;
    private final GraphService graphService;

    public RouteController(LocationRepository locationRepo,
                           RouteRepository routeRepo,
                           GraphService graphService) {

        this.locationRepo = locationRepo;
        this.routeRepo = routeRepo;
        this.graphService = graphService;
    }

    // ---------------- LOCATIONS ----------------

    @PostMapping("/locations")
    public Location addLocation(@RequestBody Location location) {
        return locationRepo.save(location);
    }

    @GetMapping("/locations")
    public List<Location> getLocations() {
        return locationRepo.findAll();
    }

    @DeleteMapping("/locations/{id}")
    public void deleteLocation(@PathVariable Long id) {

        // delete routes connected to location
        List<Route> routes = routeRepo.findAll();

        for (Route r : routes) {

            if (r.getSourceId().equals(id)
                    || r.getDestinationId().equals(id)) {

                routeRepo.deleteById(r.getId());
            }
        }

        locationRepo.deleteById(id);
    }

    // ---------------- ROUTES ----------------

    @PostMapping("/routes")
    public Route addRoute(@RequestBody Route route) {
        return routeRepo.save(route);
    }

    @GetMapping("/routes")
    public List<Route> getRoutes() {
        return routeRepo.findAll();
    }

    @DeleteMapping("/routes/{id}")
    public void deleteRoute(@PathVariable Long id) {
        routeRepo.deleteById(id);
    }

    // ---------------- PATHS ----------------

    @GetMapping("/paths")
    public Map<String, Object> getPaths(
            @RequestParam String sourceName,
            @RequestParam String destinationName
    ) {

        Location source =
                locationRepo.findByName(sourceName).orElseThrow();

        Location destination =
                locationRepo.findByName(destinationName).orElseThrow();

        List<Route> routes = routeRepo.findAll();

        Map<String, Object> shortest =
                graphService.dijkstra(
                        source.getId(),
                        destination.getId(),
                        routes
                );

        List<Map<String, Object>> allPaths =
                graphService.getAllPathsWithDistance(
                        source.getId(),
                        destination.getId(),
                        routes
                );

        return Map.of(
                "shortestPath", shortest,
                "allPaths", allPaths
        );
    }
}