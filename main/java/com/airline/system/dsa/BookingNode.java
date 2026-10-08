package com.airline.system.dsa;

import com.airline.system.model.Booking;

public class BookingNode {
    Booking booking; // Data ab Booking hai, Passenger nahi
    BookingNode next;

    public BookingNode(Booking booking) {
        this.booking = booking;
        this.next = null;
    }
}