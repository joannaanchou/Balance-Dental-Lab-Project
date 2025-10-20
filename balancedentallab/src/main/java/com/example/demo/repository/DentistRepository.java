package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Dentist;

import java.util.Optional;

@Repository


public interface DentistRepository extends JpaRepository<Dentist, Long> {
    
    Optional<Dentist> findByDentistNo(String dentistNo);
    Optional<Dentist> findByDentistName(String dentistName);
    boolean existsByDentistNo(String dentistNo);
    boolean existsByDentistName(String dentistName);
}
    