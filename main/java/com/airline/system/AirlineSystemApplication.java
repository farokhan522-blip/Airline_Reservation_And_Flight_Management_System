package com.airline.system;

import com.airline.system.model.Flight;
import com.airline.system.service.FlightService;
import com.airline.system.service.PassengerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class AirlineSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirlineSystemApplication.class, args);
    }

    // ✨ Bean ko hamesha main method se BAHAR likhein
    @Bean
    public CommandLineRunner verifyModels(FlightService flightService, PassengerService passengerService) {
        return args -> {
            System.out.println("\n============================================");
            System.out.println("🔍 TESTING OOP COMPOSITION & DATA OBJECTS");
            System.out.println("============================================");

            // 1. Flight aur Airline ka Relationship check (Composition)
            List<Flight> flights = flightService.getAllFlights();
            if (!flights.isEmpty()) {
                Flight f = flights.get(0);
                System.out.println("✅ Flight Found: " + f.getFlightId());
                
                // Airline object check [cite: 45, 47]
                if(f.getAirline() != null) {
                    System.out.println("🏢 Airline Linked: " + f.getAirline().getName());
                } else {
                    System.out.println("❌ ERROR: Airline object is NULL in Flight!");
                }
                
                // 2. Complex DSA Objects check 
                System.out.println("💺 SeatMap State: " + (f.getSeatMap() != null ? "INITIALIZED" : "NULL"));
            } else {
                System.out.println("⚠️ Warning: No flights in Database. Add a flight to verify objects.");
            }

            System.out.println("============================================\n");
        };
    }
}