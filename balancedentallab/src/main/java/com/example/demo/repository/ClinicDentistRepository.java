package com.example.demo.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.ClinicDentist;

@Repository
public interface ClinicDentistRepository extends JpaRepository<ClinicDentist, Long> {
	List<ClinicDentist> findByClinicId(Long clinicId);
	// 查某診所的牙醫清單
    List<ClinicDentist> findByClinic_ClinicNo(String clinicNo);

    // 查某牙醫的診所清單
    List<ClinicDentist> findByDentist_DentistNo(String dentistNo);

    // 判斷診所與牙醫是否已建立關聯
    boolean existsByClinic_ClinicNoAndDentist_DentistNo(String clinicNo, String dentistNo);
}