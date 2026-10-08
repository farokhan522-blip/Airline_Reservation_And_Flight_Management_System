package com.airline.system.dsa;

public class UndoStack {

    // 1. Inner Node Class (Dabba)
    private class Node {
        String data;
        Node next;

        Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    // 2. Head of the Stack (Jise hum Top kehte hain)
    private Node top;

    // Constructor
    public UndoStack() {
        this.top = null;
    }

    // --- PUSH (Add to Top) ---
    public void push(String action) {
        Node newNode = new Node(action);
        
        if (top == null) {
            top = newNode;
        } else {
            newNode.next = top; // Naya node purane top ke upar
            top = newNode;      // Top update kardo
        }
    }

    // --- POP (Remove from Top) ---
    public String pop() {
        if (isEmpty()) {
            return null;
        }
        
        String data = top.data; // Data nikalo
        top = top.next;         // Top ko neeche shift karo
        return data;
    }

    // --- PEEK (Sirf dekho, remove mat karo) ---
    // (Ye method AdminService mein error de raha tha, isliye add kiya)
    public String peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    // --- IS EMPTY CHECK ---
    public boolean isEmpty() {
        return top == null;
    }
}