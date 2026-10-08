package com.airline.system.repository;

import java.util.List;
import com.airline.system.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, String> {

    // 1. Login ke liye (Agar email match kare)
    Passenger findByEmail(String email);

    // 2. Login ke liye (Agar Passport ID match kare)
    Passenger findByPassportNumber(String passportNumber);

    // 3. Duplicate check karne ke liye
    boolean existsByEmail(String email);

    Passenger findByEmailAndPassword(String email, String password);

    List<Passenger> findByIsRegisteredTrue();

}

