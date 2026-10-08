package com.airline.system.controller;


import com.airline.system.enums.FlightStatus; // Enum import check karein
import com.airline.system.enums.MealPolicy;
import com.airline.system.model.Airline;
import com.airline.system.model.Flight;
import com.airline.system.model.Passenger;
// Import zaroori hai
import com.airline.system.service.*;
import com.airline.system.repository.AirlineRepository;
import com.airline.system.repository.BookingRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private FlightService flightService;
    @Autowired private AircraftService aircraftService; // 👈 Custom Service
    @Autowired private PassengerService passengerService;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private AirlineRepository airlineRepo;
    

    // ==========================================
    // 1. DASHBOARD LOAD (http://localhost:8080/admin)
    // ==========================================
    @GetMapping("") // Ab sirf /admin likhne se khulega
    public String adminDashboard(HttpSession session, Model model) {
        
        // --- LOGIN VERIFICATION (ON) ---
        String role = (String) session.getAttribute("role");
        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login"; // Agar login nahi hai to login page par bhejo
        }

        try {

            // A. Optional: Data Consistency ke liye Sync karle (Safe Side)
            // Agar aapko lagta hai DB direct change nahi hoga, to is line ko hata bhi sakte hain
            aircraftService.syncFleetData(flightService.getAllFlights());

            model.addAttribute("freeAircraftsMap", aircraftService.getFreeAircraftsMap());

           // ✅ CHANGE: Sirf Active/Scheduled flights dashboard par dikhayen
            model.addAttribute("flights", flightService.getScheduledFlights());
            model.addAttribute("airlines", airlineRepo.findAll());
            model.addAttribute("totalBookings", bookingRepo.count());
            // 👇 Ye line add karein: MealPolicy Enum ki saari values bhejne k liye
            model.addAttribute("mealPolicies", MealPolicy.values());
        } catch (Exception e) {
            model.addAttribute("flights", new ArrayList<>());
            model.addAttribute("airlines", new ArrayList<>());
            model.addAttribute("totalBookings", 0);
            System.out.println("Error loading dashboard: " + e.getMessage());
        }

        // --- FILE NAME CHECK ---
        // Agar aapki file ka naam templates mein 'admin.html' hai to yahan "admin" likhein
        return "admin"; 
    }

    // ==========================================
    // 2. ADD AIRLINE (Fix: DB & Object Binding)
    // ==========================================
    @PostMapping("/addAirline")
    public String addAirline(@RequestParam("airlineID") String airlineID,
                             @RequestParam("name") String name,
                             @RequestParam("country") String country,
                             @RequestParam("mealPolicy") String mealPolicy, // 👈 Naya Parameter
                             HttpSession session) {
        
        if (session.getAttribute("role") == null) return "redirect:/login";

        try {
            // 1. Object create karein (Binding check karne ke liye manual creation behtar hai)
            Airline airline = new Airline();
            airline.setAirlineID(airlineID);
            airline.setName(name);
            airline.setCountry(country);

            // Enum value set karna (Check karein k Airline model me ye field ho)
            airline.setMealPolicy(MealPolicy.valueOf(mealPolicy));

            // 2. DB Persistence (Backup)
            airlineRepo.save(airline);
            
            // 3. DSA: Agar aapka koi Airline HashMap hai toh usay update karein
            // airlineService.addToMap(airline);

            System.out.println("✅ Airline Saved Successfully: " + airlineID);
        } catch (Exception e) {
            System.err.println("❌ Airline Save Error: " + e.getMessage());
        }
        return "redirect:/admin";
    }

