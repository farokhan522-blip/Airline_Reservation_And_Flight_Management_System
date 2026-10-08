package com.airline.system.service;

import com.airline.system.dsa.CustomFleetMap;
import com.airline.system.model.Flight;
import com.airline.system.repository.FlightRepository;
import com.airline.system.enums.FlightStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class AircraftService {

    @Autowired
    private FlightRepository flightRepo; 

    // Hamara Custom DSA (Size 100)
    private CustomFleetMap fleetMap = new CustomFleetMap(100);

    // ==========================================
    // 🔥 AUTO-LOAD ON SYSTEM RESTART
    // ==========================================
    @PostConstruct
    public void init() {
        System.out.println("🔄 System Restarted: Loading Fleet Data into Custom DSA...");
        List<Flight> allFlights = flightRepo.findAll();
        syncFleetData(allFlights);
        System.out.println("✅ Fleet Data Loaded Successfully!");
    }

    // ==========================================
    // 1. SYNC LOGIC (UPDATED FOR CANCELLED/COMPLETED)
    // ==========================================
    public void syncFleetData(List<Flight> allFlights) {
        fleetMap.clear();

        // STEP 1: Pehle sab Aircrafts ko Register karlo (Default: FREE)
        // Chahe wo Cancelled ho, Completed ho ya Scheduled, sab list me ajayenge.
        for (Flight f : allFlights) {
            // Agar plane pehle se nahi hai, toh add karo
            if (!fleetMap.containsKey(f.getAircraftId())) {
                fleetMap.put(f.getAircraftId(), f.getCapacity());
                // Note: put method by default isay Available/Free rakhta hai
            }
        }

        // STEP 2: Ab sirf unko BUSY mark karo jo abhi hawa mein hain (SCHEDULED)
        for (Flight f : allFlights) {
            if (f.getStatus() == FlightStatus.SCHEDULED) {
                fleetMap.setBusyStatus(f.getAircraftId(), true);
            }
            // Logic Explanation:
            // Agar flight CANCELLED ya COMPLETED hai, toh hum kuch nahi karenge.
            // Plane Step 1 ki wajah se already FREE (Available) hai.
            // Dropdown me wo automatically show ho jayega.
        }
    }

    // ==========================================
    // 2. GET FREE PLANES (Dropdown Logic)
    // ==========================================
    public Map<String, Integer> getFreeAircraftsForDropdown() {
        // Validation: Agar map khali hai to reload karo
        if (fleetMap.getFreePlanes().isEmpty()) {
            syncFleetData(flightRepo.findAll());
        }
        // Ye method sirf un planes ko return karega jo BUSY nahi hain
        // Yani Cancelled aur Completed walay yahan show honge
        return fleetMap.getFreePlanes();
    }

    // Controller support methods
    public Map<String, Integer> getFreeAircraftsDetails() {
        return fleetMap.getFreePlanes();
    }

    public Map<String, Integer> getFreeAircraftsMap() {
        return fleetMap.getFreePlanes();
    }

    // ==========================================
    // 3. VALIDATION
    // ==========================================
    public boolean isPlaneInAir(String aircraftId) {
        return fleetMap.isPlaneBusy(aircraftId);
    }

    public boolean isAircraftExists(String aircraftId) {
        return fleetMap.containsKey(aircraftId);
    }   
}