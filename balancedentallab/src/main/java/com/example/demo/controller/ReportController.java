package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.example.demo.dto.ClinicRevenueDTO;
import com.example.demo.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin // 若你保守起見，不想動全域 CORS，可保留；你已有 CorsConfig 也可移除
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/revenue/by-clinic?start=2025-09-01T00:00:00&end=2025-10-14T23:59:59
    @GetMapping("/revenue/by-clinic")
    public List<ClinicRevenueDTO> getRevenueByClinic(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return reportService.getRevenueByClinic(start, end);
    }
}