// ==========================================
    // 3. FLIGHT SCHEDULING (Updated with Validation)
    // ==========================================
    @PostMapping("/add")
    public String addFlight(@ModelAttribute Flight flight, 
                            @RequestParam("airlineId") String airlineId, 
                            @RequestParam("date") String dateStr,
                            @RequestParam("mode") String mode) { // 👈 1. Mode param add kiya
        try {
            
            // 👇👇 2. VALIDATION LOGIC ADDED HERE 👇👇
            
            // CASE A: Agar Admin NEW Flight Schedule kar raha hai
            if (mode.equals("NEW")) {
                // Check karo k kya ye Aircraft ID pehle se system me hai?
                if (aircraftService.isAircraftExists(flight.getAircraftId())) {
                    System.out.println("❌ Error: Duplicate Aircraft ID for New Schedule");
                    return "redirect:/admin?error=DuplicateAircraft"; 
                }
            }
            
            // CASE B: Agar Admin RE-ROUTE kar raha hai
            else if (mode.equals("REROUTE")) {
                // Check karo k kya ye jahaz abhi Hawa me (Busy) hai?
                if (aircraftService.isPlaneInAir(flight.getAircraftId())) {
                    System.out.println("❌ Error: Aircraft is currently busy!");
                    return "redirect:/admin?error=PlaneBusy";
                }
            }
            // 👆👆 VALIDATION END 👆👆


            // --- BAQI AAPKA PURANA CODE ---

            // 3. DB Fetch for Airline Object
            Airline selectedAirline = airlineRepo.findById(airlineId)
                .orElseThrow(() -> new RuntimeException("Airline not found!"));

            // 4. Complete Flight Object
            flight.setAirline(selectedAirline);
            flight.setDate(dateStr); 
            flight.setStatus(FlightStatus.SCHEDULED);

            // 5. DSA & DB Persistence
            flightService.scheduleNewFlight(flight);

            System.out.println("✅ Flight " + flight.getFlightId() + " scheduled in BST and DB.");
            
        } catch (Exception e) {
            System.err.println("❌ Flight Scheduling Error: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==========================================
    // 4. PASSENGER SEARCH
    // ==========================================
    @GetMapping("/searchPassenger")
    public String searchPassenger(@RequestParam("passport") String passport, Model model, HttpSession session) {
        if (session.getAttribute("role") == null) return "redirect:/login";

        Passenger p = passengerService.searchByPassportMap(passport);
        
        // Dashboard data reload
        model.addAttribute("flights", flightService.getAllFlights());
        model.addAttribute("airlines", airlineRepo.findAll());
        model.addAttribute("totalBookings", bookingRepo.count());

        if(p != null) {
            model.addAttribute("searchedPassenger", p);
        } else {
            model.addAttribute("searchError", "Passenger not found!");
        }

        return "admin"; // 'admin.html' load hogi results ke sath
    }

    // ==========================================
    // 5. DELETE, UNDO, COMPLETE
    // ==========================================
    @PostMapping("/delete")
    public String deleteFlight(@RequestParam("id") String id, HttpSession session) {
        if (session.getAttribute("role") == null) return "redirect:/login";
        // ✅ CHANGE: FlightService use karein taake ID, Route, aur Dest teeno trees update hon
        adminService.deleteFlight(id);
        return "redirect:/admin";
    }

    @PostMapping("/undo")
    public String undoDelete(HttpSession session) {
        if (session.getAttribute("role") == null) return "redirect:/login";
        adminService.undoLastAction();
        return "redirect:/admin";
    }
    
    @PostMapping("/complete")
    public String completeFlight(@RequestParam("flightId") String flightId, HttpSession session) {
        if (session.getAttribute("role") == null) return "redirect:/login";
        flightService.completeFlight(flightId); 
        return "redirect:/admin";
    }


  @GetMapping("/searchFlight")
public String searchFlightBST(
        @RequestParam(required = false) String flightId, 
        @RequestParam(required = false) String viewMapId, 
        Model model) {
    
    List<Flight> allFlights = flightService.getAllFlights();
    model.addAttribute("allFlights", allFlights);

    // BST Search via FlightService
    if (flightId != null && !flightId.isEmpty()) {
        // Service ka method use kar rahe hain
        Flight found = flightService.getFlightById(flightId); 
        
        if (found != null) {
            allFlights.removeIf(f -> f.getFlightId().equals(found.getFlightId()));
            allFlights.add(0, found);
            model.addAttribute("highlightId", flightId);
            model.addAttribute("foundFlight", found);
        }
    }

    // Population Map Logic (DB/Passenger Bookings)
    if (viewMapId != null) {
    Flight mapFlight = flightService.getFlightById(viewMapId);
    
    // Ab is list mein sirf physical seats aayengi, Infants nahi
    List<String> physicalBookedSeats = bookingRepo.findBookedSeatsByFlightId(viewMapId);
    
    model.addAttribute("mapFlight", mapFlight);
    model.addAttribute("bookedSeats", physicalBookedSeats); 
    model.addAttribute("showModal", true);
    }

    return "admin_search_result";
}

@GetMapping("/flights/cancelled")
public String viewCancelledFlights(Model model) {
    // ✅ CHANGE: Purana manual filter hata diya, direct service call
    model.addAttribute("flights", flightService.getCancelledFlights());
    return "admin_cancelled_flights"; 
}


@GetMapping("/flights/completed")
public String viewCompletedFlights(Model model) {
    // ✅ NEW: Completed flights ki list
    model.addAttribute("flights", flightService.getCompletedFlights());
    return "admin_completed_flights"; 
}

// Service mein aik naya object rakhna parega: 
// private List<CancellationLog> recoveryLogs = new ArrayList<>();

@Autowired private BookingService bookingService; // 👈 Ye line laazmi add karein

@GetMapping("/cancellations/logs")
public String viewRecoveryLogs(Model model) {
    model.addAttribute("logs", bookingService.getRefundLogs()); // Memory list call
    return "admin_seat_recovery";
}

@PostMapping("/loadPlaneData")
public String loadPlaneData(@RequestParam("selectedAircraftId") String aircraftId, 
                            HttpSession session, 
                            Model model) {
    
    // 1. Security Check
    if (session.getAttribute("role") == null) return "redirect:/login";

    // 2. Logic: Data Nikalo (Ab ye Map<String, Integer> hai)
    Map<String, Integer> freePlanes = aircraftService.getFreeAircraftsDetails();
    
    // Map se sirf Capacity niklegi (Integer)
    Integer capacity = freePlanes.get(aircraftId);

    // 3. Temporary Flight Object Banao (Taake HTML ko Flight object mile)
    Flight tempFlight = new Flight();
    
    if (capacity != null) {
        tempFlight.setCapacity(capacity); // Map se ayi hui capacity set kardo
        // Note: Baggage aur Prices Flight class k andar default values (20, 40, etc) khud utha lenge
    }

    // 4. Data Model mein daalo
    model.addAttribute("preFillData", tempFlight); // Ab ye 'Flight' object hai, HTML khush rahega
    model.addAttribute("selectedAircraftId", aircraftId); 
    model.addAttribute("showReRouteModal", true); // Modal khula rakhne k liye

    // 5. Dashboard Refresh Logic
    aircraftService.syncFleetData(flightService.getAllFlights());
    model.addAttribute("freeAircraftsMap", aircraftService.getFreeAircraftsDetails());
    model.addAttribute("flights", flightService.getScheduledFlights());
    model.addAttribute("airlines", airlineRepo.findAll());
    model.addAttribute("mealPolicies", MealPolicy.values());

    return "admin"; 
}

// 2. WAITLIST QUEUE PAGE (FIFO VIEW - USING BOOKING QUEUE)
    @GetMapping("/queue/view")
    public String viewWaitlist(Model model) {
        
        // ✅ Ab data 'BookingService' se aayega
        model.addAttribute("queue", bookingService.getWaitingList());
        model.addAttribute("queueSize", bookingService.getQueueSize());
        
        return "admin_waitlist"; 
    }

    // 1. REGISTERED USERS PAGE
    @GetMapping("/passengers/registered")
    public String viewRegisteredPassengers(Model model) {
        // Service se list mangwa kar HTML ko bhej rahe hain
        model.addAttribute("passengers", passengerService.getRegisteredPassengers());
        return "admin_registered_users"; // HTML file ka naam
    }

    // AdminController.java ke andar ye methods add karein

// 1. SHOW ALL FLIGHTS FOR MANIFEST
@GetMapping("/manifest/flights")
public String listManifestFlights(Model model, HttpSession session) {
    if (session.getAttribute("role") == null) return "redirect:/login";
    
    // FlightService se saari active flights mangwaein
    model.addAttribute("flights", flightService.getAllFlights());
    return "admin_manifest_list"; // Ye HTML file hum aglay step me banayenge
}

// 2. VIEW PASSENGERS IN A SPECIFIC FLIGHT (Linked List DSA usage)
@GetMapping("/manifest/view/{flightId}")
public String viewFlightManifest(@PathVariable String flightId, Model model, HttpSession session) {
    if (session.getAttribute("role") == null) return "redirect:/login";

    
    Flight flight = flightService.searchFlight(flightId);

    if (flight != null) {
        
        model.addAttribute("passengers", flight.getPassengersListDSA().toList());
        model.addAttribute("flight", flight);
    }
    return "admin_manifest_view"; // Manifest display page
}

}