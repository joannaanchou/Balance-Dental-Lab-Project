package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class CaseCreateRequest {
    private String patientName;      // "test"
    private String returnAt;         // "2025-10-23" (字串，後面轉 LocalDate)

    // 前端送的是巢狀 { clinic: { clinicName }, dentist: { dentistName }, createdBy: { empNo } }
    private ClinicPart clinic;
    private DentistPart dentist;
    private MemberPart createdBy;
    
    private List<ProductLineReq> products;

    @Data 
    public static class ClinicPart  { private String clinicName; }
    @Data 
    public static class DentistPart { private String dentistName; }
    @Data 
    public static class MemberPart  { private String empNo; }
    
    @Data
    public static class ProductLineReq {
        private Long categoryId;
        private Long itemId;
        private Integer quantity;       // 允許前端不填或 <=0，後端再防呆成 1
        private Integer toothCode;  // 例如 11、12...
    }
}
