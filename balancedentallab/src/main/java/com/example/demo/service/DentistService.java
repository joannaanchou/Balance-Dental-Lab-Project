package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Dentist;
import com.example.demo.repository.DentistRepository;

@Service
@Transactional
public class DentistService {

    @Autowired
    private DentistRepository dentistRepo;

    public List<Dentist> getAllDentist() {
        return dentistRepo.findAll();
    }

    public Optional<Dentist> getDentistById(Long id) {
        return dentistRepo.findById(id);
    }

    public Optional<Dentist> getDentistByName(String dentistName) {
        return dentistRepo.findByDentistName(dentistName);
    }

    // 創建牙醫，自動生成編號 D001、D002...
    public Dentist createDentist(Dentist request) {
        if (dentistRepo.existsByDentistName(request.getDentistName())) {
            throw new IllegalArgumentException("醫師姓名已存在: " + request.getDentistName());
        }

        // 自動生成 dentistNo
        String maxNo = dentistRepo.findAll().stream()
                .map(Dentist::getDentistNo)
                .max(String::compareTo)
                .orElse("D001");

        int nextNo = Integer.parseInt(maxNo.substring(1)) + 1;
        String newDentistNo = String.format("D%03d", nextNo);

        request.setDentistNo(newDentistNo);

        return dentistRepo.save(request);
    }

    // 更新姓名（編號不可修改）
    public Dentist updateDentist(Long id, Dentist request) {
        Dentist dentist = dentistRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("項目不存在: " + id));

        if (!dentist.getDentistName().equals(request.getDentistName()) &&
                dentistRepo.existsByDentistName(request.getDentistName())) {
            throw new IllegalArgumentException("醫師姓名已存在: " + request.getDentistName());
        }

        dentist.setDentistName(request.getDentistName());

        return dentistRepo.save(dentist);
    }

    public boolean deleteDentist(Long id) {
        if (!dentistRepo.existsById(id)) {
            return false;
        }
        dentistRepo.deleteById(id);
        return true;
    }
    
    public Optional<Dentist> getDentistByNo(String dentistNo) {
        return dentistRepo.findByDentistNo(dentistNo);
    }

}
