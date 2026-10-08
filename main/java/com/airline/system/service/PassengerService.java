package com.airline.system.service;

import jakarta.annotation.PostConstruct;
import com.airline.system.model.Passenger;
import com.airline.system.repository.PassengerRepository;
import com.airline.system.dsa.PassengerHashMap;
import com.airline.system.dsa.PassengerLinkedList; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PassengerService {

    @Autowired private PassengerRepository passengerRepo;

    // --- DSA Objects ---
    private PassengerHashMap userMap = new PassengerHashMap(); // Username se login k liye
    private PassengerLinkedList passengerList = new PassengerLinkedList(); // List show karne k liye
    
    // 👇 NEW: Passport Search k liye alag HashMap (O(1) Search) 👇
    private PassengerHashMap passportMap = new PassengerHashMap(); 

    // --- STARTUP LOAD ---
    @PostConstruct
    public void init() {
        System.out.println("⏳ Loading Passengers into DSA...");
        
        List<Passenger> allPassengers = passengerRepo.findAll();
        
        // Reset DSAs
        userMap = new PassengerHashMap();
        passportMap = new PassengerHashMap(); // Reset Passport Map
        passengerList = new PassengerLinkedList();

        for (Passenger p : allPassengers) {
            // 1. Username Map (Login)
            if (p.getUsername() != null) {
                userMap.put(p.getUsername(), p); 
            }

            // 2. Passport Map (Admin Search)
            if (p.getPassportNumber() != null) {
                passportMap.put(p.getPassportNumber(), p);
            }

            // 3. Linked List (Display)
            passengerList.add(p);
        }
        System.out.println("✅ " + allPassengers.size() + " Passengers Loaded into Maps & List!");
    }

    // --- LOGIN (Username se) ---
    public Passenger login(String username, String password) {
        Passenger p = userMap.get(username); // Fast Lookup
        
        if (p != null && p.getPassword() != null && p.getPassword().equals(password)) {
            return p;
        }
        return passengerRepo.findByEmailAndPassword(username, password); // Fallback
    }

    // --- REGISTER ---
    public void register(Passenger p) {
        passengerRepo.save(p);
        addPassengerToDSA(p); // Register hotay hi RAM me b daal do
    }
    
    public PassengerLinkedList getAllPassengersList() {
        return passengerList;
    }

    // --- DSA SYNC METHOD ---
    public void addPassengerToDSA(Passenger p) {
        // Linked List update
        passengerList.add(p);
        
        // Username Map update
        if (p.getUsername() != null) {
            userMap.put(p.getUsername(), p);
        }

        // 👇 Passport Map Update (Zaroori hai) 👇
        if (p.getPassportNumber() != null) {
            passportMap.put(p.getPassportNumber(), p);
        }
    }

    // 👇👇 YEH HAI WO METHOD JO ADMIN CONTROLLER MAANG RAHA HAI 👇👇
    public Passenger searchByPassportMap(String passport) {
        // HashMap se O(1) complexity me dhoondy ga
        return passportMap.get(passport);
    }

    // PassengerService.java ke andar add karein
    public List<Passenger> getRegisteredPassengers() {
        return passengerRepo.findByIsRegisteredTrue();
    }
}