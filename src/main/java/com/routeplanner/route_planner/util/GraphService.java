package com.routeplanner.route_planner.util;

import com.routeplanner.route_planner.model.Route;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    // ------------------ Build adjacency list ------------------
    private Map<Long, List<Route>> buildGraph(List<Route> routes) {
        Map<Long, List<Route>> graph = new HashMap<>();
        for (Route route : routes) {
            graph.computeIfAbsent(route.getSourceId(), k -> new ArrayList<>()).add(route);
        }
        return graph;
    }

    // ------------------ Dijkstra Algorithm ------------------
    public Map<String, Object> dijkstra(Long source, Long destination, List<Route> routes) {

        Map<Long, List<Route>> graph = buildGraph(routes);

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.dist));

        dist.put(source, 0.0);
        pq.add(new NodeDist(source, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            Long node = current.node;
            double distance = current.dist;

            for (Route route : graph.getOrDefault(node, List.of())) {
                Long neighbor = route.getDestinationId();
                double newDist = distance + route.getDistance();

                if (!dist.containsKey(neighbor) || newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, node);
                    pq.add(new NodeDist(neighbor, newDist));
                }
            }
        }

        // Build shortest path
        List<Long> path = new ArrayList<>();
        if (!dist.containsKey(destination)) {
            return Map.of(
                    "distance", "No path found",
                    "path", path
            );
        }

        Long step = destination;
        while (step != null) {
            path.add(step);
            step = prev.get(step);
        }
        Collections.reverse(path);

        return Map.of(
                "distance", dist.get(destination),
                "path", path
        );
    }

    // ------------------ Find All Paths ------------------
    public List<List<Long>> getAllPaths(Long source, Long destination, List<Route> routes) {

        // FIX: Use Set to remove duplicate edges
        Map<Long, Set<Long>> adj = new HashMap<>();

        for (Route r : routes) {
            adj.computeIfAbsent(r.getSourceId(), k -> new HashSet<>())
                    .add(r.getDestinationId());

            adj.computeIfAbsent(r.getDestinationId(), k -> new HashSet<>())
                    .add(r.getSourceId());
        }

        List<List<Long>> result = new ArrayList<>();
        dfs(source, destination, adj, new ArrayList<>(), new HashSet<>(), result);

        return result;
    }

    // ------------------ DFS Helper ------------------
    private void dfs(Long current,
                     Long destination,
                     Map<Long, Set<Long>> adj,
                     List<Long> path,
                     Set<Long> visited,
                     List<List<Long>> result) {

        visited.add(current);
        path.add(current);

        if (current.equals(destination)) {
            result.add(new ArrayList<>(path));
        } else {
            for (Long neighbor : adj.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, destination, adj, path, visited, result);
                }
            }
        }

        // Backtrack
        path.remove(path.size() - 1);
        visited.remove(current);
    }

    // ------------------ Helper class for Dijkstra ------------------
    private static class NodeDist {
        Long node;
        double dist;

        NodeDist(Long node, double dist) {
            this.node = node;
            this.dist = dist;
        }
    }
}