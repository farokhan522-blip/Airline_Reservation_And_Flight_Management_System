package com.airline.system.service;

import com.airline.system.enums.FlightStatus;
import com.airline.system.enums.BookingStatus;
import com.airline.system.dsa.CustomGraph;
import com.airline.system.dsa.FlightBST;
import com.airline.system.model.*;
import com.airline.system.repository.*;
import org.springframework.transaction.annotation.Transactional; // 👈 Zaroori hai

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class FlightService {

    @Autowired private FlightRepository flightRepo;
    @Autowired private BookingRepository bookingRepo;
    // 👇👇 YEH LINE ADD KAREIN (Sync ke liye) 👇👇
    @Autowired private AircraftService aircraftService;

    // 🔥 3 TREES (DSA ENGINE)
    private FlightBST idTree = new FlightBST("ID");            // Admin ID Search
    private FlightBST routeTree = new FlightBST("SOURCE");     // Passenger Source Search
    private FlightBST destTree = new FlightBST("DESTINATION"); // Passenger Dest Search

    // =========================================================
    // 1. SYSTEM STARTUP & DATA LOADING
    // =========================================================
    public void loadDataToBST() {
        // ✅ DB se SAB kuch uthao (Scheduled + Cancelled + Completed)
        List<Flight> flights = flightRepo.findAll(); 
        
        for (Flight f : flights) {
            // NOTE: Ek hi 'f' object teeno trees me ja raha hai.
            // Kisi ek tree se status change hoga to sab me reflect hoga (Java Reference).
            idTree.insert(f);
            routeTree.insert(f);
            destTree.insert(f);
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("🚀 System Starting: Loading Flights into 3-Tree DSA Engine...");
        loadDataToBST();
        System.out.println("✅ Data Loaded Successfully into Memory!");
    }

    // =========================================================
    // 2. WRITE OPERATIONS (SYNC DB & MEMORY)
    // =========================================================
    
    public void addFlight(Flight f) {
        flightRepo.save(f); // DB Backup
        // Update all Trees
        idTree.insert(f);
        routeTree.insert(f);
        destTree.insert(f);
    }

    public String scheduleNewFlight(Flight newFlight) {
        if (flightRepo.isAircraftBusy(newFlight.getAircraftId())) {
            return "ERROR: Aircraft " + newFlight.getAircraftId() + " is already assigned!";
        }
        newFlight.setStatus(FlightStatus.SCHEDULED);
        addFlight(newFlight); // Reusing addFlight method for sync
        return "SUCCESS";
    }

    // 🔥 CENTRALIZED STATUS UPDATE METHOD (Safe Sync) 🔥
    @Transactional // 👈 1. Ye Annotation DB Update guarantee karegi
    public void updateFlightStatus(String flightId, FlightStatus newStatus) {
        
        System.out.println("🛠️ Update Request: " + flightId + " -> " + newStatus);

        // 1. Update Database
        Flight f = flightRepo.findById(flightId).orElse(null);
        
        if (f != null) {
            f.setStatus(newStatus);
            flightRepo.saveAndFlush(f); // 👈 2. 'save' ki jagah 'saveAndFlush' (Immediate Write)
            System.out.println("✅ DB Updated Successfully!");

            // 3. Update Memory (DSA)
            Flight memoryFlight = idTree.search(flightId);
            if (memoryFlight != null) {
                memoryFlight.setStatus(newStatus); 
            }
            
            // 4. 🔥 SYNC AIRCRAFT SERVICE 🔥
            // Ye line AircraftService ko batayegi k "Dubara check karo kon free hai"
            // Is se aapka Re-route dropdown update ho jayega.
            aircraftService.syncFleetData(flightRepo.findAll());

        } else {
            System.err.println("❌ Error: Flight ID not found for update: " + flightId);
        }
    }

    // Wrapper for Cancel (Admin uses this)
    public void cancelFlight(String flightId) {
        updateFlightStatus(flightId, FlightStatus.CANCELLED);
    }

    // Wrapper for Complete (System uses this)
    public void completeFlight(String flightId) {
        updateFlightStatus(flightId, FlightStatus.COMPLETED);
    }

    // =========================================================
    // 3. SEPARATE METHODS FOR STATUS LISTS (Requested)
    // =========================================================

    // A. ALL FLIGHTS (Archives - Raw List)
    public List<Flight> getAllFlights() {
        return idTree.toList(); 
    }

    // B. SCHEDULED FLIGHTS (Active - For Dashboard & Booking)
    public List<Flight> getScheduledFlights() {
        return idTree.toList().stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED)
                .collect(Collectors.toList());
    }

    // C. CANCELLED FLIGHTS (For Admin Cancelled Page)
    public List<Flight> getCancelledFlights() {
        return idTree.toList().stream()
                .filter(f -> f.getStatus() == FlightStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    // D. COMPLETED FLIGHTS (For History/Logs)
    public List<Flight> getCompletedFlights() {
        return idTree.toList().stream()
                .filter(f -> f.getStatus() == FlightStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    // Legacy method support (Admin Dashboard uses this name)
    public List<Flight> getActiveFlights() {
        return getScheduledFlights();
    }

    // =========================================================
    // 4. READ OPERATIONS (SEARCH)
    // =========================================================

    // Admin ID Search
    public Flight getFlightById(String id) {
        Flight f = idTree.search(id);
        return (f != null) ? f : flightRepo.findById(id).orElse(null);
    }
    
    public Flight searchInBST(String id) { return idTree.search(id); }
    public Flight searchFlight(String id) { return getFlightById(id); }

    // 🔥 SMART PASSENGER SEARCH (Uses 3 Trees + Status Filter)
    public List<Flight> searchFlightsUser(String source, String destination) {
        List<Flight> results = new ArrayList<>();
        boolean hasSource = (source != null && !source.trim().isEmpty());
        boolean hasDest = (destination != null && !destination.trim().isEmpty());

        // Strategy: Use Specific Trees for O(log n) lookup
        if (hasSource && hasDest) {
            // Source Tree se nikalo, phir destination filter karo
            List<Flight> sourceFlights = routeTree.searchByProperty(source);
            results = sourceFlights.stream()
                    .filter(f -> f.getDestination().equalsIgnoreCase(destination))
                    .collect(Collectors.toList());
        } else if (hasSource) {
            results = routeTree.searchByProperty(source);
        } else if (hasDest) {
            results = destTree.searchByProperty(destination);
        } else {
            return getScheduledFlights(); // Fallback: Show all active
        }

        // ✅ FINAL FILTER: Only show SCHEDULED to passengers
        return results.stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED)
                .collect(Collectors.toList());
    }

    // =========================================================
    // 5. BOOKING & SEAT MAP UTILS
    // =========================================================
    public SeatMap getSeatMap(String flightId) {
        List<Booking> bookings = bookingRepo.findByFlight_FlightId(flightId);
        List<String> occupiedSeats = new ArrayList<>();

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED && b.getPassengers() != null) {
                for (BookingPassenger bp : b.getPassengers()) {
                    occupiedSeats.add(bp.getSeatNo());
                }
            }
        }
        Flight f = getFlightById(flightId);
        int cap = (f != null) ? f.getCapacity() : 60;
        return new SeatMap(cap, 6, occupiedSeats); 
    }


    //Shortest path algorithm (Dijkstra) use karke smart route dhoondne ke liye method 

    public List<Flight> getSmartRoute(String source, String destination) {
        // 1. Naya Graph banao
        CustomGraph graph = new CustomGraph();

        // 2. Database se saari flights lo
        List<Flight> allFlights = flightRepo.findAll();

        // 3. Graph mein flights bhar do (Nodes & Edges creation)
        for (Flight f : allFlights) {
            // Sirf woh flights add karein jo "Scheduled" hon (Completed/Cancelled nahi)
            if (f.getStatus() == FlightStatus.SCHEDULED) {
                graph.addRoute(f);
            }
        }

        // 4. Algorithm chalao
        return graph.findCheapestPath(source, destination);
    }
}