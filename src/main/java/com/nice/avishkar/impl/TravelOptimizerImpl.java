package com.nice.avishkar.impl;

//import org.apache.commons.lang3.NotImplementedException;
import com.nice.avishkar.interfaces.ITravelOptimizer;
import com.nice.avishkar.dao.ResourceInfoReader;
import com.nice.avishkar.model.*;
import com.nice.avishkar.dao.ResourceInfo;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TravelOptimizerImpl implements ITravelOptimizer {

    private static Graph graph = new Graph();
    private static MSCPGraph mscpGraph = new MSCPGraph();
    List<Route> routes = new ArrayList<>();
    private List<List<Route>> optimalRoutes = new ArrayList<>(); // Moved here

    public Map<String, OptimalTravelSchedule> getOptimalTravelOptions(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        //throw new NotImplementedException("Not implemented yet.");
        List<CustomerRequests> requestsList = ResourceInfoReader.getCustomerRequests(resourceInfo.getCustomerRequestPath());
        List<Route> routes = ResourceInfoReader.getRoutes(resourceInfo.getTransportSchedulePath());

        routes.stream().forEach(route -> {
            graph.addEdge(route.getSource(), route.getDestination(), route);
            mscpGraph.addEdge(route.getSource(), route.getDestination(), route);
        });
        //graph.printGraph();
        mscpGraph.printGraph();

        Map<String, OptimalTravelSchedule> res = new HashMap<>();

        requestsList.forEach(customerRequests ->
        {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();
            optimalRoutes.clear(); // Clear previous results
            // Track visited paths to avoid duplicate paths

            // Find the best route based on customer criteria
            //Route optimalPath = findOptimalPath(rootSource, finalDest, criteria);


            List<OptimalTravelSchedule> optimalPaths = mscpGraph.findOptimalPaths(rootSource, finalDest,criteria.toString());
            // Store the result
            /*int totalCost = optimalPath != null ? optimalPath.getCost() : 0;
            //int totalTime = optimalPath != null ? optimalPath.getArrivalTime() - optimalPath.getDepartureTime() : 0;
            OptimalTravelSchedule schedule = new OptimalTravelSchedule(Collections.singletonList(optimalPath), criteria.toString(), totalCost);
            res.put(customerRequests.getRequestId(), schedule);
*/
            /*et<List<Route>> visitedPaths = new HashSet<>();

            // Track visited nodes to avoid cycles
            Set<String> visited = new HashSet<>();

            // Start finding paths from the rootSource to finalDest
            //graph.getEdges(rootSource).forEach(route -> {
                //visited.add(rootSource);
                List<Route> currentPath = new ArrayList<>();
                findAllPaths(rootSource, finalDest, currentPath, 0, visited, visitedPaths);
            //});
        */
            // If optimalRoutes has paths, process them based on criteria (e.g., minimize cost)
            if (!optimalPaths.isEmpty()) {
                // Find the optimal route based on cost (or any other criteria you define)
               /* Optional<List<Route>> optimalPath = optimalRoutes.stream()
                        .min(Comparator.comparingInt(path -> path.stream().mapToInt(Route::getCost).sum())); // Minimize cost

                System.out.println("Stop the debugger here");*/
                // Store the optimal path in the result

                    res.put(customerRequests.getRequestId(), optimalPaths.get(0));

            } else
            {
                OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(), criteria.toString(), 0);
                res.put(customerRequests.getRequestId(), schedule);
            }
        });
        return res;
    }

    /*private void findAllPaths(String source, String destination, List<Route> currentPath, int currentCost, Set<String> visited, Set<List<Route>> visitedPaths) {
        // Mark the current node as visited
        visited.add(source);

        *//*if(pathCostMap.containsKey(source+"-"+destination))
        {
            optimalRoutes.add(pathCostMap.get(pathCostMap.containsKey(source+"-"+destination)));
            return;
        }*//*
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
                    //pathCostMap.put(pathCopy.get(0).getSource() +"-"+ pathCopy.get(pathCopy.size()-1).getDestination(),pathCopy);
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
    }*/

    private Route findOptimalPath(String source, String destination, Criteria criteria) {
        // Priority queue to store nodes with the current best (time, cost, stop count) vector
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getPriority));
        // Distance map: key -> node name, value -> best cost vector (time, cost, stops)
        Map<String, int[]> distance = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // Initialize distances: set all distances to infinity except for the source
        distance.put(source, new int[]{0, 0, 0});  // [time, cost, stop count]
        pq.offer(new Node(source, 0, 0, 0)); // [time, cost, stops]

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            String current = currentNode.getName();
            int[] currentCost = currentNode.getCost();

            // Skip if we've already visited this node
            if (visited.contains(current)) continue;
            visited.add(current);

            // If we reach the destination, stop early
            if (current.equals(destination)) {
                break;
            }

            // Explore neighbors (routes from the current node)
            for (Route route : graph.getEdges(current)) {
                String neighbor = route.getDestination();
                LocalTime depTime = LocalTime.parse(route.getDepartureTime());
                LocalTime ArrTime = LocalTime.parse(route.getArrivalTime());

                long differenceInMinutes = ChronoUnit.MINUTES.between(ArrTime, depTime);
                int[] neighborCost = new int[]{
                        (int) (currentCost[0] + differenceInMinutes),  // time
                        currentCost[1] + route.getCost(),  // cost
                        currentCost[2] + 1  // stops (1 for each route)
                };

                // Check if waiting time is needed between connections
                if (!visited.contains(neighbor)) {
                    int[] neighborArrivalTime = distance.get(neighbor);
                    if (neighborArrivalTime != null) {

                        LocalTime nextBusDeparts = LocalTime.parse(route.getDepartureTime());
                        LocalTime currentBusArrives = LocalTime.parse(route.getArrivalTime());

                        long differenceInMinutesInTwoBuses = ChronoUnit.MINUTES.between(nextBusDeparts, currentBusArrives);

                        if (nextBusDeparts.isBefore(currentBusArrives)) {
                            // If the next bus departs before the current bus arrives, add 24 hours (next day)
                            neighborCost[0] += 24 * 60+ differenceInMinutesInTwoBuses;  // Add 24 hours in minutes
                        } else
                        {
                            neighborCost[0]+= differenceInMinutesInTwoBuses;
                        }
                    }
                }

                // Relax the edges: update the neighbor's cost vector if a better route is found
                if (!visited.contains(neighbor) && (distance.get(neighbor) == null || isBetterRoute(neighborCost, distance.get(neighbor), criteria))) {
                    distance.put(neighbor, neighborCost);
                    pq.offer(new Node(neighbor, neighborCost[0], neighborCost[1], neighborCost[2]));
                    previousNodes.put(neighbor, current);
                }
            }
        }

        // Reconstruct the path from destination to source
        return reconstructPath(destination, previousNodes);
    }

    private boolean isBetterRoute(int[] newRoute, int[] oldRoute, Criteria criteria) {
        switch (criteria) {
            case TIME:
                return newRoute[0] < oldRoute[0]; // Prefer less time
            case COST:
                return newRoute[1] < oldRoute[1]; // Prefer less cost
            case HOPS:
                return newRoute[2] < oldRoute[2]; // Prefer fewer stops
            default:
                return false;
        }
    }

    private Route reconstructPath(String destination, Map<String, String> previousNodes) {
        String current = destination;
        List<Route> path = new ArrayList<>();
        while (previousNodes.containsKey(current)) {
            String prev = previousNodes.get(current);
            Route route = graph.getRoute(prev, current);
            path.add(route);
            current = prev;
        }
        Collections.reverse(path);
        return path.isEmpty() ? null : path.get(0);  // Return the first route for simplicity
    }

    // Helper class to represent each node in the priority queue
    private static class Node {
        private String name;
        private int time;
        private int cost;
        private int stops;

        public Node(String name, int time, int cost, int stops) {
            this.name = name;
            this.time = time;
            this.cost = cost;
            this.stops = stops;
        }

        public String getName() {
            return name;
        }

        public int[] getCost() {
            return new int[]{time, cost, stops};
        }

        // Composite priority for sorting in the priority queue (you can tweak this based on criteria)
        public double getPriority() {
            return time + cost; // You can modify this based on customer priority, e.g., weighted sum.
        }
    }

    // Find all paths from source to destination, avoiding cycles
    public List<List<Route>> findAllPaths(String source, String destination) {
        List<List<Route>> allPaths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<Route> currentPath = new ArrayList<>();
        findPathsDFS(source, destination, visited, currentPath, allPaths);
        return allPaths;
    }

    private void findPathsDFS(String current, String destination, Set<String> visited,
                              List<Route> currentPath, List<List<Route>> allPaths) {
        // If we've reached the destination, save the current path
        if (current.equals(destination)) {
            allPaths.add(new ArrayList<>(currentPath));  // Add a copy of currentPath
            return;
        }

        // Mark the current node as visited
        visited.add(current);

        // Explore neighbors of the current node
        for (Route route : graph.getEdges(current)) {
            String nextNode = route.getDestination();

            // Skip if the node has been visited in the current path (cycle check)
            if (visited.contains(nextNode)) {
                continue;
            }

            // Add the route to the current path
            currentPath.add(route);

            // Recur for the next node
            findPathsDFS(nextNode, destination, visited, currentPath, allPaths);

            // Backtrack: remove the route from the path
            currentPath.remove(currentPath.size() - 1);
        }

        // Unmark the current node as visited (for backtracking)
        visited.remove(current);
    }
}

