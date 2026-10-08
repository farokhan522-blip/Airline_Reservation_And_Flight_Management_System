package com.airline.system.service;


import com.airline.system.enums.FlightStatus;
import com.airline.system.dsa.UndoStack;
import com.airline.system.model.Flight;
import com.airline.system.repository.FlightRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class AdminService {

    @Autowired private FlightRepository flightRepo;
    @Autowired private FlightService flightService;
    @Autowired private AircraftService aircraftService;
    
    // DSA Stack for Undo
    private UndoStack history = new UndoStack(); 

    @PostConstruct
    public void init() {
        // Stack ko hamesha fresh start karo
        history = new UndoStack();
        System.out.println("✅ Admin Action Stack Initialized (Empty).");
    }

    public void deleteFlight(String id) {
    if (flightRepo.existsById(id)) {
        Flight f = flightRepo.findById(id).get();
        String oldStatus = f.getStatus().toString(); 
        
        // 1. Update in Database
        f.setStatus(FlightStatus.CANCELLED);
        flightRepo.save(f); 
        
        // 2. IMPORTANT: Update in BST Memory
        // flightService ke through BST search karein aur status wahan bhi set karein
        Flight bstFlight = flightService.getFlightById(id); // Ya jo bhi aapka search method hai
        if (bstFlight != null) {
            bstFlight.setStatus(FlightStatus.CANCELLED);
        }
        
        String log = "UPDATE_STATUS|" + f.getFlightId() + "|" + oldStatus;
        history.push(log);
        
        System.out.println("Flight " + id + " updated in DB and BST as CANCELLED.");
    }
}

// AdminService.java

public String undoLastAction() { // 👈 Return type String kiya
    if (!history.isEmpty()) {
        String lastLog = history.peek(); // Pehle dekho, pop mat karo abhi
        String[] parts = lastLog.split("\\|");
        
        if (parts[0].equals("UPDATE_STATUS")) {
            String flightId = parts[1];
            FlightStatus oldStatus = FlightStatus.valueOf(parts[2]);
            
            // Flight Data nikalo
            Flight f = flightRepo.findById(flightId).orElse(null);
            if (f == null) return "ERROR";

            // 🔥 CRITICAL CHECK: 
            // Agar hum is flight ko wapis SCHEDULED kar rahe hain, 
            // to check karo k iska jahaz kahin aur busy to nahi ho gaya?
            if (oldStatus == FlightStatus.SCHEDULED) {
                
                // Pehle sure karo k hamara DSA updated hai
                aircraftService.syncFleetData(flightRepo.findAll());

                // Ab check karo
                if (aircraftService.isPlaneInAir(f.getAircraftId())) {
                    System.out.println("❌ Undo Blocked: Aircraft " + f.getAircraftId() + " is currently busy in another flight!");
                    return "CONFLICT"; // Controller ko batao k masla hai
                }
            }

            // --- Agar yahan tak pohanch gaye to sab theek hai, ab Undo karo ---
            history.pop(); // Ab stack se nikalo
            
            f.setStatus(oldStatus);
            flightRepo.save(f);
            
            Flight bstFlight = flightService.getFlightById(flightId);
            if (bstFlight != null) bstFlight.setStatus(oldStatus);

            // Sync again to reflect changes
            aircraftService.syncFleetData(flightRepo.findAll());
            
            return "SUCCESS";
        }
    }
    return "EMPTY";
}
}