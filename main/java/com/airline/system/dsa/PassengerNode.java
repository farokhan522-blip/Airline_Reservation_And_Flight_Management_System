package com.airline.system.dsa;

import com.airline.system.model.Passenger;

public class PassengerNode {
    public Passenger passenger;
    public PassengerNode next;

    public PassengerNode(Passenger passenger) {
        this.passenger = passenger;
        this.next = null;
    }
}