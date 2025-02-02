package com.nice.avishkar.model;

import com.opencsv.bean.CsvBindByName;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Route {
    @CsvBindByName(column = "Source")
    String source;

    @CsvBindByName(column = "Destination")
    String destination;

    @CsvBindByName(column = "Mode")
    String mode;
    @CsvBindByName(column = "DepartureTime")
    String departureTime;
    @CsvBindByName(column = "ArrivalTime")
    String arrivalTime;
    @CsvBindByName(column = "Cost")
    Integer cost;

    long totalTimeForTravel;

    long hops;
    
    public Route()
    {

    }

    public Route(String source, String destination, String mode, String departureTime, String arrivalTime, Integer cost, Long hops) {
        this.source = source;
        this.destination = destination;
        this.mode = mode;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.cost = cost;
        this.hops = hops;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public long getTotalTimeForTravel()
    {
        if(totalTimeForTravel == 0l)
        {
            LocalTime t1 = LocalTime.parse(this.departureTime);
            LocalTime t2 = LocalTime.parse(this.arrivalTime);

            // Calculate the difference in minutes
            long differenceInMinutes = ChronoUnit.MINUTES.between(t1, t2);
            return differenceInMinutes;
        }

        return totalTimeForTravel;
    }

    public void setTotalTimeForTravel(long totalTimeForTravel)
    {
        this.totalTimeForTravel = totalTimeForTravel;
    }

    public long getHops() {
        return hops;
    }

    public void setHops(long hops) {
        this.hops = hops;
    }

    @Override
    public String toString() {
        return "Route{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", mode='" + mode + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }
}
