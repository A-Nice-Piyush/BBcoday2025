package com.nice.avishkar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, List<Route>> adjacencyList;

    public Graph()
    {
        adjacencyList = new HashMap<>();
    }
    public void addEdge(String source, String destination,Route route) {
        adjacencyList.putIfAbsent(source, new ArrayList<>());
        adjacencyList.putIfAbsent(destination, new ArrayList<>()); // Ensure destination node is also in the graph
        adjacencyList.get(source).add(route);
    }

    public List<Route> getEdges(String source) {
        return adjacencyList.getOrDefault(source, new ArrayList<>());
    }

    public void printGraph() {
        for (Map.Entry<String, List<Route>> entry : adjacencyList.entrySet()) {
            System.out.println("Node " + entry.getKey() + " has edges: " + entry.getValue());
        }
    }

    // Get a specific route from the source to the destination
    public Route getRoute(String source, String destination) {
        // Find the route from the adjacency list
        for (Route route : adjacencyList.getOrDefault(source, new ArrayList<>())) {
            if (route.getDestination().equals(destination)) {
                return route;
            }
        }
        return null; // No route found between source and destination
    }

}
