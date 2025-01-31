package com.nice.avishkar.impl;

//import org.apache.commons.lang3.NotImplementedException;
import com.nice.avishkar.interfaces.ITravelOptimizer;
import com.nice.avishkar.dao.ResourceInfoReader;
import com.nice.avishkar.model.*;
import com.nice.avishkar.dao.ResourceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelOptimizerImpl implements ITravelOptimizer {

    private static Graph graph = new Graph();
    List<Route> routes = new ArrayList<>();
    private List<List<Route>> optimalRoutes = new ArrayList<>(); // Moved here

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

        requestsList.forEach(customerRequests ->
        {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();
            optimalRoutes.clear(); // Clear previous results
            
            graph.getEdges(rootSource).forEach(route -> {
                List<Route> currentPath = new ArrayList<>();
                findAllPaths(route.getSource(), finalDest, currentPath, 0);
            });
            
            // Print paths for one customerRequest
            System.out.println("Possible paths for customer request from " + rootSource + " to " + finalDest + ":");
            optimalRoutes.forEach(path -> {
                path.forEach(route -> System.out.print(route.getSource() + " -> " + route.getDestination() + " "));
                // System.out.println(route.getDestination());
            });
            
            // Process optimalRoutes based on criteria and add to res
            // ...implementation to process optimalRoutes...
        });
        return res;
    }

    private void findAllPaths(String source, String destination, List<Route> currentPath, int currentCost) {
        for (Route route : graph.getEdges(source)) {
            if (currentPath.stream().anyMatch(r -> r.getSource().equals(route.getSource()) && r.getDestination().equals(route.getDestination()))) {
                continue; // Skip if the route is already in the current path to avoid cycles
            }
            currentPath.add(route);
            currentCost += route.getCost();
            
            if (route.getDestination().equals(destination)) {
                optimalRoutes.add(new ArrayList<>(currentPath));
            } else {
                findAllPaths(route.getDestination(), destination, currentPath, currentCost);
            }
            
            currentPath.remove(currentPath.size() - 1);
            currentCost -= route.getCost();
        }
    }
}
