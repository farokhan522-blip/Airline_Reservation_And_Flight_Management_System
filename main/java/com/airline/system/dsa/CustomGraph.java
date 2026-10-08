package com.airline.system.dsa;

import com.airline.system.model.Flight;
import java.util.*;

public class CustomGraph {

    // Adjacency List: Har shehar (Key) ke agay us se nikalne wali flights (Value)
    private Map<String, List<Edge>> adjList = new HashMap<>();

    // 1. Edge Class (Connection represent karne k liye)
    private class Edge {
        String destination;
        double price;
        Flight flightDetails; // Flight object taake hum user ko flight dikha sakein

        public Edge(String dest, double price, Flight flight) {
            this.destination = dest;
            this.price = price;
            this.flightDetails = flight;
        }
    }

    // 2. Add Route (Graph Build karne k liye)
    public void addRoute(Flight flight) {
        // Agar shehar pehle se list mein nahi hai, toh add karo
        adjList.putIfAbsent(flight.getSource(), new ArrayList<>());
        adjList.putIfAbsent(flight.getDestination(), new ArrayList<>());

        // Source se Destination tak ka edge banao
        Edge newEdge = new Edge(flight.getDestination(), flight.getBasePrice(), flight);
        adjList.get(flight.getSource()).add(newEdge);
    }

    // 3. Dijkstra Algorithm (Sasta Rasta Dhoondne k liye)
    public List<Flight> findCheapestPath(String start, String end) {
        // Prices table (City -> Minimum Cost found so far)
        Map<String, Double> minCost = new HashMap<>();
        // Parent pointer (City -> Jis Flight se hum yahan pohanchay)
        Map<String, Flight> parentMap = new HashMap<>();
        // Priority Queue (Sasta rasta pehle check karega)
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));

        // Initialization
        for (String city : adjList.keySet()) {
            minCost.put(city, Double.MAX_VALUE);
        }
        minCost.put(start, 0.0);
        pq.add(new Node(start, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentCity = current.city;

            // Agar manzil mil gayi
            if (currentCity.equals(end)) break;

            // Agar purana rasta mehnga tha, toh skip karo
            if (current.cost > minCost.getOrDefault(currentCity, Double.MAX_VALUE)) continue;

            // Parosi shehron ko check karo
            if (adjList.containsKey(currentCity)) {
                for (Edge edge : adjList.get(currentCity)) {
                    double newCost = minCost.get(currentCity) + edge.price;

                    // Agar naya rasta sasta hai
                    if (newCost < minCost.getOrDefault(edge.destination, Double.MAX_VALUE)) {
                        minCost.put(edge.destination, newCost);
                        parentMap.put(edge.destination, edge.flightDetails); // Record flight
                        pq.add(new Node(edge.destination, newCost));
                    }
                }
            }
        }

        return reconstructPath(parentMap, end);
    }

    // Path wapas nikalne k liye helper method
    private List<Flight> reconstructPath(Map<String, Flight> parents, String end) {
        List<Flight> path = new ArrayList<>();
        String current = end;
        
        // Ulta chalenge (Destination -> Source)
        while (parents.containsKey(current)) {
            Flight f = parents.get(current);
            path.add(0, f); // List k shuru mein add karo
            current = f.getSource(); // Piche wale shehar par jao
        }
        return path; // Agar path khali hai, iska matlab rasta nahi mila
    }

    // Priority Queue k liye choti si class
    private class Node {
        String city;
        double cost;
        public Node(String c, double p) {
            this.city = c; 
            this.cost = p; 
        }
    }
}