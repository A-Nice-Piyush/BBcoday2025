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

        requestsList.forEach(customerRequests -> {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();

            // Use Dijkstra's algorithm to find the optimal path
            List<Route> optimalPath = findOptimalPath(rootSource, finalDest);

            // If optimalPath is found, process it based on criteria (e.g., minimize cost)
            if (optimalPath != null) {
                OptimalTravelSchedule schedule = new OptimalTravelSchedule(optimalPath, criteria.toString(), optimalPath.stream().mapToInt(Route::getCost).sum());
                res.put(customerRequests.getRequestId(), schedule);
            } else {
                OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(), criteria.toString(), 0);
                res.put(customerRequests.getRequestId(), schedule);
            }
        });

        return res;
    }

    private List<Route> findOptimalPath(String source, String destination) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Route> previous = new HashMap<>();
        PriorityQueue<Route> queue = new PriorityQueue<>(Comparator.comparingInt(Route::getCost));

        distances.put(source, 0);
        queue.add(new Route(source, source, "", "", "", 0));

        while (!queue.isEmpty()) {
            Route currentRoute = queue.poll();
            String currentNode = currentRoute.getDestination();

            if (currentNode.equals(destination)) {
                List<Route> path = new ArrayList<>();
                while (previous.containsKey(currentNode)) {
                    Route route = previous.get(currentNode);
                    path.add(route);
                    currentNode = route.getSource();
                }
                Collections.reverse(path);
                return path;
            }

            for (Route neighbor : graph.getEdges(currentNode)) {
                int newDist = distances.get(currentNode) + neighbor.getCost();
                if (newDist < distances.getOrDefault(neighbor.getDestination(), Integer.MAX_VALUE)) {
                    distances.put(neighbor.getDestination(), newDist);
                    previous.put(neighbor.getDestination(), neighbor);
                    queue.add(new Route(currentNode, neighbor.getDestination(), neighbor.getMode(), neighbor.getDepartureTime(), neighbor.getArrivalTime(), newDist));
                }
            }
        }

        return null; // No path found
    }

}
