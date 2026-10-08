package com.airline.system.dsa;

import com.airline.system.model.Booking;

public class BookingQueue {
    private BookingNode front, rear;
    private int size = 0;

    // ✅ Ab ye 'Booking' object accept karega
    public void enqueue(Booking b) {
        BookingNode newNode = new BookingNode(b);
        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    // ✅ Return bhi 'Booking' karega
    public Booking dequeue() {
        if (front == null) return null;
        
        Booking temp = front.booking;
        front = front.next;
        size--;

        if (front == null) rear = null;
        return temp;
    }
    
    // Sirf dekhne k liye (Remove nahi karega)
    public Booking peek() {
        if (front == null) return null;
        return front.booking;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }

    // 🔥 NEW HELPER: Admin View k liye list return karega
    public java.util.List<Booking> toList() {
        java.util.List<Booking> list = new java.util.ArrayList<>();
        BookingNode current = front;
        while (current != null) {
            list.add(current.booking);
            current = current.next;
        }
        return list;
    }
}