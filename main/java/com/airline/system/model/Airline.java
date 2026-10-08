package com.airline.system.model;

import com.airline.system.enums.MealPolicy;
import jakarta.persistence.*;

@Entity
public class Airline {
    @Id
    private String airlineID; // e.g., "PIA", "EK"
    private String name;
    private String country;

    @Enumerated(EnumType.STRING)
    private MealPolicy mealPolicy; // Meal policy Airline level par theek hai

    // --- CONSTRUCTORS ---
    public Airline() {}

    public Airline(String airlineID, String name, String country, MealPolicy mealPolicy) {
        this.airlineID = airlineID;
        this.name = name;
        this.country = country;
        this.mealPolicy = mealPolicy;
    }

    // --- GETTERS & SETTERS ---
    public String getAirlineID() { return airlineID; }
    public void setAirlineID(String airlineID) { this.airlineID = airlineID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public MealPolicy getMealPolicy() { return mealPolicy; }
    public void setMealPolicy(MealPolicy mealPolicy) { this.mealPolicy = mealPolicy; }
}