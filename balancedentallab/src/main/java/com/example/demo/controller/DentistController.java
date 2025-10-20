package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Dentist;
import com.example.demo.service.DentistService;

@RestController
@RequestMapping("/api")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class DentistController {

    @Autowired
    private DentistService dentistService;

    @GetMapping("/dentists")
    public ResponseEntity<List<Dentist>> getAllDentist() {
        return ResponseEntity.ok(dentistService.getAllDentist());
    }

    @GetMapping("/dentists/{id}")
    public ResponseEntity<Dentist> getDentistById(@PathVariable Long id) {
        return dentistService.getDentistById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 按姓名查詢
    @GetMapping("/dentists/name/{dentistName}")
    public ResponseEntity<Dentist> getDentistByName(@PathVariable String dentistName) {
        return dentistService.getDentistByName(dentistName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/dentists")
    public ResponseEntity<?> createDentist(@RequestBody Dentist request) {
        try {
            Dentist created = dentistService.createDentist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/dentists/{id}")
    public ResponseEntity<?> updateDentist(@PathVariable Long id, @RequestBody Dentist request) {
        try {
            Dentist updated = dentistService.updateDentist(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/dentists/{id}")
    public ResponseEntity<Void> deleteDentist(@PathVariable Long id) {
        boolean deleted = dentistService.deleteDentist(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/dentists/no/{dentistNo}")
    public ResponseEntity<Dentist> getDentistByNo(@PathVariable String dentistNo) {
        return dentistService.getDentistByNo(dentistNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
