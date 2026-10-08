package com.airline.system.dsa;

import com.airline.system.model.Passenger;
import java.util.LinkedList;

public class PassengerHashMap {
    // Simple Hash Map implementation using Array of LinkedLists
    private int size = 100;
    private LinkedList<Passenger>[] table;

    public PassengerHashMap() {
        table = new LinkedList[size];
        for (int i = 0; i < size; i++) {
            table[i] = new LinkedList<>();
        }
    }

    private int getHash(String key) {
        // Simple hash function based on String length and chars
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash += c;
        }
        return hash % size;
    }

    public void put(String passportId, Passenger p) {
        int index = getHash(passportId);
        table[index].add(p);
    }

    public Passenger get(String passportId) {
        int index = getHash(passportId);
        for (Passenger p : table[index]) {
            if (p.getPassportNumber().equals(passportId)) {
                return p;
            }
        }
        return null;
    }
}