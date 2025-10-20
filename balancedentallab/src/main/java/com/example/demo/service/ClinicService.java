package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Clinic;
import com.example.demo.repository.ClinicRepository;

@Service
@Transactional
public class ClinicService {

    @Autowired
    private ClinicRepository clinicRepo;

    public List<Clinic> getAllClinic() {
        return clinicRepo.findAll();
    }

    public Optional<Clinic> getClinicById(Long id) {
        return clinicRepo.findById(id);
    }

    public Optional<Clinic> getClinicByName(String clinicName) {
        return clinicRepo.findByClinicName(clinicName);
    }

    private String generateClinicNo() {
        String maxClinicNo = clinicRepo.findMaxClinicNo();
        if (maxClinicNo == null) {
            return "C001";
        }

        int num = Integer.parseInt(maxClinicNo.substring(1));
        num++; 
        return String.format("C%03d", num);
    }

    public Clinic createClinic(Clinic request) {
        if (request.getClinicNo() == null || request.getClinicNo().isEmpty()) {
            request.setClinicNo(generateClinicNo());
        }
        return clinicRepo.save(request);
    }

    public Clinic updateClinic(Long id, Clinic request) {
        Clinic clinic = clinicRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("項目不存在: " + id));

        if (!clinic.getClinicNo().equals(request.getClinicNo()) &&
                clinicRepo.existsByClinicNo(request.getClinicNo())) {
            throw new IllegalArgumentException("診所編號已存在: " + request.getClinicNo());
        }

        if (!clinic.getClinicName().equals(request.getClinicName()) &&
                clinicRepo.existsByClinicName(request.getClinicName())) {
            throw new IllegalArgumentException("診所名稱已存在: " + request.getClinicName());
        }

        clinic.setClinicNo(request.getClinicNo());
        clinic.setClinicName(request.getClinicName());
        clinic.setPhone(request.getPhone());
        clinic.setAddress(request.getAddress());

        return clinicRepo.save(clinic);
    }

    public boolean deleteClinic(Long id) {
        if (!clinicRepo.existsById(id)) {
            return false;
        }
        clinicRepo.deleteById(id);
        return true;
    }
    
    public Optional<Clinic> getClinicByNo(String clinicNo) {
        return clinicRepo.findByClinicNo(clinicNo);
    }

}
