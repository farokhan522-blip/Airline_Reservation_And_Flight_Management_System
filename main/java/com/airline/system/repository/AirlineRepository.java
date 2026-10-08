package com.airline.system.repository;

import com.airline.system.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, String> {
    // Basic CRUD operations (findById, save, etc.) JPA khud sambhal lega
}