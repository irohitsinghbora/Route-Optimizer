package com.routeplanner.route_planner.controller;

import com.routeplanner.route_planner.model.Location;
import com.routeplanner.route_planner.model.Route;
import com.routeplanner.route_planner.repository.LocationRepository;
import com.routeplanner.route_planner.repository.RouteRepository;
import com.routeplanner.route_planner.util.GraphService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteController {

    private final LocationRepository locationRepo;
    private final RouteRepository routeRepo;
    private final GraphService graphService;

    @Autowired
    public RouteController(LocationRepository locationRepo,
                           RouteRepository routeRepo,
                           GraphService graphService) {
        this.locationRepo = locationRepo;
        this.routeRepo = routeRepo;
        this.graphService = graphService;
    }

    // ------------------ Location Endpoints ------------------

    @PostMapping("/locations")
    public ResponseEntity<Location> addLocation(@RequestBody Location location) {
        Location saved = locationRepo.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getLocations() {
        List<Location> locations = locationRepo.findAll();
        return ResponseEntity.ok(locations);
    }

    // ------------------ Route Endpoints ------------------

    @PostMapping("/routes")
    public ResponseEntity<Route> addRoute(@RequestBody Route route) {
        // Optional: Validate source & destination exist
        if (!locationRepo.existsById(route.getSourceId()) || !locationRepo.existsById(route.getDestinationId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        Route saved = routeRepo.save(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getRoutes() {
        List<Route> routes = routeRepo.findAll();
        return ResponseEntity.ok(routes);
    }

    // ------------------ Shortest Path ------------------

    @GetMapping("/shortest")
    public ResponseEntity<?> getShortestPath(@RequestParam Long source,
                                             @RequestParam Long dest) {

        if (!locationRepo.existsById(source) || !locationRepo.existsById(dest)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Source or destination not found"));
        }

        List<Route> routes = routeRepo.findAll();
        Map<String, Object> shortest = graphService.dijkstra(source, dest, routes);

        return ResponseEntity.ok(shortest);
    }

    // ------------------ All Paths ------------------

    @GetMapping("/all-paths")
    public ResponseEntity<?> getAllPaths(@RequestParam Long source,
                                         @RequestParam Long dest) {

        if (!locationRepo.existsById(source) || !locationRepo.existsById(dest)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Source or destination not found"));
        }

        List<Route> routes = routeRepo.findAll();

        List<List<Long>> allPaths = graphService.getAllPaths(source, dest, routes);
        Map<String, Object> shortest = graphService.dijkstra(source, dest, routes);

        return ResponseEntity.ok(Map.of(
                "allPaths", allPaths,
                "shortestPath", shortest.get("path"),
                "shortestDistance", shortest.get("distance")
        ));
    }
}