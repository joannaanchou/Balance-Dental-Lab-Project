package com.example.demo.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Clinic;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
	@Query("SELECT c.clinicNo FROM Clinic c ORDER BY c.clinicNo DESC")
    List<String> findAllClinicNoDesc(Pageable pageable);

    default String findMaxClinicNo() {
        List<String> list = findAllClinicNoDesc(PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }
    Optional<Clinic> findByClinicNo(String clinicNo);
    Optional<Clinic> findByClinicName(String clinicName);
    
    boolean existsByClinicName(String clinicName);
    boolean existsByClinicNo(String clinicNo);
}