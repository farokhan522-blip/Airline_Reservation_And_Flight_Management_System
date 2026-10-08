package com.airline.system.repository;

import com.airline.system.enums.BookingStatus;
import com.airline.system.model.Booking;
import com.airline.system.model.Flight;
import com.airline.system.model.Passenger;
import com.airline.system.model.BookingPassenger; // Ye zaroori hai

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Ye method missing tha, isay add karein:
    List<Booking> findByFlight_FlightId(String flightId);
    
    // Ye bhi add kar lein agar missing ho:
    List<Booking> findByOwner_PassportNumber(String passportNumber);

    // Flight ID se count nikalne k liye
    int countByFlight_FlightId(String flightId);

    // Ye method flight ID ke zariye saari booked seat numbers ki list return karega
   @Query("SELECT bp.seatNo FROM BookingPassenger bp WHERE bp.booking.flight.flightId = :flightId AND bp.seatNo <> 'LAP'")
    List<String> findBookedSeatsByFlightId(@Param("flightId") String flightId);

    List<Booking> findByOwnerAndStatusIn(Passenger owner, Collection<BookingStatus> statuses);
    
    List<Booking> findByOwnerAndStatus(Passenger owner, BookingStatus status);
    
    List<Booking> findByOwner(Passenger owner);

}