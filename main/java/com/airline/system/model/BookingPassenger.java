package com.airline.system.model;

import com.airline.system.enums.MealType;
import com.airline.system.enums.TravelClass;
import jakarta.persistence.*;

@Entity
public class BookingPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String seatNo;

    // ❌ YAHAN SE NAME, AGE, PASSPORT HATA DIYA HAI
    // Kyunke ab ye data seedha Passenger table se ayega link hoke

    @Enumerated(EnumType.STRING)
    private TravelClass travelClass;
    private double baggageWeight;
    @Enumerated(EnumType.STRING)
    private MealType mealSelected;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger; // <--- YEH MAIN LINK HAI

    // Constructors, Getters, Setters (Bina name/age/passport k)
    public BookingPassenger() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSeatNo() { return seatNo; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public TravelClass getTravelClass() { return travelClass; }
    public void setTravelClass(TravelClass travelClass) { this.travelClass = travelClass; }
    public double getBaggageWeight() { return baggageWeight; }
    public void setBaggageWeight(double baggageWeight) { this.baggageWeight = baggageWeight; }
    public MealType getMealSelected() { return mealSelected; }
    public void setMealSelected(MealType mealSelected) { this.mealSelected = mealSelected; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
}