package com.airline.system.model;

import com.airline.system.enums.PaymentStatus;
import jakarta.persistence.*;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paymentID;

    private double amount;
    private String paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    // Relationship: One Payment belongs to One Booking
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public Payment() {}

    public Payment(double amount, String paymentDate, Booking booking) {
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.booking = booking;
        this.status = PaymentStatus.PENDING; // Default
    }

    // Getters and Setters
    public int getPaymentID() { return paymentID; }
    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}