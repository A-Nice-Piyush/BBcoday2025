package com.nice.avishkar.impl;

//import org.apache.commons.lang3.NotImplementedException;
import com.nice.avishkar.interfaces.ITravelOptimizer;
import com.nice.avishkar.dao.ResourceInfoReader;
import com.nice.avishkar.model.*;
import com.nice.avishkar.dao.ResourceInfo;
import com.nice.avishkar.util.CostComparator;
import com.nice.avishkar.util.HopsComparator;
import com.nice.avishkar.util.TimeComparator;

import java.io.IOException;
import java.util.*;

public class TravelOptimizerImpl implements ITravelOptimizer {

    private static Graph graph = new Graph();
    List<Route> routes = new ArrayList<>();
    private List<List<Route>> optimalRoutes = new ArrayList<>(); // Moved here
    Map<Set<List<Route>>, Integer> pathCostMap = new HashMap<>();

    public Map<String, OptimalTravelSchedule> getOptimalTravelOptions(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        // throw new NotImplementedException("Not implemented yet.");
        List<CustomerRequests> requestsList = ResourceInfoReader
                .getCustomerRequests(resourceInfo.getCustomerRequestPath());
        List<Route> routes = ResourceInfoReader.getRoutes(resourceInfo.getTransportSchedulePath());

        routes.stream().forEach(route -> {
            graph.addEdge(route.getSource(), route.getDestination(), route);
        });
        graph.printGraph();

        Map<String, OptimalTravelSchedule> res = new HashMap<>();

        requestsList.forEach(customerRequests -> {
            String rootSource = customerRequests.getSource();
            String finalDest = customerRequests.getDestination();
            Criteria criteria = customerRequests.getCriteria();

            switch (criteria) {
                case COST:
                    // Use Dijkstra's algorithm to find the optimal path
                    List<Route> optimalPathByCost = findOptimalPathByCost(rootSource, finalDest);
                    // If optimalPath is found, process it based on criteria (e.g., minimize cost)
                    if (optimalPathByCost != null) {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(optimalPathByCost,
                                criteria.toString(),
                                optimalPathByCost.stream().mapToInt(Route::getCost).sum());
                        res.put(customerRequests.getRequestId(), schedule);
                    } else {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(),
                                criteria.toString(), 0);
                        res.put(customerRequests.getRequestId(), schedule);
                    }
                    break;

                case TIME:
                    // Use Dijkstra's algorithm to find the optimal path
                    List<Route> optimalPathByTime = findOptimalPathByTime(rootSource, finalDest);
                    // If optimalPath is found, process it based on criteria (e.g., minimize time)
                    if (optimalPathByTime != null) {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(optimalPathByTime,
                                criteria.toString(),
                                optimalPathByTime.stream().mapToInt(this::calculateTotalTime).sum());
                        res.put(customerRequests.getRequestId(), schedule);
                    } else {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(),
                                criteria.toString(), 0);
                        res.put(customerRequests.getRequestId(), schedule);
                    }
                    break;
                case HOPS:
                    List<Route> optimalPath = findOptimalPathByTransfers(rootSource, finalDest);
                    if (optimalPath != null) {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(optimalPath, criteria.toString(), optimalPath.size());
                        res.put(customerRequests.getRequestId(), schedule);
                    } else {
                        OptimalTravelSchedule schedule = new OptimalTravelSchedule(new ArrayList<>(), criteria.toString(), 0);
                        res.put(customerRequests.getRequestId(), schedule);
                    }

                default:
                    break;
            }

        });

        return res;
    }

    private List<Route> findOptimalPathByCost(String source, String destination) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Route> previous = new HashMap<>();
        PriorityQueue<Route> queue = new PriorityQueue<>(new CostComparator());

        distances.put(source, 0);
        queue.add(new Route(source, source, "", "", "", 0, 0L));

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
                    queue.add(new Route(currentNode, neighbor.getDestination(), neighbor.getMode(),
                            neighbor.getDepartureTime(), neighbor.getArrivalTime(), newDist, 0L));
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

    private List<Route> findOptimalPathByTime(String source, String destination) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Route> previous = new HashMap<>();
        PriorityQueue<Route> queue = new PriorityQueue<>(new TimeComparator());

        distances.put(source, 0);
        queue.add(new Route(source, source, "", "", "", 0, 0L));

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
                int newDist = distances.get(currentNode) + calculateTotalTime(neighbor);
                if (newDist < distances.getOrDefault(neighbor.getDestination(), Integer.MAX_VALUE)) {
                    distances.put(neighbor.getDestination(), newDist);
                    previous.put(neighbor.getDestination(), neighbor);
                    queue.add(new Route(currentNode, neighbor.getDestination(), neighbor.getMode(),
                            neighbor.getDepartureTime(), neighbor.getArrivalTime(), newDist, 0L));
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

    private int calculateTotalTime(Route route) {
        int departureTime = convertToMinutes(route.getDepartureTime());
        int arrivalTime = convertToMinutes(route.getArrivalTime());
        int travelTime = arrivalTime - departureTime;
        if (travelTime < 0) {
            travelTime += 1440; // Add 24 hours (1440 minutes) if travel time is negative
        }
        return travelTime;
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
    // Node class to store information about a node in the priority queue
    private static class Node {
        String stop;
        int transfers;

        Node(String stop, int transfers) {
            this.stop = stop;
            this.transfers = transfers;
        }

        // Comparator for the priority queue to prioritize the node with fewer transfers
        public int compareTo(Node other) {
            return Integer.compare(this.transfers, other.transfers);
        }
    }

    // Dijkstra to minimize transfers and return the optimal path with the routes
    /*public static List<Route> findMinTransfersWithRoutes(String start, String destination) {
        // Distance map, key = node, value = minimum number of transfers to reach that node
        Map<String, Integer> transfers = new HashMap<>();
        // Previous node map for path reconstruction
        Map<String, String> previous = new HashMap<>();
        // Priority queue (min-heap) to store nodes with fewest transfers at the top
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.transfers));
        // Set to track visited nodes
        Set<String> visited = new HashSet<>();

        // Initialize: set the source node transfer count to 0
        transfers.put(start, 0);
        pq.offer(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentStop = current.stop;

            // Skip the node if it has already been visited
            if (visited.contains(currentStop)) {
                continue;
            }
            visited.add(currentStop);

            // If we reach the destination, we can stop early
            if (currentStop.equals(destination)) {
                break;
            }

            // Explore neighbors (routes from the current stop)
            for (Route route : graph.getEdges(currentStop)) {
                String nextStop = route.getDestination();

                // Calculate new transfer count (each edge is a transfer)
                int newTransferCount = current.transfers + 1;  // Each edge counts as 1 transfer

                // If we haven't visited the next stop or found a better way to get there
                if (!transfers.containsKey(nextStop) || newTransferCount < transfers.get(nextStop)) {
                    transfers.put(nextStop, newTransferCount);
                    previous.put(nextStop, currentStop);
                    pq.offer(new Node(nextStop, newTransferCount));
                }
            }
        }

        // Reconstruct the path by backtracking from destination to source
        List<Route> path = new ArrayList<>();
        String current = destination;

        while (previous.containsKey(current)) {
            String prev = previous.get(current);
            // Find the route corresponding to the previous and current stops
            for (Route route : graph.getEdges(prev)) {
                if (route.getDestination().equals(current)) {
                    path.add(route);
                    break;
                }
            }
            current = prev;
        }

        Collections.reverse(path);  // Reverse to get the correct order from start to destination
        return path;
    }*/

    private List<Route> findOptimalPathByTransfers(String source, String destination) {
        Map<String, Long> transferCounts = new HashMap<>();
        Map<String, Route> previous = new HashMap<>();
        PriorityQueue<Route> queue = new PriorityQueue<>(new HopsComparator());

        // Initialize transfer count for the source
        transferCounts.put(source, 0L);
        queue.add(new Route(source, source, "", "", "", 0, 0L));

        while (!queue.isEmpty()) {
            Route currentRoute = queue.poll();
            String currentNode = currentRoute.getDestination();

            // If we reached the destination, reconstruct the path
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

            // Explore neighbors (routes from the current stop)
            for (Route neighbor : graph.getEdges(currentNode)) {
                long newTransfers = transferCounts.get(currentNode) + 1; // Add one transfer for each new route taken

                // If a better transfer count is found for the neighbor, update it
                if (newTransfers < transferCounts.getOrDefault(neighbor.getDestination(), Long.MAX_VALUE)) {
                    transferCounts.put(neighbor.getDestination(), newTransfers);
                    previous.put(neighbor.getDestination(), neighbor);
                    queue.add(new Route(currentNode, neighbor.getDestination(), neighbor.getMode(),
                            neighbor.getDepartureTime(), neighbor.getArrivalTime(), 0, newTransfers));
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

}
