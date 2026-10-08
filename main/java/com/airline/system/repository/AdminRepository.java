package com.airline.system.repository;

import com.airline.system.model.Admin; // Ye class hum abhi banayenge
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    // Login ke liye username aur password check karo
    Admin findByUsernameAndPassword(String username, String password);
}