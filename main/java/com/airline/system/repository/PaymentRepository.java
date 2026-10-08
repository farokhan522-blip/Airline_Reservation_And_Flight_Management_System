package com.airline.system.repository;

import com.airline.system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Transaction history by booking
    Payment findByBooking_BookingID(int bookingId);
}