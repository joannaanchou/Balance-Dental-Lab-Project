package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.example.demo.dto.ClinicRevenueDTO;
import com.example.demo.repository.CaseRepository;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CaseRepository caseRepository;

    public List<ClinicRevenueDTO> getRevenueByClinic(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = caseRepository.sumRevenueByClinicBetween(start, end);
        List<ClinicRevenueDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new ClinicRevenueDTO(
                (String) r[0],
                (BigDecimal) r[1]
            ));
        }
        return result;
    }
}
