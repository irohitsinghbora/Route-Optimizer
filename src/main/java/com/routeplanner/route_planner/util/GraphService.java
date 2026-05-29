package com.routeplanner.route_planner.util;

import com.routeplanner.route_planner.model.Route;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    // ---------------- BUILD GRAPH ----------------

    private Map<Long, List<Route>> buildGraph(List<Route> routes) {

        Map<Long, List<Route>> graph = new HashMap<>();

        for (Route route : routes) {

            graph.computeIfAbsent(route.getSourceId(),
                            k -> new ArrayList<>())
                    .add(route);

            // reverse edge
            Route reverse = new Route();

            reverse.setSourceId(route.getDestinationId());
            reverse.setDestinationId(route.getSourceId());
            reverse.setDistance(route.getDistance());

            graph.computeIfAbsent(route.getDestinationId(),
                            k -> new ArrayList<>())
                    .add(reverse);
        }

        return graph;
    }

    // ---------------- DIJKSTRA ----------------

    public Map<String, Object> dijkstra(
            Long source,
            Long destination,
            List<Route> routes
    ) {

        Map<Long, List<Route>> graph =
                buildGraph(routes);

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();

        PriorityQueue<Node> pq =
                new PriorityQueue<>(
                        Comparator.comparingDouble(n -> n.dist)
                );

        dist.put(source, 0.0);

        pq.add(new Node(source, 0.0));

        while (!pq.isEmpty()) {

            Node current = pq.poll();

            if (current.dist >
                    dist.getOrDefault(
                            current.node,
                            Double.MAX_VALUE
                    )) {
                continue;
            }

            for (Route edge :
                    graph.getOrDefault(
                            current.node,
                            Collections.emptyList()
                    )) {

                double newDist =
                        current.dist + edge.getDistance();

                if (newDist <
                        dist.getOrDefault(
                                edge.getDestinationId(),
                                Double.MAX_VALUE
                        )) {

                    dist.put(
                            edge.getDestinationId(),
                            newDist
                    );

                    prev.put(
                            edge.getDestinationId(),
                            current.node
                    );

                    pq.add(
                            new Node(
                                    edge.getDestinationId(),
                                    newDist
                            )
                    );
                }
            }
        }

        List<Long> path = new ArrayList<>();

        if (!dist.containsKey(destination)) {

            return Map.of(
                    "distance", -1,
                    "pathIds", path
            );
        }

        Long step = destination;

        while (step != null) {

            path.add(step);

            step = prev.get(step);
        }

        Collections.reverse(path);

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "distance",
                dist.get(destination)
        );

        result.put(
                "pathIds",
                path
        );

        return result;
    }

    // ---------------- ALL PATHS WITH DISTANCE ----------------

    public List<Map<String, Object>> getAllPathsWithDistance(
            Long source,
            Long destination,
            List<Route> routes
    ) {

        Map<Long, Set<Long>> adj =
                new HashMap<>();

        Map<String, Double> distanceMap =
                new HashMap<>();

        for (Route r : routes) {

            adj.computeIfAbsent(
                            r.getSourceId(),
                            k -> new HashSet<>())
                    .add(r.getDestinationId());

            adj.computeIfAbsent(
                            r.getDestinationId(),
                            k -> new HashSet<>())
                    .add(r.getSourceId());

            distanceMap.put(
                    r.getSourceId() + "-" + r.getDestinationId(),
                    r.getDistance()
            );

            distanceMap.put(
                    r.getDestinationId() + "-" + r.getSourceId(),
                    r.getDistance()
            );
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        dfs(
                source,
                destination,
                adj,
                distanceMap,
                new ArrayList<>(),
                new HashSet<>(),
                result
        );

        return result;
    }

    private void dfs(
            Long current,
            Long destination,
            Map<Long, Set<Long>> adj,
            Map<String, Double> distanceMap,
            List<Long> path,
            Set<Long> visited,
            List<Map<String, Object>> result
    ) {

        visited.add(current);

        path.add(current);

        if (current.equals(destination)) {

            double totalDistance = 0;

            for (int i = 0; i < path.size() - 1; i++) {

                String key =
                        path.get(i) + "-" + path.get(i + 1);

                totalDistance += distanceMap.get(key);
            }

            Map<String, Object> pathData =
                    new HashMap<>();

            pathData.put(
                    "path",
                    new ArrayList<>(path)
            );

            pathData.put(
                    "distance",
                    totalDistance
            );

            result.add(pathData);

        } else {

            for (Long next :
                    adj.getOrDefault(
                            current,
                            Collections.emptySet()
                    )) {

                if (!visited.contains(next)) {

                    dfs(
                            next,
                            destination,
                            adj,
                            distanceMap,
                            path,
                            visited,
                            result
                    );
                }
            }
        }

        path.remove(path.size() - 1);

        visited.remove(current);
    }

    // ---------------- NODE ----------------

    private static class Node {

        Long node;
        double dist;

        Node(Long node, double dist) {

            this.node = node;
            this.dist = dist;
        }
    }
}