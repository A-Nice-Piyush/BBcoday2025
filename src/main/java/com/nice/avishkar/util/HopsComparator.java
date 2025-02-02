package com.nice.avishkar.util;

import com.nice.avishkar.model.Route;

import java.util.Comparator;

public class HopsComparator implements Comparator<Route> {
    @Override
    public int compare(Route r1, Route r2) {
        // First, compare by number of transfers
        int transferCompare = Long.compare(r1.getHops(), r2.getHops());
        if (transferCompare != 0) {
            return transferCompare;
        }

        // If transfers are the same, compare by time (arrival - departure)
        int timeCompare = Integer.compare(calculateTotalTime(r1), calculateTotalTime(r2));
        if (timeCompare != 0) {
            return timeCompare;
        }

        // If time is also the same, compare by cost
        return Integer.compare(r1.getCost(), r2.getCost());
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
}
