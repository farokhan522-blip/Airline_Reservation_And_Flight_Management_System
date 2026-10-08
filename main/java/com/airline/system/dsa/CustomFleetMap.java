package com.airline.system.dsa;

import java.util.HashMap;
import java.util.Map;

public class CustomFleetMap {
    private FleetNode[] buckets;
    private int size;

    public CustomFleetMap(int capacity) {
        this.size = capacity;
        this.buckets = new FleetNode[capacity];
    }

    private int getBucketIndex(String key) {
        return Math.abs(key.hashCode() % size);
    }

    // 1. PUT (Sirf ID aur Capacity store karo)
    public void put(String key, int capacity) {
        int index = getBucketIndex(key);
        FleetNode head = buckets[index];

        while (head != null) {
            if (head.key.equals(key)) {
                head.capacity = capacity; // Sirf Capacity update hogi
                return;
            }
            head = head.next;
        }

        FleetNode newNode = new FleetNode(key, capacity);
        newNode.next = buckets[index];
        buckets[index] = newNode;
    }

    // 2. BUSY STATUS SETTER
    public void setBusyStatus(String key, boolean status) {
        int index = getBucketIndex(key);
        FleetNode head = buckets[index];
        while (head != null) {
            if (head.key.equals(key)) {
                head.isBusy = status;
                return;
            }
            head = head.next;
        }
    }

    // 3. IS BUSY CHECK
    public boolean isPlaneBusy(String key) {
        int index = getBucketIndex(key);
        FleetNode head = buckets[index];
        while (head != null) {
            if (head.key.equals(key)) return head.isBusy;
            head = head.next;
        }
        return false;
    }
    
    // 4. CLEAR
    public void clear() { this.buckets = new FleetNode[size]; }

    // 5. EXPORT DATA (Dropdown k liye simple Map: ID -> Capacity)
    public Map<String, Integer> getFreePlanes() {
        Map<String, Integer> freeList = new HashMap<>();
        for (int i = 0; i < size; i++) {
            FleetNode head = buckets[i];
            while (head != null) {
                if (!head.isBusy) {
                    freeList.put(head.key, head.capacity);
                }
                head = head.next;
            }
        }
        return freeList;
    }

    // Check karne k liye k kya ye ID exist karti hai?
    public boolean containsKey(String key) {
        int index = getBucketIndex(key);
        FleetNode head = buckets[index];

        while (head != null) {
            if (head.key.equals(key)) {
                return true; // Mil gaya! Pehle se hai.
            }
            head = head.next;
        }
        return false; // Nahi mila, yani Naya hai.
    }
}