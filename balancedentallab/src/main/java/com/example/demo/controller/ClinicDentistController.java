package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.ClinicDentist;
import com.example.demo.service.ClinicDentistService;

@RestController
@RequestMapping("/api")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class ClinicDentistController {

    @Autowired
    private ClinicDentistService clinicDentistService;

    // 🔹 取得全部
    @GetMapping("/clinic-dentists")
    public ResponseEntity<List<ClinicDentist>> getAllClinicDentist() {
        return ResponseEntity.ok(clinicDentistService.getAllClinicDentist());
    }

    // 🔹 依ID查詢
    @GetMapping("/clinic-dentists/{id}")
    public ResponseEntity<ClinicDentist> getClinicDentistById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicDentistService.getClinicDentistById(id));
    }

    // 🔹 新增
    @PostMapping("/clinic-dentists")
    public ResponseEntity<ClinicDentist> createClinicDentist(@RequestBody ClinicDentist request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicDentistService.createClinicDentist(request));
    }

    // 🔹 修改
    @PutMapping("/clinic-dentists/{id}")
    public ResponseEntity<ClinicDentist> updateClinicDentist(@PathVariable Long id, @RequestBody ClinicDentist request) {
        return ResponseEntity.ok(clinicDentistService.updateClinicDentist(id, request));
    }

    // 🔹 刪除
    @DeleteMapping("/clinic-dentists/{id}")
    public ResponseEntity<Void> deleteClinicDentist(@PathVariable Long id) {
        clinicDentistService.deleteClinicDentist(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 依診所編號查詢牙醫
    @GetMapping("/clinic-dentists/clinic/{clinicNo}")
    public ResponseEntity<List<ClinicDentist>> getDentistsByClinicNo(@PathVariable String clinicNo) {
        List<ClinicDentist> list = clinicDentistService.getDentistsByClinicNo(clinicNo);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    // 🔹 依牙醫編號查詢診所
    @GetMapping("/clinic-dentists/dentist/{dentistNo}")
    public ResponseEntity<List<ClinicDentist>> getClinicsByDentistNo(@PathVariable String dentistNo) {
        List<ClinicDentist> list = clinicDentistService.getClinicsByDentistNo(dentistNo);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    // 🔹 檢查診所與牙醫的關聯是否已存在
    // 範例：GET /api/clinic-dentists/check?clinicNo=C001&dentistNo=D002
    @GetMapping("/clinic-dentists/check")
    public ResponseEntity<Boolean> checkRelationExists(
            @RequestParam String clinicNo,
            @RequestParam String dentistNo) {
        boolean exists = clinicDentistService.checkRelationExists(clinicNo, dentistNo);
        return ResponseEntity.ok(exists);
    }
}
