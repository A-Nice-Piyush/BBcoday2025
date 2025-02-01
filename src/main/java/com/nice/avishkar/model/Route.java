package com.nice.avishkar.model;

import com.opencsv.bean.CsvBindByName;

//Map<this.Source, List<this>>
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
    
    public Route()
    {

    }

    public Route(String source, String destination, String mode, String departureTime, String arrivalTime) {
        this.source = source;
        this.destination = destination;
        this.mode = mode;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
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
