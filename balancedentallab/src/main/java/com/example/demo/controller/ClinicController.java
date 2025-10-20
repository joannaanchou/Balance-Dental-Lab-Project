package com.example.demo.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Clinic;
import com.example.demo.model.Dentist;
import com.example.demo.service.ClinicDentistService;
import com.example.demo.service.ClinicService;

@RestController
@RequestMapping("/api")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class ClinicController {

    @Autowired
    private ClinicService clinicService;
    
    @Autowired
    private ClinicDentistService clinicDentistService;

    @GetMapping("/clinics")
    public ResponseEntity<List<Clinic>> getAllClinic() {
        return ResponseEntity.ok(clinicService.getAllClinic());
    }
    
    //新增：用於 create_case html 
    @GetMapping("/clinics/options")
    public ResponseEntity<List<Map<String, Object>>> listClinicOptions() {
        List<Clinic> list = clinicService.getAllClinic();
        List<Map<String, Object>> body = list.stream()
            .map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("clinicNo", c.getClinicNo());   
                m.put("name", c.getClinicName());
                return m;
            })
            .toList();
        return ResponseEntity.ok(body);
    }

    
    


    @GetMapping("/clinics/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable Long id) {
        return clinicService.getClinicById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clinics/name/{clinicName}")
    public ResponseEntity<Clinic> getClinicByName(@PathVariable String clinicName) {
        return clinicService.getClinicByName(clinicName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clinics")
    public ResponseEntity<?> createClinic(@RequestBody Clinic request) {
        try {
            Clinic created = clinicService.createClinic(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/clinics/{id}")
    public ResponseEntity<?> updateClinic(@PathVariable Long id, @RequestBody Clinic request) {
        return clinicService.getClinicById(id)
                .map(existingClinic -> {
                    request.setClinicNo(existingClinic.getClinicNo());

                    existingClinic.setClinicName(request.getClinicName());
                    existingClinic.setPhone(request.getPhone());
                    existingClinic.setAddress(request.getAddress());

                    try {
                        Clinic updated = clinicService.updateClinic(id, existingClinic);
                        return ResponseEntity.ok(updated);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/clinics/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        boolean deleted = clinicService.deleteClinic(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
    
    
    //新增：診所<>醫師連動
    @GetMapping("/clinics/{clinicId}/dentists")
    public ResponseEntity<List<Map<String, Object>>> getDentistsByClinic(@PathVariable Long clinicId) {
        List<Dentist> dentists = clinicDentistService.findDentistsByClinicId(clinicId);
        List<Map<String, Object>> body = dentists.stream()
            .map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", d.getId());
                map.put("dentistNo", d.getDentistNo()); 
                map.put("name", d.getDentistName());
                return map;
            })
            .toList();
        return ResponseEntity.ok(body);
    }

    
    @GetMapping("/clinics/no/{clinicNo}")
    public ResponseEntity<Clinic> getClinicByNo(@PathVariable String clinicNo) {
        return clinicService.getClinicByNo(clinicNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}
