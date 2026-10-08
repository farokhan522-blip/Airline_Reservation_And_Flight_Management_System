package com.airline.system.repository;

import com.airline.system.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {

    // 1. Source aur Destination se flight dhoondo (Search Bar ke liye)
    // SQL: SELECT * FROM flight WHERE source = 'Lahore' AND destination = 'Dubai'
    List<Flight> findBySourceAndDestination(String source, String destination);

    // 2. Sirf Active flights dikhao (Cancelled nahi)
    List<Flight> findByStatus(String status);
    
    // 3. Price range mein dhoondo (Advanced Filter)
    List<Flight> findByBasePriceLessThan(double price);

   @Query("SELECT COUNT(f) > 0 FROM Flight f WHERE f.aircraftId = :aircraftId AND f.status = com.airline.system.enums.FlightStatus.SCHEDULED")
    boolean isAircraftBusy(String aircraftId);
    
    // 3. Active Flights
    @Query("SELECT f FROM Flight f WHERE f.status = com.airline.system.enums.FlightStatus.SCHEDULED")
    List<Flight> findAllActiveFlights();
}