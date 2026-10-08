package com.airline.system.dsa;

public class FleetNode {
    String key;         // Aircraft ID
    int capacity;       // Sirf Capacity yaad rakhenge
    boolean isBusy;     // Status
    FleetNode next;     // Chaining Link

    public FleetNode(String key, int capacity) {
        this.key = key;
        this.capacity = capacity;
        this.isBusy = false;
        this.next = null;
    }
}