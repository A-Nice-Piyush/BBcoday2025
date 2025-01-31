package com.nice.avishkar.model;

import com.opencsv.bean.CsvBindByName;

public class CustomerRequests {
    @CsvBindByName(column = "RequestId")
    String requestId;
    @CsvBindByName(column = "CustomerName")
    String customerName;
    @CsvBindByName(column = "Source")
    String source;
    @CsvBindByName(column = "Destination")
    String destination;
    @CsvBindByName(column = "Criteria")
    Criteria criteria;

    public CustomerRequests()
    {

    }

    public CustomerRequests(String requestId, String customerName, String source, String destination, Criteria criteria) {
        this.requestId = requestId;
        this.customerName = customerName;
        this.source = source;
        this.destination = destination;
        this.criteria = criteria;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public Criteria getCriteria() {
        return criteria;
    }
    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

}
