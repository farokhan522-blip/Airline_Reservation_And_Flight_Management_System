package com.airline.system.controller;

import com.airline.system.enums.BookingStatus;
import com.airline.system.enums.MealType;
import com.airline.system.enums.TravelClass;
import com.airline.system.model.*;
import com.airline.system.service.*;
import com.airline.system.repository.AdminRepository;
import com.airline.system.repository.BookingRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Controller
public class MainController {

    @Autowired private FlightService flightService;
    @Autowired private BookingService bookingService;
    @Autowired private PassengerService passengerService;
    @Autowired private AdminRepository adminRepo;
    @Autowired private BookingRepository bookingRepo; // 👈 Ye missing hai is liye error aa raha hai
    

    // ==========================================
    // 1. LOGIN & REGISTER FLOW (OLD LOGIC KEPT)
    // ==========================================

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Passenger p, @RequestParam("passportId") String passportId) {
        try {
            // 1. Passport ID ko set karein (HTML mein name 'passportId' hai, model mein 'passportNumber' ho sakta hai)
            p.setPassportNumber(passportId);
        
            // 2. IS_REGISTERED = 1 (True) set karna
            // Ye wahi flag hai jo aap chahte hain ke registration form se 1 ho jaye
            p.setRegistered(true); 

            // 3. Service call (Isme save aur DSA Map update dono hona chahiye)
            passengerService.register(p);
        
            System.out.println("✅ Registration Successful for: " + p.getFullName());
            return "redirect:/login?success=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/register?error=true";
        }
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                               @RequestParam String password, 
                               HttpSession session, 
                               Model model) {
        
        // Backdoor (For Testing)
        if (username.equals("admin") && password.equals("123")) {
            com.airline.system.model.Admin fakeAdmin = new com.airline.system.model.Admin();
            fakeAdmin.setUsername("admin");
            session.setAttribute("role", "ADMIN");
            session.setAttribute("user", fakeAdmin);
            return "redirect:/admin";
        }

        try {
            // Robust Admin Check (Ignores case/spaces)
            List<com.airline.system.model.Admin> admins = adminRepo.findAll();
            com.airline.system.model.Admin foundAdmin = null;
            for (com.airline.system.model.Admin a : admins) {
                if (a.getUsername().trim().equalsIgnoreCase(username.trim()) && 
                    a.getPassword().trim().equals(password.trim())) {
                    foundAdmin = a;
                    break;
                }
            }

            if (foundAdmin != null) {
                session.setAttribute("role", "ADMIN");
                session.setAttribute("user", foundAdmin);
                return "redirect:/admin";
            }

            // Passenger Check
            Passenger p = passengerService.login(username, password);
            if (p != null) {
                session.setAttribute("role", "PASSENGER");
                session.setAttribute("user", p);
                return "redirect:/user_dashboard"; 
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("error", "Invalid Username or Password!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================
    // 2. DASHBOARDS (UPDATED)
    // ==========================================


   @GetMapping("/user_dashboard")
public String userDashboard(HttpSession session, Model model) {
    // 1. User Authentication Check
    Passenger user = (Passenger) session.getAttribute("user");
    if (user == null) return "redirect:/login";
    
    model.addAttribute("passenger", user);

    // 2. Initialize Lists (Null Safety)
    List<Booking> myBookings = new ArrayList<>();
    List<Booking> travelHistory = new ArrayList<>();
    List<Payment> paymentHistory = new ArrayList<>();
    double totalSpent = 0.0;

    // 3. Logic: Categorize Bookings
    List<Booking> allBookings = bookingRepo.findAll();

    if (allBookings != null) {
        for (Booking b : allBookings) {
            // Passport number match logic
            if (b.getOwner() != null && b.getOwner().getPassportNumber().equals(user.getPassportNumber())) {
                
                // A. Active Bookings (Confirmed/Waiting)
                if (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.WAITING) {
                    myBookings.add(b);
                } 
                // B. Travel History (Completed/Cancelled)
                else if (b.getStatus() == BookingStatus.CONFIRMED    || b.getStatus() == BookingStatus.CANCELLED) {
                    travelHistory.add(b);
                }

                // C. Payment History Calculation
                if (b.getPayment() != null) {
                    paymentHistory.add(b.getPayment());
                    totalSpent += b.getPayment().getAmount();
                }
            }
        }
    }

    // 4. Model Attributes (Names MUST match your HTML)
    model.addAttribute("myBookings", myBookings);
    model.addAttribute("travelHistory", travelHistory);
    model.addAttribute("paymentHistory", paymentHistory);
    model.addAttribute("totalSpent", totalSpent);
    
    // Ensure this method exists in your FlightService
    model.addAttribute("flights", flightService.getScheduledFlights()); 
    
    return "user_dashboard"; 
}

    // ==========================================
    // 3. SEARCH (UPDATED)
    // ==========================================

@GetMapping("/search")
    public String searchFlights(@RequestParam(value = "source", required = false) String source, 
                                @RequestParam(value = "destination", required = false) String destination, 
                                Model model) {
        
        // Service call
        List<Flight> results = flightService.searchFlightsUser(source, destination);
        
        // Results bhejo
        model.addAttribute("flights", results);
        
        // User ne kya search kiya tha, wo wapas input box mein dikhane k liye
        model.addAttribute("searchSource", source);
        model.addAttribute("searchDest", destination);
        
        // Agar list khali hai to flag set karo (Frontend message k liye)
        if (results.isEmpty()) {
            model.addAttribute("noRecords", true);
        }
        
        return "search"; 
    }

    // ==========================================
    // 4. NEW BOOKING FLOW (MULTI-STEP)
    // ==========================================

// STEP 1: Select Multiple Seats (Visual Map)
    @GetMapping("/booking/select-seats")
    public String selectSeatsPage(@RequestParam String flightId, Model model, HttpSession session) {
        
        // 1. Security Check
        if (session.getAttribute("user") == null) return "redirect:/login";
        
        // 2. Get Flight Data
        Flight flight = flightService.searchFlight(flightId);

        // --- OLD WORKING LOGIC (Seats fetch karne k liye) ---
        // Kyunke ye method aapke paas pehle se chal raha tha, hum isay hi use karenge
        SeatMap map = flightService.getSeatMap(flightId);
        model.addAttribute("bookedSeats", map.getBookedSeatsSet());
        // ----------------------------------------------------

        // --- NEW GENERIC LOGIC (Rows calculate karne k liye) ---
        int totalRows = (int) Math.ceil(flight.getCapacity() / 6.0);
        
        // First Class = Top 15% rows
        int firstLimit = Math.max(1, (int) (totalRows * 0.15)); 
        
        // Business Class = Next 25% rows
        int busLimit = firstLimit + (int) Math.ceil(totalRows * 0.25);
        
        // HTML/JS ko batane k liye limits bhej rahe hain
        model.addAttribute("firstClassLimit", firstLimit);
        model.addAttribute("businessClassLimit", busLimit);
        // -------------------------------------------------------

        model.addAttribute("flight", flight);
        
        return "seat_selection";
    }

    // ==========================================
    // STEP 2: Enter Details (MERGED & FIXED)
    // ==========================================
   @PostMapping("/booking/passenger-details")
    public String showPassengerForm(@RequestParam String flightId, 
                                    @RequestParam("selectedSeats") List<String> selectedSeats, 
                                    Model model, HttpSession session) {
        
        Passenger currentUser = (Passenger) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        Flight flight = flightService.searchFlight(flightId);
        List<SeatDTO> seatDetails = new ArrayList<>();

        // Limits Calculation
        int totalRows = (int) Math.ceil(flight.getCapacity() / 6.0);
        int firstLimit = Math.max(1, (int) (totalRows * 0.15));
        int busLimit = firstLimit + (int) Math.ceil(totalRows * 0.25);

        // 👇 SAFETY CHECK: Agar DB me factors 0 hain to Default set karo
        double firstFactor = (flight.getFirstClassPriceFactor() > 0) ? flight.getFirstClassPriceFactor() : 4.0;
        double busFactor = (flight.getBusinessPriceFactor() > 0) ? flight.getBusinessPriceFactor() : 2.5;

        for (String seat : selectedSeats) {
            int row = Integer.parseInt(seat.replaceAll("[^0-9]", ""));
            TravelClass tClass;
            double price;

            if (row <= firstLimit) {
                tClass = TravelClass.FIRSTCLASS;
                price = flight.getBasePrice() * firstFactor; // ✅ Fixed
            } else if (row <= busLimit) {
                tClass = TravelClass.BUSINESS;
                price = flight.getBasePrice() * busFactor;   // ✅ Fixed
            } else {
                tClass = TravelClass.ECONOMY;
                price = flight.getBasePrice();
            }
            seatDetails.add(new SeatDTO(seat, tClass, price));
        }

        model.addAttribute("flight", flight);
        model.addAttribute("seatDetails", seatDetails); 
        model.addAttribute("mealTypes", MealType.values()); 
        model.addAttribute("currentUser", currentUser);
        
        return "passenger_form";
    }

    // Helper DTO Class (Ise Controller class ke andar hi rehne dein, last bracket se pehle)
    public class SeatDTO {
        public String seatNo;
        public TravelClass travelClass;
        public double price;
    
        public SeatDTO(String s, TravelClass t, double p) { 
           this.seatNo = s; this.travelClass = t; this.price = p; 
        }
    }

   
    // STEP 3: Payment Summary (FIXED)
    // STEP 3: Payment Summary (FIXED & UPDATED)
    // ==========================================
    // STEP 3: Payment Summary (UPDATED: Added pPrices)
    // ==========================================
    // ==========================================
    // STEP 3: Payment Summary (FINAL FIX: Added pMeals)
    // ==========================================
    // ==========================================
    // STEP 3: Payment Summary (FIXED PRICES & LOGIC)
    // ==========================================
   // ==========================================
    // STEP 3: Payment Summary (FIXED: MATCHING UI FACTORS)
    // ==========================================
    @PostMapping("/booking/payment-summary")
public String paymentPage(@RequestParam String flightId,
                          @RequestParam("seatNo") List<String> seatNos,
                          @RequestParam("pName") List<String> pNames,
                          @RequestParam("pAge") List<Integer> pAges,
                          @RequestParam("pPassport") List<String> pPassports,
                          @RequestParam("pMeal") List<String> pMeals,
                          Model model, HttpSession session) {

    Flight flight = flightService.getFlightById(flightId);
    
    // --- 1. SAFETY CHECKS ---
    double baseFare = (flight.getBasePrice() > 0) ? flight.getBasePrice() : 5000.0;
    double firstFactor = (flight.getFirstClassPriceFactor() > 0) ? flight.getFirstClassPriceFactor() : 4.0;
    double busFactor = (flight.getBusinessPriceFactor() > 0) ? flight.getBusinessPriceFactor() : 2.5;

    // Processed Data Lists
    List<String> pClasses = new ArrayList<>();
    List<Double> pPrices = new ArrayList<>(); 
    List<String> processedSeats = new ArrayList<>(); // To handle "LAP" logic
    double totalAmount = 0.0;

    // --- 2. CORE LOGIC PROCESSING (JAVA ONLY) ---
    for (int i = 0; i < pNames.size(); i++) {
        int age = pAges.get(i);
        String seat = seatNos.get(i);
        double seatPrice = 0.0;
        String currentClass = "ECONOMY";

        // Step A: Row identification for Class Mapping
        int row = Integer.parseInt(seat.replaceAll("[^0-9]", ""));
        if (row <= 2) currentClass = "FIRSTCLASS";
        else if (row <= 5) currentClass = "BUSINESS";

        // Step B: INFANT LOGIC (Age <= 2)
        if (age <= 2) {
            seatPrice = 0.0;          // No charges for infants
            processedSeats.add("LAP"); // Freed up the seat
            System.out.println("Infant Detected: " + pNames.get(i) + " marked as LAP.");
        } else {
            // Normal Passenger Pricing
            if (currentClass.equals("FIRSTCLASS")) {
                seatPrice = baseFare * firstFactor;
            } else if (currentClass.equals("BUSINESS")) {
                seatPrice = baseFare * busFactor;
            } else {
                seatPrice = baseFare;
            }
            processedSeats.add(seat); // Occupies original seat
        }

        pClasses.add(currentClass);
        pPrices.add(seatPrice);
        totalAmount += seatPrice;
    }

    // --- 3. MODEL ATTRIBUTES ---
    model.addAttribute("flight", flight);
    model.addAttribute("seatNos", processedSeats); // Pass "LAP" for infants
    model.addAttribute("pNames", pNames);
    model.addAttribute("pAges", pAges);
    model.addAttribute("pPassports", pPassports);
    model.addAttribute("pMeals", pMeals);
    model.addAttribute("pClasses", pClasses); 
    model.addAttribute("pPrices", pPrices);   
    model.addAttribute("totalAmount", totalAmount);

    return "payment"; 
}

    // STEP 4: Final Confirm (Save to DB)
    @Autowired private com.airline.system.repository.PassengerRepository passengerRepo; // Ye upar autowire kar lena

    // STEP 4: Final Confirm (Saving to DB + DSA Update)
    @PostMapping("/booking/confirm")
public String confirmBooking(@RequestParam String flightId,
                             @RequestParam("seatNo") List<String> seatNos,
                             @RequestParam("pName") List<String> pNames,
                             @RequestParam("pAge") List<Integer> pAges,
                             @RequestParam("pPassport") List<String> pPassports,
                             @RequestParam("pClass") List<String> pClasses, 
                             @RequestParam("pMeal") List<String> pMeals,    
                             HttpSession session, Model model) {

    Passenger loggedInUser = (Passenger) session.getAttribute("user");
    if (loggedInUser == null) return "redirect:/login"; 

    List<BookingPassenger> bookingPassengersList = new ArrayList<>();
    Flight f = flightService.searchFlight(flightId);

    for (int i = 0; i < seatNos.size(); i++) {
        Passenger traveler;
        int age = pAges.get(i); 

        // 1. Passenger Identification & DB Backup
        if (i == 0 && pNames.get(i).equalsIgnoreCase(loggedInUser.getFullName())) {
            traveler = loggedInUser;
            if(traveler.getPassportNumber() == null) traveler.setPassportNumber(pPassports.get(i));
            passengerRepo.save(traveler);
        } else {
            traveler = new Passenger();
            traveler.setFullName(pNames.get(i));
            traveler.setAge(age);
            traveler.setPassportNumber(pPassports.get(i));
            traveler.setRegistered(false); // Sirf booking ke liye bane passengers 0 honge
            passengerRepo.save(traveler); 
        }

        // 2. DSA UPDATE: HashMap/Linked List update
        passengerService.addPassengerToDSA(traveler); 

        // 3. Create Booking Passenger Entry with PERSISTENCE LOGIC
        BookingPassenger bp = new BookingPassenger();
        bp.setPassenger(traveler);
        bp.setTravelClass(TravelClass.valueOf(pClasses.get(i))); 
        bp.setMealSelected(MealType.valueOf(pMeals.get(i)));
        
        // 👇👇 RE-APPLYING JAVA LOGIC FOR FINAL SAVE 👇👇
        if (age <= 2) {
            // Infant: Seat "LAP" aur baggage 0 save hoga
            bp.setSeatNo("LAP"); 
            bp.setBaggageWeight(0.0);
            System.out.println("Finalizing: Infant " + pNames.get(i) + " saved with LAP seat.");
        } else {
            // Adult: Numeric seat aur class-wise baggage limits
            bp.setSeatNo(seatNos.get(i));
            
            if(bp.getTravelClass() == TravelClass.ECONOMY) bp.setBaggageWeight(f.getEconomyBaggage());
            else if(bp.getTravelClass() == TravelClass.BUSINESS) bp.setBaggageWeight(f.getBusinessBaggage());
            else bp.setBaggageWeight(f.getFirstClassBaggage());
        }
        // ---------------------------------------------------

        bookingPassengersList.add(bp);
    }

    try {
        // Core DSA logic and DB commit
        bookingService.createBooking(loggedInUser, flightId, bookingPassengersList);
        model.addAttribute("message", "✅ Booking Successful! Your tickets are issued.");
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("message", "❌ Booking Failed: " + e.getMessage());
    }
    
    return "confirmation";
}


@PostMapping("/user/cancelBooking")
public String cancelBooking(@RequestParam("bookingId") int bookingId, HttpSession session) { // Change Long to int
    Passenger user = (Passenger) session.getAttribute("user");
    if (user == null) return "redirect:/login";

    Booking booking = bookingRepo.findById(bookingId).orElse(null);

    if (booking != null) {
        for (BookingPassenger bp : booking.getPassengers()) {
            if (bp.getPassenger().getAge() > 2) {
                // Ye method ab niche service mein add kiya hai
                bookingService.recordCancellation(
                    bp.getPassenger().getFullName(),
                    booking.getFlight().getFlightId(),
                    bp.getSeatNo(),
                    bp.getTravelClass()
                );
            }
        }
        bookingService.processCancellation(bookingId);
    }
    return "redirect:/user_dashboard?cancelled=true";
}

// 1. My Bookings Page (Active & Waiting)
@GetMapping("/user/bookings")
public String viewUserBookings(HttpSession session, Model model) {
    Passenger user = (Passenger) session.getAttribute("user");
    if (user == null) return "redirect:/login";

    // Confirmed aur Waiting bookings filter karein
    List<Booking> bookings = bookingRepo.findByOwnerAndStatusIn(user, 
            List.of(BookingStatus.CONFIRMED, BookingStatus.WAITING));
    
    model.addAttribute("passenger", user);
    model.addAttribute("bookings", bookings);
    return "user_bookings_detail"; 
}

// 2. Travel History Page (Using Linked List Logic)
@GetMapping("/user/history")
public String viewUserHistory(HttpSession session, Model model) {
    Passenger user = (Passenger) session.getAttribute("user");
    if (user == null) return "redirect:/login";

    // Sirf Completed bookings (Linked List demonstration)
    List<Booking> history = bookingRepo.findByOwnerAndStatus(user, BookingStatus.CONFIRMED);
    
    model.addAttribute("passenger", user);
    model.addAttribute("history", history);
    return "user_history_detail";
}

// 3. Payment Logs Page
@GetMapping("/user/payments")
public String viewUserPayments(HttpSession session, Model model) {
    Passenger user = (Passenger) session.getAttribute("user");
    if (user == null) return "redirect:/login";

    List<Booking> allUserBookings = bookingRepo.findByOwner(user);
    List<Payment> payments = new ArrayList<>();
    for(Booking b : allUserBookings) {
        if(b.getPayment() != null) payments.add(b.getPayment());
    }
    
    model.addAttribute("passenger", user);
    model.addAttribute("payments", payments);
    return "user_payments_detail";
}


@GetMapping("/searchSmart")
public String searchSmartRoute(@RequestParam String from, @RequestParam String to, Model model) {
    
    // Graph Algorithm se rasta mango
    List<Flight> smartPath = flightService.getSmartRoute(from, to);
    
    if (smartPath.isEmpty()) {
        model.addAttribute("error", "No route found between these cities.");
    } else {
        // Total price calculate karo
        double totalCost = smartPath.stream().mapToDouble(Flight::getBasePrice).sum();
        
        model.addAttribute("flights", smartPath); // List of flights (Connecting)
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("isSmartSearch", true); // UI pe alag dikhane k liye
    }
    
    return "search"; // Wahi purana result page use kar sakte hain
}

}