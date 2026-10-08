# ✈️ SkyTravel — Enterprise Airline Reservation & Flight Operations Management System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/ORM-Hibernate%20%2F%20JPA-blue.svg)](https://hibernate.org/)
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Frontend-Thymeleaf%20%2B%20Bootstrap-green.svg)](https://www.thymeleaf.org/)

---

## 📌 Project Overview
**SkyTravel** is an enterprise-grade airline reservation and fleet operations platform engineered using **Java 17**, **Spring Boot**, and **MySQL**. Designed to simulate real-world commercial aviation logistics, the platform bridges modern enterprise MVC architecture with bare-metal custom **Data Structures & Algorithms (DSA)** to solve complex scheduling, multi-leg flight path calculations, and dynamic passenger roster management without relying exclusively on third-party collections.

---

## 👨‍💻 Developer Information
* **Lead Developer:** Muhammad Farooq Adnan Khan (M. Farooq)
* **Domain:** Computer Science / Software Engineering & Data Systems
* **Role:** System Architect & Full-Stack Java Developer

---

## 🚀 Key Modules & System Functionalities

### 1. 🔍 Flight Search & Smart Route Engine
* **Direct Flight Search:** Indexed flight retrieval filtered by Source and Destination using a custom Binary Search Tree (**BST**) engine for efficient query resolution.
* **Smart Route Finder (Connecting Flights):** Built-in Graph Engine utilizing **Dijkstra’s Shortest Path Algorithm** on an Adjacency List to compute the cheapest multi-leg flight paths between disconnected airports.
* **Visual Route Timeline:** Toggleable itinerary view displaying multi-hop connection breakdowns, individual leg prices, layover milestones, and total combined fares.

### 2. 💺 Interactive Seat Selection & Booking Engine
* **2D Seat Map Layout:** Visual matrix display representing cabin seating configurations (Economy and Business classes) mapped through dedicated coordinates.
* **Real-time Occupancy Check:** Dynamic seat locking preventing duplicate reservations on identical flight instances.
* **Multi-Passenger Booking:** Multi-ticket reservations handling individual passenger profiles, seat mapping, and meal/travel class preferences.

### 3. 👥 Passenger Management & Portal
* **Role-Based Authentication:** Dedicated registration and login workflows for passengers and administrative staff.
* **Booking Ledger & Dashboard:** Centralized tracking interface for confirmed, scheduled, and past travel itineraries.
* **Cancellations & Manifest Deallocation:** Real-time booking status modification with automated seat release[cite: 2].

### 4. 🛠️ Administrative Operations & Fleet Control
* **Flight Scheduling & Dispatch:** Route definition, aircraft allocation, base fare configurations, and schedule validation.
* **Real-time Fleet Tracking:** Aircraft directory monitoring active (`isBusy`) versus idle planes to prevent dispatch conflicts.
* **Action Reversal (Undo):** Reversible administrative actions (schedule reversals, cancellations) managed through memory rollback stacks.

---

## 🧠 Advanced Data Structures & Algorithmic Design

| Data Structure | Module Integration | Algorithmic Implementation & Time Complexity |
| :--- | :--- | :--- |
| **Weighted Graph (Adjacency List)** | Smart Route Finder | **Dijkstra's Algorithm** with Min-Heap ($O(E \log V)$) for cheapest multi-city connecting itineraries. |
| **Binary Search Tree (BST)** | Direct Flight Engine | Hierarchical lookup by flight keys for $O(\log n)$ average search efficiency. |
| **Custom HashMap (Chaining)** | Fleet & Passenger Directory | Array of linked buckets resolving collisions via chaining for near-instant $O(1)$ lookups. |
| **Queue (FIFO)** | Flight Overbooking Waitlist | Composition-based per-flight queue allocating seats on a First-Come, First-Served basis. |
| **Stack (LIFO)** | Admin Audit & Undo Operations | Constant time $O(1)$ push/pop tracking state snapshots for rollback capabilities. |
| **Singly Linked List** | Dynamic Flight Manifests | Dynamic memory allocation for passenger rosters without static array overhead. |

---

## 🏗️ Technical Architecture & Design Principles

src/main/java/com/airline/system/
├── config/              # Security and Spring Bean configurations
├── controller/          # REST & MVC Controllers (Admin, Passenger, Search)
├── model/               # JPA Entities & Domain Models (Flight, Passenger, Booking, SeatMap)[cite: 1, 2]
├── repository/          # Spring Data JPA interfaces[cite: 2]
├── service/             # Business Logic & Transactional processing
├── dsa/                 # Custom Data Structure implementations
│   ├── graph/          # Graph Nodes, Adjacency List, Dijkstra Runner
│   ├── tree/           # Binary Search Tree implementation
│   ├── hashmap/        # Custom HashMap & Hash Nodes
│   ├── queue/          # Waitlist Queue implementation
│   └── stack/          # Action History Undo Stack
└── AirlineSystemApplication.java # Application Bootstrapper


* **Object-Oriented Integrity:** Full adherence to Encapsulation, Polymorphism, and Composition (Flight has-a SeatMap, has-a Waitlist, has-a PassengerManifest)[cite: 1, 2].
* **Persistence Layer:** Integrated **Spring Data JPA** with **Hibernate** for schema automation, dirty checking, and transaction safety[cite: 2].

---

## ⚙️ Installation & Setup

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher
* **Build Tool:** Maven 3.8+
* **Database Engine:** MySQL Server 8.0+

### Database Configuration
1. Create a MySQL database instance:
   ```sql
   CREATE DATABASE airline_db;s