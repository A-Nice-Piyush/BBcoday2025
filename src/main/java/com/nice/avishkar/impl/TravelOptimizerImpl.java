package com.nice.avishkar.impl;

//import org.apache.commons.lang3.NotImplementedException;
import com.nice.avishkar.ITravelOptimizer;
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
        requestsList.stream().forEach(customerRequests ->
        {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();
            //Route currentNode = new Route();
            graph.getEdges(rootSource).forEach(route -> {

            });
        });
        return res;
    }


}
