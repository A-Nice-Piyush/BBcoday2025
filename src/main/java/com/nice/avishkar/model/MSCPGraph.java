
package com.nice.avishkar.model;

import java.util.*;
import java.util.stream.Collectors;

import java.util.*;
import java.util.stream.Collectors; // Import Collectors

public class MSCPGraph {
    private Map<String, List<Route>> adjacencyList;

    public MSCPGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addEdge(String source, String destination, Route route) {
        adjacencyList.putIfAbsent(source, new ArrayList<>());
        adjacencyList.putIfAbsent(destination, new ArrayList<>());
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

    // Helper function to convert time string "HH:mm" to total minutes
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes; // Return total minutes
    }

    // Priority queue comparison based on user-specified criteria with lexicographical fallback
    private int comparePaths(State a, State b, String criterion) {
        switch (criterion.toLowerCase()) {
            case "cost":
                if (a.totalCost != b.totalCost) {
                    return Integer.compare(a.totalCost, b.totalCost);
                } else if (a.totalJourneyTime != b.totalJourneyTime) {
                    return Integer.compare(a.totalJourneyTime, b.totalJourneyTime);
                } else {
                    return Integer.compare(a.transfers, b.transfers);
                }
            case "time":
                if (a.totalJourneyTime != b.totalJourneyTime) {
                    return Integer.compare(a.totalJourneyTime, b.totalJourneyTime);
                } else if (a.totalCost != b.totalCost) {
                    return Integer.compare(a.totalCost, b.totalCost);
                } else {
                    return Integer.compare(a.transfers, b.transfers);
                }
            case "transfers":
                if (a.transfers != b.transfers) {
                    return Integer.compare(a.transfers, b.transfers);
                } else if (a.totalCost != b.totalCost) {
                    return Integer.compare(a.totalCost, b.totalCost);
                } else {
                    return Integer.compare(a.totalJourneyTime, b.totalJourneyTime);
                }
            default:
                return 0;
        }
    }

