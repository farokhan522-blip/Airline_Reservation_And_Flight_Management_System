package com.airline.system.service;

import com.airline.system.enums.*;
import com.airline.system.model.*;
import com.airline.system.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Service
public class BookingService {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private FlightService flightService;

    // ✅ PRESERVED: Purana recovery logs ka record
    private List<CancellationLog> recoveryLogs = new ArrayList<>();

    // =========================================================
    // 1. SYSTEM STARTUP: Sync DB with Flight-specific DSAs
    // =========================================================
    @PostConstruct
    public void init() {
        System.out.println("⏳ Syncing Database with Flight DSAs...");
        List<Booking> allBookings = bookingRepo.findAll();
        
        for (Booking b : allBookings) {
            Flight flight = b.getFlight();
            if (flight == null) continue;

            // ✅ NEW LOGIC: Har flight ki apni queue aur list bharna [cite: 27, 72, 76]
            if (b.getStatus() == BookingStatus.WAITING) {
                flight.getWaitListDSA().enqueue(b); 
            } else if (b.getStatus() == BookingStatus.CONFIRMED) {
                for (BookingPassenger bp : b.getPassengers()) {
                    flight.getPassengersListDSA().add(bp.getPassenger());
                }
            }
        }
        System.out.println("✅ DSA Re-populated successfully.");
    }

    // =========================================================
    // 2. CREATE BOOKING: Smart Capacity + DSA Insertion
    // =========================================================
    public void createBooking(Passenger owner, String flightId, List<BookingPassenger> passengers) {
        Flight flight = flightService.searchFlight(flightId);
        
        // ✅ PRESERVED: Infant (LAP) logic and seat count
        int currentBookings = bookingRepo.countByFlight_FlightId(flightId);
        long seatsNeeded = passengers.stream()
            .filter(bp -> bp.getPassenger().getAge() > 2)
            .count();
            
        int availableSeats = flight.getCapacity() - currentBookings;
        
        // ✅ NEW: Specific Flight ki queue ka faisla [cite: 27]
        BookingStatus statusToSet = (seatsNeeded > availableSeats) ? BookingStatus.WAITING : BookingStatus.CONFIRMED;
        boolean addToQueue = (statusToSet == BookingStatus.WAITING);

        Booking booking = new Booking(flight, owner, LocalDate.now().toString());
        booking.setStatus(statusToSet);

        double totalAmount = 0;
        for (BookingPassenger bp : passengers) {
            double price = calculateTicketPrice(bp, flight); // ✅ PRESERVED: Price logic
            totalAmount += price;
            bp.setBooking(booking);
            booking.getPassengers().add(bp);
        }

        Payment payment = new Payment(totalAmount, LocalDate.now().toString(), booking);
        booking.setPayment(payment);
        bookingRepo.save(booking);

        // ✅ NEW: Flight-specific DSA Update 
        if (addToQueue) {
            flight.getWaitListDSA().enqueue(booking); // Usi flight ki line me lagao
        } else {
            for (BookingPassenger bp : passengers) {
                flight.getPassengersListDSA().add(bp.getPassenger()); // Usi flight ki manifest me daalo
            }
        }
    }

    // =========================================================
    // 3. CANCELLATION: Seat Recovery + Auto-Promotion
    // =========================================================
    public void processCancellation(int bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking != null) {
            Flight flight = booking.getFlight();
            
            // Seat free karo [cite: 34, 35]
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepo.save(booking);
            
            // ✅ PRESERVED: Record cancellation in logs 
            recordCancellation(booking.getOwner().getFullName(), flight.getFlightId(), "AUTO", TravelClass.ECONOMY);

            // ✅ NEW: Automatically promote next passenger from THIS flight's queue [cite: 36, 68]
            processSpecificWaitingQueue(flight);
        }
    }

    private void processSpecificWaitingQueue(Flight flight) {
        // Sirf is flight ki line se agla banda uthao (FIFO) [cite: 72]
        if (!flight.getWaitListDSA().isEmpty()) {
            Booking nextInLine = flight.getWaitListDSA().dequeue();
            nextInLine.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(nextInLine);
            
            // Confirmed list me add karo [cite: 76]
            for (BookingPassenger bp : nextInLine.getPassengers()) {
                flight.getPassengersListDSA().add(bp.getPassenger());
            }
            System.out.println("DSA Update: Next passenger promoted for Flight " + flight.getFlightId());
        }
    }

    // =========================================================
    // 4. PRESERVED HELPERS (Price & Logs)
    // =========================================================

    public double calculateTicketPrice(BookingPassenger bp, Flight flight) {
        // ✅ PRESERVED: Infant travel is free
        if (bp.getPassenger().getAge() <= 2) return 0.0;

        double basePrice = flight.getBasePrice();
        double multiplier = 1.0;
        if (bp.getTravelClass() == TravelClass.BUSINESS) multiplier = flight.getBusinessPriceFactor();
        else if (bp.getTravelClass() == TravelClass.FIRSTCLASS) multiplier = flight.getFirstClassPriceFactor();

        double mealCost = (bp.getMealSelected() != MealType.NONE) ? 20.0 : 0.0;
        return (basePrice * multiplier) + mealCost;
    }

    public void recordCancellation(String pName, String fId, String sNo, TravelClass tClass) {
        recoveryLogs.add(new CancellationLog(pName, fId, sNo, tClass));
    }

    public List<CancellationLog> getRefundLogs() {
        return recoveryLogs;
    }

    public List<Booking> getWaitingList(String flightId) {
        Flight f = flightService.searchFlight(flightId);
        return (f != null) ? f.getWaitListDSA().toList() : new ArrayList<>();
    }

    // BookingService.java ke andar ye methods replace/add karein

    // 1. GLOBAL WAITING LIST (Saari flights ki waitlist jama karke return karega)
    public List<Booking> getWaitingList() {
        List<Booking> allWaiting = new ArrayList<>();
        
        // FlightService se saari flights lo aur unki queues check karo
        // (Agar aapke paas FlightService me getAllFlights hai)
        List<Flight> allFlights = flightService.getAllFlights(); 
        
        for (Flight f : allFlights) {
            if (f.getWaitListDSA() != null) {
                // Har flight ki custom queue ko list me convert karke add karo
                allWaiting.addAll(f.getWaitListDSA().toList());
            }
        }
        return allWaiting;
    }

    // 2. GLOBAL QUEUE SIZE
    public int getQueueSize() {
        int totalSize = 0;
        List<Flight> allFlights = flightService.getAllFlights();
        
        for (Flight f : allFlights) {
            if (f.getWaitListDSA() != null) {
                totalSize += f.getWaitListDSA().size();
            }
        }
        return totalSize;
    }
}