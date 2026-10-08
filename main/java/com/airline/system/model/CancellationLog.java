package com.airline.system.model;

import com.airline.system.enums.TravelClass;
import java.time.LocalDateTime;

public class CancellationLog {
    private String passengerName;
    private String flightId;
    private String seatNo;
    private TravelClass travelClass;
    private LocalDateTime timestamp;

    // Constructor
    public CancellationLog(String pName, String fId, String sNo, TravelClass tClass) {
        this.passengerName = pName;
        this.flightId = fId;
        this.seatNo = sNo;
        this.travelClass = tClass;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getPassengerName() { return passengerName; }
    public String getFlightId() { return flightId; }
    public String getSeatNo() { return seatNo; }
    public TravelClass getTravelClass() { return travelClass; }
    public LocalDateTime getTimestamp() { return timestamp; }
}