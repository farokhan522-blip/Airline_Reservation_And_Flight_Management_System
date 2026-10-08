package com.airline.system.model;

import com.airline.system.enums.FlightStatus;
import com.airline.system.dsa.PassengerLinkedList; // Aapki custom class [cite: 76]
import com.airline.system.dsa.BookingQueue;        // Aapki custom class (Waitlist) [cite: 72]
import jakarta.persistence.*;
import java.util.ArrayList;

@Entity
public class Flight {

    @Id
    @Column(name = "flight_id")
    private String flightId;

    private String source;
    private String destination;
    private String date;
    private String time;
    private int capacity;
    
    @Column(name = "base_price")
    private double basePrice; 
    
    @Column(name = "aircraft_id")
    private String aircraftId;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    @ManyToOne
    @JoinColumn(name = "airline_id")
    private Airline airline;

    // Pricing & Baggage Defaults
    private double businessPriceFactor = 2.5; 
    private double firstClassPriceFactor = 4.0; 
    private double economyBaggage = 20.0;
    private double businessBaggage = 40.0;
    private double firstClassBaggage = 50.0;

    // 🔥🔥🔥 DSA COMPOSITION (UML REQUIREMENTS) 🔥🔥🔥
    // @Transient isliye lagaya hai taake Hibernate inhein DB table me na dhoonde [cite: 47]
    
    @Transient 
    private PassengerLinkedList passengersListDSA; // Confirmed Passengers

    @Transient 
    private BookingQueue waitListDSA; // Waiting List

    @Transient 
    private SeatMap seatMap; // Seat Matrix (2D Array)

    // --- 1. DEFAULT CONSTRUCTOR ---
    public Flight() {
        // Composition: Object banne par hi DSAs initialize ho rahe hain
        this.passengersListDSA = new PassengerLinkedList();
        this.waitListDSA = new BookingQueue();
        // Default 60 capacity assume karke 10x6 map
        this.seatMap = new SeatMap(10, 6, new ArrayList<>());
    }

    // --- 2. PARAMETERIZED CONSTRUCTOR ---
    public Flight(String flightId, Airline airline, String source, String destination, String date, String time, int capacity, double basePrice, FlightStatus status, String aircraftId) {
        this.flightId = flightId;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.status = status;
        this.aircraftId = aircraftId;

        // 🔥 Composition: Constructors ke andar hi initialization 🔥
        this.passengersListDSA = new PassengerLinkedList();
        this.waitListDSA = new BookingQueue();
        
        // SeatMap Logic: Capacity ko 6 columns par divide karke rows nikaali
        int rows = (capacity > 0) ? (int) Math.ceil(capacity / 6.0) : 10;
        this.seatMap = new SeatMap(rows, 6, new ArrayList<>());
    }

    // --- GETTERS & SETTERS FOR DSA OBJECTS ---

    public PassengerLinkedList getPassengersListDSA() { return passengersListDSA; }
    public void setPassengersListDSA(PassengerLinkedList list) { this.passengersListDSA = list; }

    public BookingQueue getWaitListDSA() { return waitListDSA; }
    public void setWaitListDSA(BookingQueue queue) { this.waitListDSA = queue; }

    public SeatMap getSeatMap() { return seatMap; }
    public void setSeatMap(SeatMap seatMap) { this.seatMap = seatMap; }

    // --- STANDARD GETTERS & SETTERS ---
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }
    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public Airline getAirline() { return airline; }
    public void setAirline(Airline airline) { this.airline = airline; }
    public double getBusinessPriceFactor() { return businessPriceFactor; }
    public void setBusinessPriceFactor(double val) { this.businessPriceFactor = val; }
    public double getFirstClassPriceFactor() { return firstClassPriceFactor; }
    public void setFirstClassPriceFactor(double val) { this.firstClassPriceFactor = val; }
    public double getEconomyBaggage() { return economyBaggage; }
    public void setEconomyBaggage(double val) { this.economyBaggage = val; }
    public double getBusinessBaggage() { return businessBaggage; }
    public void setBusinessBaggage(double val) { this.businessBaggage = val; }
    public double getFirstClassBaggage() { return firstClassBaggage; }
    public void setFirstClassBaggage(double val) { this.firstClassBaggage = val; }

    
}