    public List<OptimalTravelSchedule> findOptimalPaths(String start, String end, String criterion) {

        // Priority queue with dynamic comparison based on the user-specified criterion
        PriorityQueue<State> pq = new PriorityQueue<>((a, b) -> comparePaths(a, b, criterion));
        Set<String> visited = new HashSet<>();
        Map<String, Set<State>> bestPaths = new HashMap<>();  // Use a Set to avoid duplicates

        pq.offer(new State(start, 0, 0, 0, 0, new ArrayList<>())); // initial state (start, arrival time, cost, transfers, journey time, route list)

        while (!pq.isEmpty()) {
            State current = pq.poll();
            String node = current.node;
            int arrivalTime = current.arrivalTime;
            int totalCost = current.totalCost;
            int transfers = current.transfers;
            int totalJourneyTime = current.totalJourneyTime;
            List<Route> currentRouteList = current.routeList; // List of routes taken so far

            // If we reached the destination, add the path
            if (node.equals(end)) {
                bestPaths.computeIfAbsent(end, k -> new HashSet<>()).add(current);
                continue;
            }

            String stateKey = node + "-" + arrivalTime + "-" + totalCost + "-" + transfers;
            if (visited.contains(stateKey)) continue; // avoid processing the same state
            visited.add(stateKey);

            // Traverse the neighbors of the current node
            for (Route route : adjacencyList.getOrDefault(node, new ArrayList<>())) {
                int depTimeInMinutes = timeToMinutes(route.getDepartureTime()); // Convert departure time to minutes
                if (depTimeInMinutes >= arrivalTime) { // valid departure time
                    int waitingTime = depTimeInMinutes - arrivalTime; // calculate waiting time in minutes
                    // If arrival time matches departure time, no waiting time
                    if (arrivalTime == depTimeInMinutes) {
                        waitingTime = 0;
                    }
                    int travelTime = timeToMinutes(route.getArrivalTime()) - depTimeInMinutes; // travel time in minutes
                    int newArrival = timeToMinutes(route.getArrivalTime()); // arrival time in minutes
                    int newCost = totalCost + route.getCost();
                    int newTransfers = transfers + 1;
                    int newTotalJourneyTime = totalJourneyTime + waitingTime + travelTime; // total journey time

                    // Create a new route list including the current route
                    List<Route> newRouteList = new ArrayList<>(currentRouteList);
                    newRouteList.add(route);

                    State newState = new State(route.getDestination(), newArrival, newCost, newTransfers, newTotalJourneyTime, newRouteList);

                    // Pareto-dominance check
                    boolean dominated = false;
                    Set<State> existingStates = bestPaths.getOrDefault(route.getDestination(), new HashSet<>());
                    for (State existing : existingStates) {
                        if (isDominated(existing, newState, criterion)) {
                            dominated = true;
                            break;
                        }
                    }

                    if (!dominated) {
                        bestPaths.computeIfAbsent(route.getDestination(), k -> new HashSet<>()).add(newState);
                        pq.offer(newState);
                    }
                }
            }
        }

        switch (criterion.toLowerCase()) {
            case "cost":
                return bestPaths.getOrDefault(end, Collections.emptySet())
                        .stream()
                        .map(s -> new OptimalTravelSchedule(s.routeList, criterion, s.totalCost))
                        .collect(Collectors.toList()); // Collect the results as a List<OptimalRouteResult>
            case "time":
                return bestPaths.getOrDefault(end, Collections.emptySet())
                        .stream()
                        .map(s -> new OptimalTravelSchedule(s.routeList, criterion, s.totalJourneyTime))
                        .collect(Collectors.toList()); // Collect the results as a List<OptimalRouteResult>
            case "hops":
                return bestPaths.getOrDefault(end, Collections.emptySet())
                        .stream()
                        .map(s -> new OptimalTravelSchedule(s.routeList, criterion, s.transfers))
                        .collect(Collectors.toList()); // Collect the results as a List<OptimalRouteResult>
        }
        // Return the best paths as a list of Route objects, along with the corresponding criterion value
        return new ArrayList<>();
    }


    // Determine if a path is dominated by another (based on user preference)
    private boolean isDominated(State existing, State newState, String criterion) {
        switch (criterion.toLowerCase()) {
            case "cost":
                return existing.totalCost <= newState.totalCost &&
                        existing.totalJourneyTime <= newState.totalJourneyTime &&
                        existing.transfers <= newState.transfers;
            case "time":
                return existing.totalJourneyTime <= newState.totalJourneyTime &&
                        existing.totalCost <= newState.totalCost &&
                        existing.transfers <= newState.transfers;
            case "hops":
                return existing.transfers <= newState.transfers &&
                        existing.totalJourneyTime <= newState.totalJourneyTime &&
                        existing.totalCost <= newState.totalCost;
            default:
                return false;
        }
    }

    // State class to hold path information
    static class State {
        String node;
        int arrivalTime;
        int totalCost;
        int transfers;
        int totalJourneyTime;
        List<Route> routeList; // List of routes taken so far

        State(String node, int arrivalTime, int totalCost, int transfers, int totalJourneyTime, List<Route> routeList) {
            this.node = node;
            this.arrivalTime = arrivalTime;
            this.totalCost = totalCost;
            this.transfers = transfers;
            this.totalJourneyTime = totalJourneyTime;
            this.routeList = routeList;
        }

        // Override equals and hashCode to avoid duplicates in the final list
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            State state = (State) obj;
            return arrivalTime == state.arrivalTime &&
                    totalCost == state.totalCost &&
                    transfers == state.transfers &&
                    totalJourneyTime == state.totalJourneyTime &&
                    node.equals(state.node) &&
                    routeList.equals(state.routeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, arrivalTime, totalCost, transfers, totalJourneyTime, routeList);
        }
    }
}
