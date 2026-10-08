package com.airline.system.model;

import com.airline.system.enums.BookingStatus;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingID;

    public Payment getPayment() {
        return payment;
    }
    private String bookingDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Passenger owner;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    // --- NEW UML CHANGE: One Booking has Many Passengers ---
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookingPassenger> passengers = new ArrayList<>();

    public Booking() {}

    public Booking(Flight flight, Passenger owner, String bookingDate) {
        this.flight = flight;
        this.owner = owner;
        this.bookingDate = bookingDate;
        this.status = BookingStatus.PENDING;
    }

    // Helper method to add passenger
    public void addPassenger(BookingPassenger bp) {
        passengers.add(bp);
        bp.setBooking(this);
    }

    // Getters and Setters
    public List<BookingPassenger> getPassengers() { return passengers; }
    public void setPassengers(List<BookingPassenger> passengers) { this.passengers = passengers; }
    
    // (Baqi purane Getters/Setters same rahenge...)
    public int getBookingID() { return bookingID; }
    public void setBookingID(int bookingID) { this.bookingID = bookingID; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String date) { this.bookingDate = date; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public Passenger getOwner() { return owner; }
    public void setOwner(Passenger owner) { this.owner = owner; }
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}