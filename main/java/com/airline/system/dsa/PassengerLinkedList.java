package com.airline.system.dsa;

import com.airline.system.model.Passenger;

public class PassengerLinkedList {
    private PassengerNode head;

    public void add(Passenger p) {
        PassengerNode newNode = new PassengerNode(p);
        if (head == null) {
            head = newNode;
        } else {
            PassengerNode temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next = newNode;
        }
    }

    public void displayAll() {
        PassengerNode temp = head;
        while (temp != null) {
            System.out.println("Passenger: " + temp.passenger.getFullName());
            temp = temp.next;
        }
    }
    
    // Size check karne ke liye helper
    public int getSize() {
        int count = 0;
        PassengerNode temp = head;
        while(temp != null) {
            count++;
            temp = temp.next;
        }
        return count;
    }

    // PassengerLinkedList.java ke andar add karein

public java.util.List<com.airline.system.model.Passenger> toList() {
    java.util.List<com.airline.system.model.Passenger> list = new java.util.ArrayList<>();
    PassengerNode current = head;
    while (current != null) {
        list.add(current.passenger);
        current = current.next;
    }
    return list;
}
}