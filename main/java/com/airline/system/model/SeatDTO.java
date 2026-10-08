package com.airline.system.model;

public class SeatDTO {
    private String number;
    private boolean occupied;
    private boolean isInfant; // For LAP status

    public SeatDTO(String number, boolean occupied, boolean isInfant) {
        this.number = number;
        this.occupied = occupied;
        this.isInfant = isInfant;
    }

    // Getters
    public String getNumber() { return number; }
    public boolean isOccupied() { return occupied; }
    public boolean isInfant() { return isInfant; }
}