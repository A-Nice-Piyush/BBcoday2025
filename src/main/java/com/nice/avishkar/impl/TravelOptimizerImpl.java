package com.nice.avishkar.impl;

//import org.apache.commons.lang3.NotImplementedException;
import com.nice.avishkar.interfaces.ITravelOptimizer;
import com.nice.avishkar.dao.ResourceInfoReader;
import com.nice.avishkar.model.*;
import com.nice.avishkar.dao.ResourceInfo;

import java.io.IOException;
import java.util.*;

public class TravelOptimizerImpl implements ITravelOptimizer {

    private static Graph graph = new Graph();
    List<Route> routes = new ArrayList<>();
    private List<List<Route>> optimalRoutes = new ArrayList<>(); // Moved here
    Map<Set<List<Route>>, Integer> pathCostMap = new HashMap<>();

    public Map<String, OptimalTravelSchedule> getOptimalTravelOptions(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        //throw new NotImplementedException("Not implemented yet.");
        List<CustomerRequests> requestsList = ResourceInfoReader.getCustomerRequests(resourceInfo.getCustomerRequestPath());
        List<Route> routes = ResourceInfoReader.getRoutes(resourceInfo.getTransportSchedulePath());

        routes.stream().forEach(route -> {
            graph.addEdge(route.getSource(), route.getDestination(), route);
        });
        graph.printGraph();

        Map<String, OptimalTravelSchedule> res = new HashMap<>();


        // Track visited paths to avoid duplicate paths
        Set<List<Route>> visitedPaths = new HashSet<>();

        requestsList.forEach(customerRequests ->
        {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();
            optimalRoutes.clear(); // Clear previous results

            // Track visited nodes to avoid cycles
            Set<String> visited = new HashSet<>();



            // Start finding paths from the rootSource to finalDest
            //graph.getEdges(rootSource).forEach(route -> {
                //visited.add(rootSource);
                List<Route> currentPath = new ArrayList<>();
                findAllPaths(rootSource, finalDest, currentPath, 0, visited, visitedPaths);
            //});

            // If optimalRoutes has paths, process them based on criteria (e.g., minimize cost)
            if (!optimalRoutes.isEmpty()) {
                // Find the optimal route based on cost (or any other criteria you define)
                Optional<List<Route>> optimalPath = optimalRoutes.stream()
                        .min(Comparator.comparingInt(path -> path.stream().mapToInt(Route::getCost).sum())); // Minimize cost

                System.out.println("Stop the debugger here");
                // Store the optimal path in the result
                optimalPath.ifPresent(path -> {
                    OptimalTravelSchedule schedule = new OptimalTravelSchedule(path, criteria.toString(), path.stream().mapToInt(Route::getCost).sum());
                    res.put(customerRequests.getRequestId(), schedule);
                });
            } else
            {
                OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(), criteria.toString(), 0);
                res.put(customerRequests.getRequestId(), schedule);
            }
        });

            return res;
    }

    private void findAllPaths(String source, String destination, List<Route> currentPath, int currentCost, Set<String> visited, Set<List<Route>> visitedPaths) {
        // Mark the current node as visited
        visited.add(source);

        // Iterate through all routes starting from 'source'
        for (Route route : graph.getEdges(source)) {
            String nextNode = route.getDestination();

            // Skip if the next node has already been visited (avoid cycles)
            if (visited.contains(nextNode)) {
                continue;
            }

            // Add the route to the current path
            currentPath.add(route);
            currentCost += route.getCost();

            // If we've reached the destination, add the current path to the list
            if (nextNode.equals(destination)) {
                // To avoid duplicates, we check if the path already exists in visitedPaths
                List<Route> pathCopy = new ArrayList<>(currentPath);
                if (!visitedPaths.contains(pathCopy)) {
                    optimalRoutes.add(pathCopy);
                    visitedPaths.add(pathCopy); // Mark this path as visited
                    pathCostMap.put(visitedPaths,visitedPaths.stream()
                            .flatMap(List::stream) // Flatten the List<List<Route>> into a single stream of Route objects
                            .mapToInt(Route::getCost) // Convert each Route to its cost
                            .sum());
                }
            } else {
                // Recursively search for paths from the next node
                findAllPaths(nextNode, destination, currentPath, currentCost, visited, visitedPaths);
            }

            // Backtrack: remove the current route from the path and revert cost
            currentPath.remove(currentPath.size() - 1);
            currentCost -= route.getCost();
        }

        // Remove the current node from visited set to allow other paths
        visited.remove(source);
    }

}
