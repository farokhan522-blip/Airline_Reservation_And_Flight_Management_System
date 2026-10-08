package com.airline.system.dsa;

import com.airline.system.model.Flight;
import java.util.ArrayList;
import java.util.List;

class BSTNode {
    Flight flight;
    BSTNode left, right;

    public BSTNode(Flight flight) {
        this.flight = flight;
        this.left = null;
        this.right = null;
    }
}

public class FlightBST {
    private BSTNode root;
    private String sortType; // "ID", "SOURCE", or "DESTINATION"

    // ✅ Constructor ab 3 types handle karega
    public FlightBST(String sortType) {
        this.sortType = sortType;
        this.root = null;
    }
    
    public FlightBST() { this("ID"); } // Default

    public void insert(Flight flight) {
        root = insertRec(root, flight);
    }

    private BSTNode insertRec(BSTNode root, Flight flight) {
        if (root == null) {
            root = new BSTNode(flight);
            return root;
        }

        int compareResult;

        // 🔥 LOGIC: Check karo k ye Tree kis cheez ka hai
        if (this.sortType.equals("SOURCE")) {
            compareResult = flight.getSource().compareToIgnoreCase(root.flight.getSource());
            if (compareResult == 0) compareResult = 1; // Duplicates to Right
        } 
        else if (this.sortType.equals("DESTINATION")) { // 👈 NEW LOGIC
            compareResult = flight.getDestination().compareToIgnoreCase(root.flight.getDestination());
            if (compareResult == 0) compareResult = 1; // Duplicates to Right
        } 
        else {
            compareResult = flight.getFlightId().compareTo(root.flight.getFlightId());
        }

        if (compareResult < 0) {
            root.left = insertRec(root.left, flight);
        } else {
            root.right = insertRec(root.right, flight);
        }
        return root;
    }

    // --- Search Logic (Generic Method for Source/Dest) ---
    // Ye method recursive helper ko call karega
    public List<Flight> searchByProperty(String value) {
        List<Flight> results = new ArrayList<>();
        searchPropRec(root, value, results);
        return results;
    }

    private void searchPropRec(BSTNode root, String value, List<Flight> results) {
        if (root == null) return;

        // Check karo hum Source compare karein ya Destination
        String nodeValue = this.sortType.equals("DESTINATION") ? 
                           root.flight.getDestination() : root.flight.getSource();

        int cmp = value.compareToIgnoreCase(nodeValue);

        if (cmp < 0) {
            searchPropRec(root.left, value, results);
        } else if (cmp > 0) {
            searchPropRec(root.right, value, results);
        } else {
            // Match Found!
            results.add(root.flight);
            searchPropRec(root.right, value, results); // Check duplicates on right
        }
    }

    // ID Search (Only for ID Tree)
    public Flight search(String id) {
        return searchRec(root, id);
    }
    private Flight searchRec(BSTNode root, String id) {
        if (root == null) return null;
        if (root.flight.getFlightId().equals(id)) return root.flight;
        if (id.compareTo(root.flight.getFlightId()) < 0) return searchRec(root.left, id);
        else return searchRec(root.right, id);
    }

    // Tree to List
    public List<Flight> toList() {
        List<Flight> list = new ArrayList<>();
        inOrderRec(root, list);
        return list;
    }
    private void inOrderRec(BSTNode root, List<Flight> list) {
        if (root != null) {
            inOrderRec(root.left, list);
            list.add(root.flight);
            inOrderRec(root.right, list);
        }
    }
}